package com.townssiege.hytale.events;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

        if(Objects.equals(placeBlockEvent.getItemInHand().getItemId(),"Furniture_Temple_Light_Brazier")){
            Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
            Player playerComponent = store.getComponent(ref, Player.getComponentType());

            if(playerComponent!=null){
                playerComponent.sendMessage(Message.raw("You placed a Siege Banner."));
            }


        }



    }
}

