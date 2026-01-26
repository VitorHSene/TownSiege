package com.townssiege;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.townssiege.commands.SiegeCommand;
import com.townssiege.models.SiegeManager;

import java.util.logging.Level;

public class TownsSiege extends JavaPlugin {

    private static TownsSiege instance;

    private SiegeManager siegeManager;

    public TownsSiege(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    private static final long HOUR = 3600000;

    @Override
    protected void setup() {
        super.setup();

        this.getLogger().at(Level.INFO).log("Initializing TownsSiege");
        this.siegeManager = new SiegeManager(3 * HOUR, 1 * HOUR, 24 * HOUR);

        getCommandRegistry().registerCommand(new SiegeCommand());
    }

    @Override
    protected void shutdown() {
        super.shutdown();
    }

    public static TownsSiege getInstance() {
        return instance;
    }

    public SiegeManager getSiegeManager() {
        return siegeManager;
    }
}
