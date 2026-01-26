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

public final class SiegeStartCommand extends AbstractPlayerCommand {

    private final RequiredArg<PlayerRef> attackerArg;
    private final RequiredArg<PlayerRef> defenderArg;

    public SiegeStartCommand() {
        super("start", "Start a test siege");
        this.attackerArg = withRequiredArg("attacker", "Attacker player", ArgTypes.PLAYER_REF);
        this.defenderArg = withRequiredArg("defender", "Defender player", ArgTypes.PLAYER_REF);
    }

    @Override
    protected void execute(
            @Nonnull CommandContext ctx,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world
    ) {
        PlayerRef attacker = attackerArg.get(ctx);
        PlayerRef defender = defenderArg.get(ctx);

        UUID territoryId = UUID.randomUUID();
        boolean started = TownsSiege.getInstance().getSiegeManager()
                .startSiege(territoryId, attacker.getUuid(), defender.getUuid());

        if (started) {
            ctx.sendMessage(Message.raw("Siege started! Territory: " + territoryId).color(Color.GREEN));
        } else {
            ctx.sendMessage(Message.raw("Failed to start siege").color(Color.RED));
        }
    }
}
