package com.townssiege;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.townssiege.hytale.commands.SiegeCommand;
import com.townssiege.hytale.events.BannerTickSystem;
import com.townssiege.hytale.events.PlaceBannerEvent;
import com.townssiege.hytale.events.PlayerDeathSystem;
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

    // TODO: Change back to HOUR for production
    private static final long MINUTE = 60000;

    @Override
    protected void setup() {
        super.setup();
        this.getEntityStoreRegistry().registerSystem(new PlaceBannerEvent());
        this.getEntityStoreRegistry().registerSystem(new BannerTickSystem());
        this.getEntityStoreRegistry().registerSystem(new PlayerDeathSystem());
        this.getLogger().at(Level.INFO).log("Initializing TownsSiege");
        this.siegeManager = new SiegeManager(3 * MINUTE, 1 * MINUTE, 24 * MINUTE);
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
