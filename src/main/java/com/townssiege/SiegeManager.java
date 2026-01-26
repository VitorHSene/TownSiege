package com.townssiege;

import com.hypixel.hytale.math.vector.Vector3i;
import com.townssiege.models.Siege;
import com.townssiege.utils.SystemTimeProvider;
import com.townssiege.utils.TimeProvider;

import java.util.*;

public class SiegeManager {

    private final Map<UUID, Siege> activeSieges;
    private final TimeProvider timeProvider;
    private final long bannerInterval;
    private final long bannerDuration;
    private final long siegeDuration;

    public SiegeManager(long bannerInterval, long bannerDuration, long siegeDuration) {
        this(new SystemTimeProvider(), bannerInterval, bannerDuration, siegeDuration);
    }

    public SiegeManager(TimeProvider timeProvider, long bannerInterval, long bannerDuration, long siegeDuration) {
        this.activeSieges = new HashMap<>();
        this.timeProvider = timeProvider;
        this.bannerInterval = bannerInterval;
        this.bannerDuration = bannerDuration;
        this.siegeDuration = siegeDuration;
    }

    public boolean startSiege(UUID territoryId, UUID attackerId, UUID defenderId, Vector3i bannerLocation,
                              String dimension, int chunkX, int chunkZ) {
        if (activeSieges.containsKey(territoryId)) {
            return false;
        }
        activeSieges.put(territoryId, new Siege(timeProvider, bannerInterval, bannerDuration, siegeDuration,
                attackerId, defenderId, bannerLocation, dimension, chunkX, chunkZ));
        return true;
    }

    public boolean endSiege(UUID territoryId) {
        return activeSieges.remove(territoryId) != null;
    }

    public boolean isUnderSiege(UUID territoryId) {
        return this.activeSieges.containsKey(territoryId);
    }

    public Siege getSiege(UUID territoryId) {
        return activeSieges.get(territoryId);
    }

    public Siege getSiegeByPlayer(UUID playerId) {
        for (Siege siege : activeSieges.values()) {
            if (siege.isAtSiege(playerId)) {
                return siege;
            }
        }
        return null;
    }

    public boolean isPlayerAtSiege(UUID playerId) {
        return getSiegeByPlayer(playerId) != null;
    }

    public Map<UUID, Siege> getActiveSieges() {
        return activeSieges;
    }
}