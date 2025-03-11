package com.stephanofer.customCosmeticsVelocity;

import com.google.inject.Inject;
import com.jakub.jpremium.proxy.api.App;
import com.jakub.jpremium.proxy.api.JPremiumApi;
import com.stephanofer.customCosmeticsVelocity.Events.PlayerDisconnect;
import com.stephanofer.customCosmeticsVelocity.Events.PlayerLoginSuccessful;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Plugin(
        id = "customcosmetics-velocity",
        name = "CustomCosmetics-Velocity",
        version = BuildConstants.VERSION,
        url = "https://stephanofer.com/",
        authors = {"stephanofer"},
        dependencies = {
                @Dependency(id = "luckperms"),
                @Dependency(id = "jpremium")
        }
)

public class CustomCosmeticsVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private LuckPerms luckperms;
    private App jpremium;
    private CacheManager cacheManager;
    private Utils utils;
    private final Path dataDirectory;
    private ConfigManager configManager;

    @Inject
    public CustomCosmeticsVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
            this.server = server;
            this.logger = logger;
            this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        try {
            luckperms = LuckPermsProvider.get();
            logger.info("LuckPerms loaded successfully");
        } catch (IllegalStateException e) {
            logger.error("LuckPerms is not installed, please install it first");
            return;
        }

        try {
            jpremium = JPremiumApi.getApp();
            if (jpremium == null) {
                logger.error("Failed to get JPremium application instance");
                return;
            }
            logger.info("JPremium loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading JPremium: " + e.getMessage());
            return;
        }

        cacheManager = new CacheManager();
        configManager = new ConfigManager(dataDirectory,this);
        utils = new Utils(this);

        ToggleJoinCommand toggleJoinCommand = new ToggleJoinCommand(this);
        BrigadierCommand brigadierCommand = toggleJoinCommand.createBrigadierCommand();
        CommandManager commandManager = server.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder(brigadierCommand)
                        .plugin(this)
                        .build();

        commandManager.register(commandMeta, brigadierCommand);

        logger.info("Comando /joinannounce ha sido registrado correctamente");





//        server.getEventManager().register(this, new PlayerEventListener(this));
        server.getEventManager().register(this, new PlayerDisconnect(this));
        server.getEventManager().register(this, new PlayerLoginSuccessful(this));
        logger.info("CustomCosmetics initialization completed successfully");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public LuckPerms getLuckperms() {
        return luckperms;
    }

    public App getJpremium() {
        return jpremium;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Utils getUtils() {
        return utils;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

