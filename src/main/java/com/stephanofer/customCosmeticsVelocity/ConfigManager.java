package com.stephanofer.customCosmeticsVelocity;

import com.velocitypowered.api.plugin.PluginContainer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public class ConfigManager {


    private static YamlDocument config;
    private final CustomCosmeticsVelocity server;
    private final Logger logger;
    private final String permissionDonor;
    private final String permissionToggle;
    private final String messageRPG;
    private final String messageGLOBAL;
    private final String messageLogoutRPG;
    private final String messageLogoutGLOBAL;

    public ConfigManager(Path dataDirectory, CustomCosmeticsVelocity server) {
        this.server = server;
        this.logger = this.server.getLogger();
        try {
            config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        }catch (IOException e){
            logger.error(e.getMessage());
            Optional<PluginContainer> container = server.getServer().getPluginManager().getPlugin("CustomCosmetics-Velocity");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }


        this.permissionDonor = config.getString(Route.from("permission-donor"));
        this.permissionToggle = config.getString(Route.from("permission-command-usage"));

        this.messageRPG = config.getString(Route.from("messageRPG"));
        this.messageGLOBAL = config.getString(Route.from("messageGLOBAL"));

        this.messageLogoutRPG = config.getString(Route.from("messageLogoutRPG"));
        this.messageLogoutGLOBAL = config.getString(Route.from("messageLogoutGLOBAL"));


    }

    public String getPermissionToggle() {
        return permissionToggle;
    }

    public String getPermissionDonor() {
        return permissionDonor;
    }

    public static YamlDocument getConfig() {
        return config;
    }

    public String getMessageLogoutGLOBAL() {
        return messageLogoutGLOBAL;
    }

    public String getMessageLogoutRPG() {
        return messageLogoutRPG;
    }

    public String getMessageGLOBAL() {
        return messageGLOBAL;
    }

    public String getMessageRPG() {
        return messageRPG;
    }
}
