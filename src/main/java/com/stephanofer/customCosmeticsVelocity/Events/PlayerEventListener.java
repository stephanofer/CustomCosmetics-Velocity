package com.stephanofer.customCosmeticsVelocity.Events;

import com.stephanofer.customCosmeticsVelocity.CacheManager;
import com.stephanofer.customCosmeticsVelocity.CustomCosmeticsVelocity;
import com.stephanofer.customCosmeticsVelocity.PlayerPrefixData;
import com.stephanofer.customCosmeticsVelocity.Utils;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryMode;
import net.luckperms.api.query.QueryOptions;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;

    public class PlayerEventListener {

        private final CustomCosmeticsVelocity server;
        private final Logger logger;
        private final LuckPerms luckperms;
        private final CacheManager cacheManager;
        private final Utils utils;

        private static final ImmutableContextSet CONTEXT_RPG = ImmutableContextSet.of(DefaultContextKeys.SERVER_KEY, "rpg");
        private static final ImmutableContextSet CONTEXT_LOBBY = ImmutableContextSet.of(DefaultContextKeys.SERVER_KEY, "lobby");
        public static final QueryOptions QUERY_RPG = QueryOptions.builder(QueryMode.CONTEXTUAL).context(CONTEXT_RPG).build();
        public static final QueryOptions QUERY_LOBBY = QueryOptions.builder(QueryMode.CONTEXTUAL).context(CONTEXT_LOBBY).build();

        public PlayerEventListener(CustomCosmeticsVelocity server) {
            this.server = server;
            this.logger = server.getLogger();
            this.luckperms = server.getLuckperms();
            this.cacheManager = server.getCacheManager();
            this.utils = server.getUtils();
        }

        @Subscribe
        public void onPlayerJoin(LoginEvent event) {
            Player player = event.getPlayer();
            CompletableFuture.runAsync(() -> {
                User user = luckperms.getPlayerAdapter(Player.class).getUser(player);

                if (!utils.hasPermission(user)) {
                    return;
                }

                String prefixRPG = user.getCachedData().getMetaData(QUERY_RPG).getPrefix();
                String prefixLobby = user.getCachedData().getMetaData(QUERY_LOBBY).getPrefix() ;

//                logger.info("RPG: "+prefixRPG);
//                logger.info("PROXY: "+prefixLobby);

                cacheManager.updatePlayerPrefix(
                        player.getUniqueId(),
                        new PlayerPrefixData(
                                prefixRPG != null ? prefixRPG : "",
                                prefixLobby != null ? prefixLobby : ""
                        )
                );
            });
    }
}