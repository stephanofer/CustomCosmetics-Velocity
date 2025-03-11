package com.stephanofer.customCosmeticsVelocity.Events;

import com.jakub.jpremium.proxy.api.event.velocity.UserEvent;
import com.stephanofer.customCosmeticsVelocity.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerLoginSuccessful {

    private final CustomCosmeticsVelocity server;
    private final Logger logger;
    private final LuckPerms luckperms;
    private final CacheManager cacheManager;
    private final Utils utils;
    private final ConfigManager configManager;

    private static final ImmutableContextSet CONTEXT_RPG = ImmutableContextSet.of(DefaultContextKeys.SERVER_KEY, "rpg");
    private static final ImmutableContextSet CONTEXT_LOBBY = ImmutableContextSet.of(DefaultContextKeys.SERVER_KEY, "lobby");
    public static final QueryOptions QUERY_RPG = QueryOptions.builder(QueryMode.CONTEXTUAL).context(CONTEXT_RPG).build();
    public static final QueryOptions QUERY_LOBBY = QueryOptions.builder(QueryMode.CONTEXTUAL).context(CONTEXT_LOBBY).build();

    public PlayerLoginSuccessful(CustomCosmeticsVelocity server) {
        this.server = server;
        this.logger = server.getLogger();
        this.luckperms = server.getLuckperms();
        this.cacheManager = server.getCacheManager();
        this.configManager = server.getConfigManager();
        this.utils = server.getUtils();
    }

    @Subscribe
    public void onAuthSuccess(UserEvent.Login event) {
        UUID userId = event.getUserProfile().getUniqueId();
//        logger.info("User " + userId + " logged in");

        server.getServer().getPlayer(userId).ifPresent(player -> {
            CompletableFuture.supplyAsync(() -> {
                User user = luckperms.getPlayerAdapter(Player.class).getUser(player);

                if (!utils.hasPermission(user)) {
                    return null;
                }

                String prefixRPG = user.getCachedData().getMetaData(QUERY_RPG).getPrefix();
                String prefixLobby = user.getCachedData().getMetaData(QUERY_LOBBY).getPrefix();

                PlayerPrefixData prefixData = new PlayerPrefixData(
                        prefixRPG != null ? prefixRPG : "",
                        prefixLobby != null ? prefixLobby : ""
                );

                cacheManager.updatePlayerPrefix(player.getUniqueId(), prefixData);

                return prefixData;
            }).thenAccept(prefixData -> {
                if (prefixData == null) {
                    return;
                }

                String messageRPG = utils.replacePlaceholder(configManager.getMessageRPG(), prefixData.getPrefixRpg(), player.getUsername());
                String messageGLOBAL = utils.replacePlaceholder(configManager.getMessageGLOBAL(), prefixData.getPrefixGlobal(), player.getUsername());


//                logger.info("RPG: " + prefixData.getPrefixRpg());
//                logger.info("PROXY: " + prefixData.getPrefixGlobal());

                Component finalMessageRPG = utils.formatMessage(messageRPG);
                Component finalMessageGLOBAL = utils.formatMessage(messageGLOBAL);

                server.getServer().getScheduler()
                        .buildTask(server, () -> utils.customSendMessage(finalMessageRPG, finalMessageGLOBAL))
                        .delay(2, TimeUnit.SECONDS)
                        .schedule();

            }).exceptionally(ex -> {
                logger.error("Error procesando login del jugador " + player.getUsername(), ex);
                return null;
            });
        });
    }
}