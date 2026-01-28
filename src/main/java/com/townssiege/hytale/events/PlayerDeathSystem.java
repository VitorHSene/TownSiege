package com.townssiege.hytale.events;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.TownsSiege;
import com.townssiege.hytale.notification.SiegeMessages;
import com.townssiege.models.Siege;
import com.townssiege.models.Team;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerDeathSystem extends DeathSystems.OnDeathSystem {

    private static final int KILL_POINTS = 100;

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onComponentAdded(@Nonnull Ref ref, @Nonnull DeathComponent component, @Nonnull Store store, @Nonnull CommandBuffer commandBuffer) {
        PlayerRef victim = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
        if (victim == null) {
            return;
        }

        PlayerRef killer = getKiller(component, store);
        if (killer == null) {
            return;
        }

        handleSiegeKill(killer.getUuid(), victim.getUuid());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private PlayerRef getKiller(DeathComponent component, Store store) {
        Damage.Source source = component.getDeathInfo().getSource();

        if (!(source instanceof Damage.EntitySource)) {
            return null;
        }

        Damage.EntitySource entitySource = (Damage.EntitySource) source;
        Ref<EntityStore> killerRef = entitySource.getRef();

        return (PlayerRef) store.getComponent(killerRef, PlayerRef.getComponentType());
    }

    private void handleSiegeKill(UUID killerId, UUID victimId) {
        Siege siege = TownsSiege.getInstance().getSiegeManager().getSiegeByPlayer(killerId);
        if (siege == null) {
            return;
        }

        if (!siege.isAtSiege(victimId)) {
            return;
        }

        if (siege.isSameTeam(killerId, victimId)) {
            return;
        }

        siege.addPoints(killerId, KILL_POINTS);

        // Notify the killer
        Team team = siege.getPlayerTeam(killerId);
        Universe universe = Universe.get();
        if (team != null && universe != null) {
            PlayerRef killer = universe.getPlayer(killerId);
            if (killer != null) {
                killer.sendMessage(SiegeMessages.pointsEarnedKill(KILL_POINTS, team.getPoints(), "an enemy"));
            }
        }
    }
}