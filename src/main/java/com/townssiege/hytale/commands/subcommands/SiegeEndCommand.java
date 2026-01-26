package com.townssiege.hytale.commands.subcommands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.TownsSiege;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.UUID;

public final class SiegeEndCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> territoryArg;

    public SiegeEndCommand() {
        super("end", "End a siege");
        this.territoryArg = withRequiredArg("territory", "Territory UUID", ArgTypes.STRING);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world
    ) {
        String territoryStr = territoryArg.get(ctx);

        try {
            UUID territoryId = UUID.fromString(territoryStr);
            boolean ended = TownsSiege.getInstance().getSiegeManager().endSiege(territoryId);

            if (ended) {
                ctx.sendMessage(Message.raw("Siege ended!").color(Color.GREEN));
            } else {
                ctx.sendMessage(Message.raw("No siege found for that territory").color(Color.RED));
            }
        } catch (IllegalArgumentException e) {
            ctx.sendMessage(Message.raw("Invalid UUID format").color(Color.RED));
        }
    }
}
