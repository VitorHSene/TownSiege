package com.townssiege.hytale.events;

import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.TownsSiege;
import com.townssiege.hytale.integration.SimpleClaimsIntegration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

public class PlaceBannerEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    private static final String SIEGE_BANNER_ITEM_ID = "Furniture_Temple_Light_Brazier";

    public PlaceBannerEvent() {
        super(PlaceBlockEvent.class);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull PlaceBlockEvent placeBlockEvent) {
        if (!isSiegeBanner(placeBlockEvent)) {
            return;
        }

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

        handleSiegeBannerPlacement(player, playerRef, placeBlockEvent.getTargetBlock());
    }

    private boolean isSiegeBanner(PlaceBlockEvent event) {
        var item = event.getItemInHand();
        return item != null && Objects.equals(item.getItemId(), SIEGE_BANNER_ITEM_ID);
    }

    private void handleSiegeBannerPlacement(Player player, PlayerRef playerRef, Vector3i bannerPos) {
        var world = player.getWorld();
        if (world == null) {
            return;
        }

        UUID attackerId = playerRef.getUuid();

        if (TownsSiege.getInstance().getSiegeManager().isPlayerAtSiege(attackerId)) {
            player.sendMessage(Message.raw("You are already participating in a siege!"));
            return;
        }

        ChunkInfo nearestClaim = SimpleClaimsIntegration.findNearestClaim(
                world.getName(),
                bannerPos.getX(),
                bannerPos.getZ()
        );

        if (nearestClaim == null) {
            player.sendMessage(Message.raw("No territory found within range!"));
            return;
        }

        UUID defenderId = SimpleClaimsIntegration.getClaimOwner(nearestClaim);
        if (defenderId == null) {
            player.sendMessage(Message.raw("Could not find territory owner!"));
            return;
        }

        if (attackerId.equals(defenderId)) {
            player.sendMessage(Message.raw("You cannot siege your own territory!"));
            return;
        }

        startSiege(player, nearestClaim, attackerId, defenderId, bannerPos);
    }

    private void startSiege(Player player, ChunkInfo claim, UUID attackerId, UUID defenderId, Vector3i bannerPos) {
        UUID territoryId = claim.getPartyOwner();
        String dimension = player.getWorld().getName();

        boolean started = TownsSiege.getInstance().getSiegeManager()
                .startSiege(territoryId, attackerId, defenderId, bannerPos,
                        dimension, claim.getChunkX(), claim.getChunkZ());

        if (started) {
            player.sendMessage(Message.raw("Siege started! You are attacking this territory."));
        } else {
            player.sendMessage(Message.raw("A siege is already in progress for this territory!"));
        }
    }
}

