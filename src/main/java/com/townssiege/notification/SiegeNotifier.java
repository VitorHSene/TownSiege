package com.townssiege.notification;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.townssiege.models.Siege;
import com.townssiege.models.SiegeManager;

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
    private final Map<UUID, SiegeState> siegeStates = new ConcurrentHashMap<>();
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
        siegeStates.keySet().removeIf(id -> !activeSieges.containsKey(id));

        for (Map.Entry<UUID, Siege> entry : activeSieges.entrySet()) {
            UUID territoryId = entry.getKey();
            Siege siege = entry.getValue();

            boolean isNew = !siegeStates.containsKey(territoryId);
            SiegeState state = siegeStates.computeIfAbsent(territoryId, k -> new SiegeState());

            if (isNew) {
                broadcastToSiege(siege, "[Siege] A siege has begun! Prepare for battle!");
                state.wasBannerActive = siege.isBannerActive();
            }

            checkSiegeState(siege, state);
        }
    }

    private void checkSiegeState(Siege siege, SiegeState state) {
        boolean bannerActive = siege.isBannerActive();
        boolean siegeExpired = siege.isSiegeExpired();
        long timeUntilBanner = siege.getTimeUntilBannerActive();
        long remainingSiegeTime = siege.getRemainingSiegeTime();

        if (bannerActive && !state.wasBannerActive) {
            broadcastToSiege(siege, "[Siege] The banner is now ACTIVE! Capture it!");
            state.wasBannerActive = true;
            state.sentBannerWarning5m = false;
            state.sentBannerWarning1m = false;
        }

        if (!bannerActive && state.wasBannerActive) {
            broadcastToSiege(siege, "[Siege] The banner is no longer active.");
            state.wasBannerActive = false;
            state.sentBannerWarning5m = false;
            state.sentBannerWarning1m = false;
        }

        if (!bannerActive && !siegeExpired) {
            if (timeUntilBanner <= FIVE_MINUTES && !state.sentBannerWarning5m) {
                broadcastToSiege(siege, "[Siege] Banner activates in 5 minutes!");
                state.sentBannerWarning5m = true;
            }
            if (timeUntilBanner <= ONE_MINUTE && !state.sentBannerWarning1m) {
                broadcastToSiege(siege, "[Siege] Banner activates in 1 minute!");
                state.sentBannerWarning1m = true;
            }
        }

        if (!siegeExpired) {
            if (remainingSiegeTime <= FIVE_MINUTES && !state.sentSiegeWarning5m) {
                broadcastToSiege(siege, "[Siege] Siege ends in 5 minutes!");
                state.sentSiegeWarning5m = true;
            }
            if (remainingSiegeTime <= ONE_MINUTE && !state.sentSiegeWarning1m) {
                broadcastToSiege(siege, "[Siege] Siege ends in 1 minute!");
                state.sentSiegeWarning1m = true;
            }
        }

        if (siegeExpired && !state.siegeEnded) {
            var winner = siege.getWinner();
            String msg = winner != null
                ? "[Siege] Siege has ended! Winner: " + winner.getRole().name()
                : "[Siege] Siege has ended in a tie!";
            broadcastToSiege(siege, msg);
            state.siegeEnded = true;
        }
    }

    private void broadcastToSiege(Siege siege, String message) {
        Set<UUID> attackers = siege.getAttackers().getPlayers();
        Set<UUID> defenders = siege.getDefenders().getPlayers();
        for (UUID playerId : attackers) sendMessage(playerId, message);
        for (UUID playerId : defenders) sendMessage(playerId, message);
    }

    private void sendMessage(UUID playerId, String message) {
        PlayerRef player = Universe.get().getPlayer(playerId);
        if (player != null) player.sendMessage(Message.raw(message));
    }

    private static class SiegeState {
        boolean wasBannerActive = false;
        boolean sentBannerWarning5m = false;
        boolean sentBannerWarning1m = false;
        boolean sentSiegeWarning5m = false;
        boolean sentSiegeWarning1m = false;
        boolean siegeEnded = false;
    }
}
