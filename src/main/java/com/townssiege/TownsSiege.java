package com.townssiege;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.townssiege.hytale.commands.SiegeCommand;
import com.townssiege.hytale.events.PlaceBannerEvent;
import com.townssiege.hytale.notification.SiegeNotifier;

import java.util.logging.Level;

public class TownsSiege extends JavaPlugin {

    private static TownsSiege instance;

    private SiegeManager siegeManager;
    private SiegeNotifier siegeNotifier;

    public TownsSiege(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    private static final long HOUR = 3600000;

    @Override
    protected void setup() {
        super.setup();
        this.getEntityStoreRegistry().registerSystem(new PlaceBannerEvent());
        this.getLogger().at(Level.INFO).log("Initializing TownsSiege");
        this.siegeManager = new SiegeManager(3 * HOUR, 1 * HOUR, 24 * HOUR);
        this.siegeNotifier = new SiegeNotifier(siegeManager);
        this.siegeNotifier.start();

        getCommandRegistry().registerCommand(new SiegeCommand());
    }

    @Override
    protected void shutdown() {
        if (siegeNotifier != null) {
            siegeNotifier.stop();
        }
        super.shutdown();
    }

    public static TownsSiege getInstance() {
        return instance;
    }

    public SiegeManager getSiegeManager() {
        return siegeManager;
    }
}
