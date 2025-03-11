package com.stephanofer.customCosmeticsVelocity.Events;

import com.stephanofer.customCosmeticsVelocity.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.UUID;

public class PlayerDisconnect {

    private final CustomCosmeticsVelocity server;
    private final CacheManager cacheManager;
    private final Logger logger;
    private final Utils utils;
    private final ConfigManager configManager;

    public PlayerDisconnect(CustomCosmeticsVelocity server) {
        this.server = server;
        this.cacheManager = server.getCacheManager();
        this.logger = server.getLogger();
        this.utils = server.getUtils();
        this.configManager = server.getConfigManager();
    }


    @Subscribe

    public void onPlayerDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(!cacheManager.hasPlayerPrefixInCache(uuid)) {
            return;
        }

        PlayerPrefixData playerPrefixData = cacheManager.getPlayerPrefix(uuid);


        String messageRPG = utils.replacePlaceholder(configManager.getMessageLogoutRPG(), playerPrefixData.getPrefixRpg(), player.getUsername());
        String messageGLOBAL = utils.replacePlaceholder(configManager.getMessageLogoutGLOBAL(), playerPrefixData.getPrefixRpg(), player.getUsername());



        Component finalMessageRPG = utils.formatMessage(messageRPG);
        Component finalMessageGLOBAL = utils.formatMessage(messageGLOBAL);

        utils.customSendMessage(finalMessageRPG, finalMessageGLOBAL);

        cacheManager.invalidatePlayerPrefix(uuid);
//        logger.info( " (" + uuid + ") se ha elminado de la cache.");

    }
}
