package com.townssiege.hytale.events;

import com.buuz135.simpleclaims.claim.chunk.ChunkInfo;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.TownsSiege;
import com.townssiege.hytale.integration.SimpleClaimsIntegration;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class PlaceBannerEvent extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    public PlaceBannerEvent() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }

    @Override
    public void handle(int i, @NotNull ArchetypeChunk<EntityStore> archetypeChunk, @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer, @NotNull PlaceBlockEvent placeBlockEvent) {

        if (Objects.equals(placeBlockEvent.getItemInHand().getItemId(), "Furniture_Temple_Light_Brazier")) {
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            if (playerComponent != null) {
                Vector3i bannerPos = placeBlockEvent.getTargetBlock();

                UUID attackerId = playerComponent.getUuid();

                ChunkInfo nearestClaim = SimpleClaimsIntegration.findNearestClaim(
                        playerComponent.getWorld().getName(),
                        bannerPos.getX(),
                        bannerPos.getZ()
                );

                if (nearestClaim == null) {
                    playerComponent.sendMessage(Message.raw("No territory found within range!"));
                    return;
                }

                // Get defender (claim owner)
                UUID defenderId = SimpleClaimsIntegration.getClaimOwner(nearestClaim);
                if (defenderId == null) {
                    playerComponent.sendMessage(Message.raw("Could not find territory owner!"));
                    return;
                }

                // Prevent self-siege
                if (attackerId.equals(defenderId)) {
                    playerComponent.sendMessage(Message.raw("You cannot siege your own territory!"));
                    return;
                }

                // Start the siege using party UUID as territory ID
                UUID territoryId = nearestClaim.getPartyOwner();
                boolean started = TownsSiege.getInstance().getSiegeManager()
                        .startSiege(territoryId, attackerId, defenderId, bannerPos);

                if (started) {
                    playerComponent.sendMessage(Message.raw("Siege started! You are attacking this territory."));
                } else {
                    playerComponent.sendMessage(Message.raw("A siege is already in progress for this territory!"));
                }
            }
        }
    }
}

