package com.townssiege.hytale.commands.subcommands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.TownsSiege;
import com.townssiege.models.Siege;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Map;
import java.util.UUID;

public final class SiegeInfoCommand extends AbstractPlayerCommand {

    public SiegeInfoCommand() {
        super("info", "Show active sieges info");
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world
    ) {
        Map<UUID, Siege> sieges = TownsSiege.getInstance().getSiegeManager().getActiveSieges();

        if (sieges.isEmpty()) {
            ctx.sendMessage(Message.raw("No active sieges").color(Color.YELLOW));
            return;
        }

        ctx.sendMessage(Message.raw("Active Sieges:").color("#C1E0FF"));
        for (Map.Entry<UUID, Siege> entry : sieges.entrySet()) {
            UUID territoryId = entry.getKey();
            Siege siege = entry.getValue();

            String status = siege.isBannerActive() ? "BANNER ACTIVE" : "Banner inactive";
            long remainingMs = siege.getRemainingSiegeTime();
            String remaining = formatTime(remainingMs);

            ctx.sendMessage(Message.raw("")
                    .insert("\n  Territory: " + territoryId.toString().substring(0, 8) + "...")
                    .insert("\n  Status: " + status)
                    .insert("\n  Attackers: " + siege.getAttackers().getPlayers().size())
                    .insert("\n  Defenders: " + siege.getDefenders().getPlayers().size())
                    .insert("\n  Attacker Points: " + siege.getAttackers().getPoints())
                    .insert("\n  Defender Points: " + siege.getDefenders().getPoints())
                    .insert("\n  Time Left: " + remaining)
            );
        }
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
    }
}
