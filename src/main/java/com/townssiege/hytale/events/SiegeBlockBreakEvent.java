package com.townssiege.hytale.events;

import com.buuz135.simpleclaims.claim.ClaimManager;
import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.TownsSiege;
import com.townssiege.hytale.integration.SimpleClaimsIntegration;
import com.townssiege.hytale.notification.SiegeMessages;
import com.townssiege.models.Siege;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;

public class SiegeBlockBreakEvent extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private static final int PROTECTION_RADIUS = 40;
    private static final int PROTECTION_RADIUS_SQ = PROTECTION_RADIUS * PROTECTION_RADIUS;

    public SiegeBlockBreakEvent() {
        super(BreakBlockEvent.class);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull BreakBlockEvent breakBlockEvent) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            return;
        }

        var reference = player.getReference();
        if (reference == null) {
            return;
        }

        PlayerRef playerRef = store.getComponent(reference, PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        Vector3i blockPos = breakBlockEvent.getTargetBlock();

        if (shouldProtectBlock(player, playerRef, blockPos, breakBlockEvent)) {
            return;
        }
    }

    private boolean shouldProtectBlock(Player player, PlayerRef playerRef, Vector3i blockPos, BreakBlockEvent event) {
        Map<UUID, Siege> sieges = TownsSiege.getInstance().getSiegeManager().getActiveSieges();
        if (sieges.isEmpty()) {
            return false;
        }

        var world = player.getWorld();
        if (world == null) {
            return false;
        }

        String dimension = world.getName();
        UUID playerId = playerRef.getUuid();

        for (Siege siege : sieges.values()) {
            if (!siege.getDimension().equals(dimension)) {
                continue;
            }

            Vector3i bannerPos = siege.getBannerLocation();
            int dx = blockPos.getX() - bannerPos.getX();
            int dy = blockPos.getY() - bannerPos.getY();
            int dz = blockPos.getZ() - bannerPos.getZ();
            int distSq = dx * dx + dy * dy + dz * dz;

            if (distSq <= PROTECTION_RADIUS_SQ) {
                // Block is within protection radius - check if player is defender in their own claim
                if (siege.isDefender(playerId)) {
                    int chunkX = ChunkUtil.chunkCoordinate(blockPos.getX());
                    int chunkZ = ChunkUtil.chunkCoordinate(blockPos.getZ());
                    ChunkInfo claim = ClaimManager.getInstance().getChunk(dimension, chunkX, chunkZ);

                    if (claim != null) {
                        UUID defenderParty = SimpleClaimsIntegration.getPlayerParty(playerId);
                        if (defenderParty != null && claim.getPartyOwner().equals(defenderParty)) {
                            // Defender breaking block in their own claim - allow it
                            return false;
                        }
                    }
                }

                // Block protected - cancel the event
                event.setCancelled(true);
                player.sendMessage(SiegeMessages.cannotBreakNearBanner());
                return true;
            }
        }

        return false;
    }
}

