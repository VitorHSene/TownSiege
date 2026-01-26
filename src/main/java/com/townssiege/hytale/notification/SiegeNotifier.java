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
                broadcastToSiege(siege, "[Siege] A siege has begun! Prepare for battle!");
                state.wasBannerActive = siege.isBannerActive();
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
    }

    private void checkBannerState(Siege siege, NotifiedState state) {
        boolean active = siege.isBannerActive();

        if (active == state.wasBannerActive) {
            if (!active && !siege.isSiegeExpired()) {
                checkTimeWarning(siege, state.bannerWarnings, siege.getTimeUntilBannerActive(), "Banner activates");
            }
            return;
        }

        state.wasBannerActive = active;
        state.bannerWarnings.reset();
        broadcastToSiege(siege, active
            ? "[Siege] The banner is now ACTIVE! Capture it!"
            : "[Siege] The banner is no longer active.");
    }

    private void checkSiegeExpiration(Siege siege, NotifiedState state) {
        if (siege.isSiegeExpired()) {
            return;
        }
        checkTimeWarning(siege, state.siegeWarnings, siege.getRemainingSiegeTime(), "Siege ends");
    }

    private void handleSiegeEnd(Siege siege) {
        Team winner = siege.getWinner();

        if (winner == null) {
            broadcastToSiege(siege, "[Siege] The siege ended in a draw! Territory remains with defenders.");
            return;
        }

        boolean attackersWon = winner.getRole() == TeamRole.ATTACKER;

        if (attackersWon) {
            boolean transferred = transferClaimToAttacker(siege);
            if (transferred) {
                broadcastToSiege(siege, "[Siege] Attackers won! Territory has been conquered!");
            } else {
                broadcastToSiege(siege, "[Siege] Attackers won but claim transfer failed!");
            }
        } else {
            broadcastToSiege(siege, "[Siege] Defenders won! Territory remains protected.");
        }
    }

    private boolean transferClaimToAttacker(Siege siege) {
        ChunkInfo claim = ClaimManager.getInstance().getChunk(
                siege.getDimension(),
                siege.getChunkX(),
                siege.getChunkZ()
        );

        if (claim == null) {
            return false;
        }

        UUID attackerLeader = siege.getAttackers().getPlayers().stream().findFirst().orElse(null);
        if (attackerLeader == null) {
            return false;
        }

        UUID attackerPartyId = SimpleClaimsIntegration.getPlayerParty(attackerLeader);
        if (attackerPartyId == null) {
            return false;
        }

        return SimpleClaimsIntegration.transferClaim(siege.getDimension(), claim, attackerPartyId);
    }

    private void checkTimeWarning(Siege siege, TimeWarnings warnings, long remaining, String prefix) {
        if (remaining <= ONE_MINUTE && !warnings.sent1m) {
            warnings.sent1m = true;
            broadcastToSiege(siege, "[Siege] " + prefix + " in 1 minute!");
        } else if (remaining <= FIVE_MINUTES && !warnings.sent5m) {
            warnings.sent5m = true;
            broadcastToSiege(siege, "[Siege] " + prefix + " in 5 minutes!");
        }
    }

    private void broadcastToSiege(Siege siege, String message) {
        Set<UUID> attackers = siege.getAttackers().getPlayers();
        Set<UUID> defenders = siege.getDefenders().getPlayers();
        for (UUID playerId : attackers) sendMessage(playerId, message);
        for (UUID playerId : defenders) sendMessage(playerId, message);
    }

    private void sendMessage(UUID playerId, String message) {
        Universe universe = Universe.get();
        if (universe == null) return;
        PlayerRef player = universe.getPlayer(playerId);
        if (player != null) player.sendMessage(Message.raw(message));
    }

    private static class NotifiedState {
        boolean wasBannerActive = false;
        boolean claimTransferred = false;
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
