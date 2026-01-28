package com.townssiege.hytale.notification;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.townssiege.hytale.integration.SimpleClaimsIntegration;
import com.townssiege.models.Siege;
import com.townssiege.models.Team;
import com.townssiege.models.TeamRole;
import com.townssiege.SiegeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SiegeNotifier {

    private static final long FIVE_MINUTES = 5 * 60 * 1000;
    private static final long ONE_MINUTE = 60 * 1000;
    private static final long THIRTY_SECONDS = 30 * 1000;

    private final SiegeManager siegeManager;
    private final ScheduledExecutorService scheduler;
    private final Map<UUID, NotifiedState> notifiedStates = new ConcurrentHashMap<>();
    private ScheduledFuture<?> tickTask;
    private boolean running;

    public SiegeNotifier(SiegeManager siegeManager) {
        this.siegeManager = siegeManager;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (running) return;
        running = true;
        tickTask = scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        running = false;
        if (tickTask != null) tickTask.cancel(false);
        scheduler.shutdown();
    }

    private void tick() {
        if (!running) return;
        Map<UUID, Siege> activeSieges = siegeManager.getActiveSieges();
        notifiedStates.keySet().removeIf(id -> !activeSieges.containsKey(id));

        List<UUID> expiredSieges = new ArrayList<>();

        for (Map.Entry<UUID, Siege> entry : activeSieges.entrySet()) {
            UUID territoryId = entry.getKey();
            Siege siege = entry.getValue();

            boolean isNew = !notifiedStates.containsKey(territoryId);
            NotifiedState state = notifiedStates.computeIfAbsent(territoryId, k -> new NotifiedState());

            if (isNew) {
                broadcastToSiege(siege, SiegeMessages.siegeStarted());
                state.wasBannerActive = siege.isBannerActive();
                state.lastScoreUpdate = System.currentTimeMillis();
            }

            checkNotifiedState(siege, state);

            if (siege.isSiegeExpired() && !state.claimTransferred) {
                state.claimTransferred = true;
                handleSiegeEnd(siege);
                expiredSieges.add(territoryId);
            }
        }

        for (UUID territoryId : expiredSieges) {
            siegeManager.endSiege(territoryId);
            notifiedStates.remove(territoryId);
        }
    }

    private void checkNotifiedState(Siege siege, NotifiedState state) {
        checkBannerState(siege, state);
        checkSiegeExpiration(siege, state);
        checkScoreUpdate(siege, state);
    }

    private void checkScoreUpdate(Siege siege, NotifiedState state) {
        long now = System.currentTimeMillis();
        if (now - state.lastScoreUpdate >= THIRTY_SECONDS) {
            state.lastScoreUpdate = now;
            int attackerPoints = siege.getAttackers().getPoints();
            int defenderPoints = siege.getDefenders().getPoints();
            broadcastToSiege(siege, SiegeMessages.scoreUpdate(attackerPoints, defenderPoints));
        }
    }

    private void checkBannerState(Siege siege, NotifiedState state) {
        boolean active = siege.isBannerActive();

        if (active == state.wasBannerActive) {
            if (!active && !siege.isSiegeExpired()) {
                checkTimeWarning(siege, state.bannerWarnings, siege.getTimeUntilBannerActive(), true);
            }
            return;
        }

        state.wasBannerActive = active;
        state.bannerWarnings.reset();
        broadcastToSiege(siege, active ? SiegeMessages.bannerActive() : SiegeMessages.bannerInactive());
    }

    private void checkSiegeExpiration(Siege siege, NotifiedState state) {
        if (siege.isSiegeExpired()) {
            return;
        }
        checkTimeWarning(siege, state.siegeWarnings, siege.getRemainingSiegeTime(), false);
    }

    private void handleSiegeEnd(Siege siege) {
        Team winner = siege.getWinner();

        // Final score announcement
        int attackerPoints = siege.getAttackers().getPoints();
        int defenderPoints = siege.getDefenders().getPoints();
        broadcastToSiege(siege, SiegeMessages.scoreUpdate(attackerPoints, defenderPoints));

        if (winner == null) {
            broadcastToSiege(siege, SiegeMessages.siegeEndDraw());
            return;
        }

        boolean attackersWon = winner.getRole() == TeamRole.ATTACKER;

        if (attackersWon) {
            boolean transferred = transferClaimToAttacker(siege);
            if (transferred) {
                broadcastToSiege(siege, SiegeMessages.siegeEndAttackersWon());
            } else {
                broadcastToSiege(siege, SiegeMessages.siegeEndAttackersWonTransferFailed());
            }
        } else {
            broadcastToSiege(siege, SiegeMessages.siegeEndDefendersWon());
        }
    }

    private boolean transferClaimToAttacker(Siege siege) {
        ChunkInfo originClaim = ClaimManager.getInstance().getChunk(
                siege.getDimension(),
                siege.getChunkX(),
                siege.getChunkZ()
        );

        if (originClaim == null) {
            return false;
        }

        UUID defenderPartyId = originClaim.getPartyOwner();

        UUID attackerLeader = siege.getAttackers().getPlayers().stream().findFirst().orElse(null);
        if (attackerLeader == null) {
            return false;
        }

        UUID attackerPartyId = SimpleClaimsIntegration.getPlayerParty(attackerLeader);
        if (attackerPartyId == null) {
            return false;
        }

        // Get all chunks owned by the defender's party
        Map<String, List<ChunkInfo>> defenderChunks = SimpleClaimsIntegration.getAllChunksByParty(defenderPartyId);

        if (defenderChunks.isEmpty()) {
            return false;
        }

        // Transfer all chunks to the attacker
        int transferred = 0;
        for (Map.Entry<String, List<ChunkInfo>> entry : defenderChunks.entrySet()) {
            String dimension = entry.getKey();
            for (ChunkInfo chunk : entry.getValue()) {
                if (SimpleClaimsIntegration.transferClaim(dimension, chunk, attackerPartyId)) {
                    transferred++;
                }
            }
        }

        return transferred > 0;
    }

    private void checkTimeWarning(Siege siege, TimeWarnings warnings, long remaining, boolean isBannerWarning) {
        if (remaining <= ONE_MINUTE && !warnings.sent1m) {
            warnings.sent1m = true;
            Message msg = isBannerWarning
                    ? SiegeMessages.bannerActivatesIn("1 minute")
                    : SiegeMessages.siegeEndsIn("1 minute");
            broadcastToSiege(siege, msg);
        } else if (remaining <= FIVE_MINUTES && !warnings.sent5m) {
            warnings.sent5m = true;
            Message msg = isBannerWarning
                    ? SiegeMessages.bannerActivatesIn("5 minutes")
                    : SiegeMessages.siegeEndsIn("5 minutes");
            broadcastToSiege(siege, msg);
        }
    }

    private void broadcastToSiege(Siege siege, Message message) {
        Set<UUID> attackers = siege.getAttackers().getPlayers();
        Set<UUID> defenders = siege.getDefenders().getPlayers();
        for (UUID playerId : attackers) sendMessage(playerId, message);
        for (UUID playerId : defenders) sendMessage(playerId, message);
    }

    private void sendMessage(UUID playerId, Message message) {
        Universe universe = Universe.get();
        if (universe == null) return;
        PlayerRef player = universe.getPlayer(playerId);
        if (player != null) player.sendMessage(message);
    }

    private static class NotifiedState {
        boolean wasBannerActive = false;
        boolean claimTransferred = false;
        long lastScoreUpdate = 0;
        final TimeWarnings bannerWarnings = new TimeWarnings();
        final TimeWarnings siegeWarnings = new TimeWarnings();
    }

    private static class TimeWarnings {
        boolean sent5m = false;
        boolean sent1m = false;

        void reset() {
            sent5m = false;
            sent1m = false;
        }
    }
}
