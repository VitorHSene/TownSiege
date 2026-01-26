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
import com.townssiege.models.Siege;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.UUID;

public final class SiegeJoinCommand extends AbstractPlayerCommand {

    private final RequiredArg<String> territoryArg;
    private final RequiredArg<String> teamArg;

    public SiegeJoinCommand() {
        super("join", "Join a siege");
        this.territoryArg = withRequiredArg("territory", "Territory UUID", ArgTypes.STRING);
        this.teamArg = withRequiredArg("team", "Team (attacker/defender)", ArgTypes.STRING);
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
        String team = teamArg.get(ctx).toLowerCase();

        UUID territoryId;
        try {
            territoryId = UUID.fromString(territoryStr);
        } catch (IllegalArgumentException e) {
            ctx.sendMessage(Message.raw("Invalid UUID format").color(Color.RED));
            return;
        }

        Siege siege = TownsSiege.getInstance().getSiegeManager().getSiege(territoryId);
        if (siege == null) {
            ctx.sendMessage(Message.raw("No siege found for that territory").color(Color.RED));
            return;
        }

        UUID playerId = player.getUuid();
        if (siege.isAtSiege(playerId)) {
            ctx.sendMessage(Message.raw("You are already in this siege").color(Color.RED));
            return;
        }

        switch (team) {
            case "attacker":
            case "attack":
            case "a":
                siege.addAttacker(playerId);
                ctx.sendMessage(Message.raw("Joined as ATTACKER!").color(Color.GREEN));
                break;
            case "defender":
            case "defend":
            case "d":
                siege.addDefender(playerId);
                ctx.sendMessage(Message.raw("Joined as DEFENDER!").color(Color.GREEN));
                break;
            default:
                ctx.sendMessage(Message.raw("Invalid team. Use 'attacker' or 'defender'").color(Color.RED));
        }
    }
}
