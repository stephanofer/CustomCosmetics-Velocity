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
import net.luckperms.api.model.user.User;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<UUID, String> prefixCache = new ConcurrentHashMap<>();

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

//        logger.info("EVENTO JOIN PARA "+ player.getUsername());
        String displayName = player.getUsername();


        CompletableFuture.supplyAsync(() -> {
            User lpUser = luckperms.getUserManager().getUser(uuidPlayer);
            if (lpUser == null) return "";

            String prefix = lpUser.getCachedData()
                    .getMetaData()
                    .getPrefix();

//            logger.info("1 PREFFIX "+ prefix);

            prefixCache.put(player.getUniqueId(), prefix != null ? prefix : "");

            return prefix != null ? prefix : "";
        }).thenAccept(prefix -> {
//            logger.info("Preffix para el jugador "+ prefix);
            String message = "&r &bSwoooosh "+prefix+"&f"+displayName+" &bha aterrizado!" ;
            Component component =  LegacyComponentSerializer.legacyAmpersand().deserialize(message);

            server.getAllPlayers().forEach(p -> {
//                logger.info("Enviado mensaje a " + p.getUsername());
                p.sendMessage(component);
            });
        }).exceptionally(throwable -> {
            logger.error("Error al procesar el mensaje de conexión para " + player.getUsername(), throwable);
            return null;
        });
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event){
        Player player = event.getPlayer();
        String displayName = player.getUsername();

        if(!(player.hasPermission("heranetwork.donor"))) return;
//        logger.info("EVENTO DISCONNECT PARA "+ player.getUsername());

        String prefix = prefixCache.getOrDefault(player.getUniqueId(), "");
//        logger.info("Preffix para el jugador "+ prefix);

        String message = "&r &c¡El jugador "+prefix+"&f"+displayName+" &cha salido del servidor!" ;
        Component component =  LegacyComponentSerializer.legacyAmpersand().deserialize(message);

        server.getAllPlayers().forEach(p -> {
//            logger.info("Enviado mensaje a " + p.getUsername());
            p.sendMessage(component);
        });


//        for (Player playerConnected : instance.server.getAllServers()
//                .stream()
//                .flatMap(server -> server.getPlayersConnected().stream())
//                .collect(Collectors.toList())){
//            logger.info("Enviando mensaje a: " + playerConnected.getUsername());
//            playerConnected.sendMessage(message);
//        }
        prefixCache.remove(player.getUniqueId());


    }
}
