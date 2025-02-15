package com.stephanofer.customCosmeticsVelocity;

import com.google.inject.Inject;
import com.jakub.jpremium.proxy.api.App;
import com.jakub.jpremium.proxy.api.event.velocity.UserEvent;
import com.jakub.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.slf4j.Logger;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private static CustomCosmeticsVelocity instance;
    private final ProxyServer server;
    public static Logger logger;
    private LuckPerms luckperms;
    private App jpremium;

    private final Map<UUID, PlayerPrefixData> prefixCache = new ConcurrentHashMap<>();

    @Inject
    public CustomCosmeticsVelocity(ProxyServer server, Logger logger){
            instance = this;
            this.server = server;
            CustomCosmeticsVelocity.logger = logger;
    }


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.luckperms = LuckPermsProvider.get();
        this.jpremium = JPremiumVelocity.getApplication();

        if (this.luckperms != null && this.jpremium != null)  {
            logger.info("CustomCosmetics Iniciado correctamente");
        }

    }

    @Subscribe
    public void onAuthSuccess(UserEvent.Login event){
        UUID uuidPlayer = event.getUserProfile().getUniqueId();
        Player player = server.getPlayer(uuidPlayer).orElse(null);

        if(player == null) return;
        if(!(player.hasPermission("heranetwork.donor"))) return;

        String displayName = player.getUsername();

        CompletableFuture.supplyAsync(() -> {

            QueryOptions queryOptions = QueryOptions.contextual(ImmutableContextSet.of("server", "rpg"));

            User lpUser = luckperms.getUserManager().getUser(uuidPlayer);
            if (lpUser == null) return new PlayerPrefixData("", "");

            String prefixGlobal = Optional.ofNullable(lpUser.getCachedData()
                            .getMetaData()
                            .getPrefix())
                    .orElse("");

            String prefixRpg = Optional.ofNullable(lpUser.getCachedData()
                            .getMetaData(queryOptions)
                            .getPrefix())
                    .orElse("");

            PlayerPrefixData prefixes = new PlayerPrefixData(prefixGlobal, prefixRpg);
            prefixCache.put(uuidPlayer, prefixes);

            return prefixes;
        }).thenAccept( (PlayerPrefixData prefixes )-> {

            String prefixGlobal = prefixes.prefixGlobal ;
            String prefixRpg = prefixes.prefixRpg;
            String messageGlobal = "&r &bSwoooosh "+prefixGlobal+"&f"+displayName+" &bha aterrizado!" ;
            String messageRpg = "&r &bSwoooosh "+prefixRpg+"&f"+displayName+" &bha aterrizado!" ;

            Component componentGlobal =  messageFormat(messageGlobal);
            Component componentRpg =  messageFormat(messageRpg);

            server.getScheduler()
                    .buildTask(this, () -> sendCustomMessage(componentGlobal, componentRpg))
                    .delay(500, TimeUnit.MILLISECONDS)
                    .schedule();

        }).exceptionally(throwable -> {
            logger.error("Error al procesar el mensaje de conexión para " + player.getUsername(), throwable);
            return null;
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event){
        Player player = event.getPlayer();
        UUID uuidPlayer = player.getUniqueId();
        String displayName = player.getUsername();

        if(!(player.hasPermission("heranetwork.donor"))) return;

        String prefixGlobal = prefixCache.get(uuidPlayer).prefixGlobal;
        String prefixRpg = prefixCache.get(uuidPlayer).prefixRpg;

        String messageGlobal = "&r &c¡El jugador "+prefixGlobal+"&f"+displayName+" &cha salido del servidor!" ;
        String messageRpg = "&r &c¡El jugador "+prefixRpg+"&f"+displayName+" &cha salido del servidor!" ;
        Component componentGlobal = messageFormat(messageGlobal);
        Component componentRpg =  messageFormat(messageRpg);

        sendCustomMessage(componentGlobal, componentRpg);

//        for (Player playerConnected : instance.server.getAllServers()
//                .stream()
//                .flatMap(server -> server.getPlayersConnected().stream())
//                .collect(Collectors.toList())){
//            logger.info("Enviando mensaje a: " + playerConnected.getUsername());
//            playerConnected.sendMessage(message);
//        }
        if (prefixCache.containsKey(uuidPlayer)) {
            prefixCache.remove(uuidPlayer);
        }
    }


    public Component messageFormat(String message){
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public void sendCustomMessage(Component messageGlobal, Component messageRpg){
        server.getAllPlayers().forEach(p -> {
            p.getCurrentServer().ifPresent(serverConnection -> {
                String serverName = serverConnection.getServerInfo().getName();

                if (serverName.equalsIgnoreCase("rpg")) {
                    p.sendMessage(messageRpg);
                } else {
                    p.sendMessage(messageGlobal);
                }
            });
        });
    }
    }

