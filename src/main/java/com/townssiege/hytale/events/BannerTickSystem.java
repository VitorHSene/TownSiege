package com.townssiege.hytale.events;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.SiegeManager;
import com.townssiege.TownsSiege;
import com.townssiege.hytale.notification.SiegeMessages;
import com.townssiege.models.Siege;
import com.townssiege.models.Team;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BannerTickSystem extends EntityTickingSystem<EntityStore> {

    private static final int POINTS_PER_REWARD = 10;
    private static final double BANNER_RANGE = 40.0;
    private static final int REWARD_INTERVAL_TICKS = 300;

    private final Map<UUID, Integer> tickCounter = new HashMap<>();

    @Override
    public void tick(float v, int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        Player player = archetypeChunk.getComponent(i, Player.getComponentType());

        if (player == null)
            return;

        PlayerRef playerRef = store.getComponent(player.getReference(), PlayerRef.getComponentType());

        if (playerRef == null)
            return;
        UUID playerId = playerRef.getUuid();

        SiegeManager siegeManager = TownsSiege.getInstance().getSiegeManager();

        Siege siege = siegeManager.getSiegeByPlayer(playerId);
        if (siege == null)
            return;

        if (!siege.isBannerActive())
            return;

        var transform = store.getComponent(player.getReference(), EntityModule.get().getTransformComponentType());

        Vector3d playerPos = transform.getPosition();
        Vector3i bannerPos = siege.getBannerLocation();

        double distance = calculateDistance(playerPos, bannerPos);
        if (distance > BANNER_RANGE)
            return;

        int ticks = tickCounter.getOrDefault(playerId, 0) + 1;

        if (ticks >= REWARD_INTERVAL_TICKS) {
            siege.addPoints(playerId, POINTS_PER_REWARD);
            Team team = siege.getPlayerTeam(playerId);
            if (team != null) {
                playerRef.sendMessage(SiegeMessages.pointsEarnedTerritory(POINTS_PER_REWARD, team.getPoints()));
            }
            ticks = 0;
        }

        tickCounter.put(playerId, ticks);
    }

    private double calculateDistance(Vector3d playerPos, Vector3i bannerPos) {
        double dx = playerPos.getX() - bannerPos.getX();
        double dy = playerPos.getY() - bannerPos.getY();
        double dz = playerPos.getZ() - bannerPos.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }
}
