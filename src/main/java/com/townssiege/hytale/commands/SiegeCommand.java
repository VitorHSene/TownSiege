package com.townssiege.hytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.townssiege.hytale.commands.subcommands.SiegeEndCommand;
import com.townssiege.hytale.commands.subcommands.SiegeInfoCommand;
import com.townssiege.hytale.commands.subcommands.SiegeJoinCommand;
import com.townssiege.hytale.commands.subcommands.SiegeStartCommand;

import javax.annotation.Nonnull;

public final class SiegeCommand extends AbstractPlayerCommand {

    public SiegeCommand() {
        super("siege", "TownsSiege main command");

        addSubCommand(new SiegeStartCommand());
        addSubCommand(new SiegeEndCommand());
        addSubCommand(new SiegeInfoCommand());
        addSubCommand(new SiegeJoinCommand());
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world
    ) {
        ctx.sendMessage(
                Message.raw("Siege commands:")
                        .color("#C1E0FF")
                        .insert("\n/siege start <attacker> <defender>")
                        .insert("\n/siege end <territoryId>")
                        .insert("\n/siege info")
                        .insert("\n/siege join <territoryId> <attacker|defender>")
        );
    }
}
