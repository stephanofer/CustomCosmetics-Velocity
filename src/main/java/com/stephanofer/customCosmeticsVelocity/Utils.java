package com.stephanofer.customCosmeticsVelocity;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;

public class Utils {

    private final CustomCosmeticsVelocity server;
    private final Logger logger;
    private final ConfigManager configManager;

    public Utils(CustomCosmeticsVelocity server) {
        this.server = server;
        this.logger = server.getLogger();
        this.configManager=  server.getConfigManager();
    }

    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer
            .builder()
            .character('&')
            .hexColors()
            .build();

    public boolean hasPermission(User player) {
        return player.getCachedData().getPermissionData().checkPermission(configManager.getPermissionDonor()).asBoolean();
    }

    public Component formatMessage(String message) {
        return legacySerializer.deserialize(message);
    }

    public void customSendMessage(Component messageRPG, Component mesageGlobal) {

        for (Player player : server.getServer().getAllPlayers()) {
            player.getCurrentServer().ifPresent(serverConnection -> {

                String serverName = serverConnection.getServerInfo().getName();

                if (serverName.equalsIgnoreCase("rpg")){
//                    logger.info("Send message to " + player.getUsername());
                    player.sendMessage(messageRPG);
                } else {
//                    logger.info("Send message to " + player.getUsername());
                    player.sendMessage(mesageGlobal);
                }
            });
        }
    }

    public String replacePlaceholder(String message, String prefix, String userName){
        return message.replace("%prefix%", prefix).replace("%player%", userName);
    }

}
