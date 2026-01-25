package com.townssiege;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.townssiege.commands.SiegeCommand;

import java.util.logging.Level;

public class TownsSiege extends JavaPlugin {

    private static TownsSiege instance;

    public TownsSiege(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();

        this.getLogger().at(Level.INFO).log("Initializing TownsSiege");

        getCommandRegistry().registerCommand(new SiegeCommand());
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    public static TownsSiege getInstance() {
        return instance;
    }
}
