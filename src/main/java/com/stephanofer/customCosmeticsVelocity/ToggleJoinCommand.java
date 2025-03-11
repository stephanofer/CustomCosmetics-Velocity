package com.stephanofer.customCosmeticsVelocity;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;

import java.util.UUID;

public class ToggleJoinCommand {

    private final CustomCosmeticsVelocity server;
    private final LuckPerms luckPerms;
    private final ConfigManager configManager;
    private final Utils utils;

    public ToggleJoinCommand(CustomCosmeticsVelocity server) {
        this.server = server;
        this.luckPerms = server.getLuckperms();
        this.configManager = server.getConfigManager();
        this.utils = server.getUtils();
    }


    public BrigadierCommand createBrigadierCommand() {
        LiteralCommandNode<CommandSource> commandNode = BrigadierCommand.literalArgumentBuilder("joinannounce")
                .requires(source -> source.hasPermission(configManager.getPermissionToggle()))
                .executes(context -> {
                    CommandSource source = context.getSource();

                    if (!(source instanceof Player)){
                        source.sendMessage(Component.text("You need to be in a player to use this command!")
                                .color(NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }

                    Player player = (Player) source;
                    UUID playerUUID = player.getUniqueId();

                    luckPerms.getUserManager().loadUser(playerUUID).thenAcceptAsync(user -> {
                        boolean hasDonorPerm = user.getCachedData().getPermissionData().checkPermission(configManager.getPermissionDonor()).asBoolean();

                        Node permissionNode = Node.builder(configManager.getPermissionDonor())
                                .value(!hasDonorPerm)
                                .build();

                        user.data().clear(node -> node.getKey().equals(configManager.getPermissionDonor()));
                        user.data().add(permissionNode);

                        luckPerms.getUserManager().saveUser(user);

//                        String status = !hasDonorPerm ? "Activado" : "Desactivado";

                        player.sendMessage(utils.formatMessage("&r &a¡Éxito! &fAlertas de ingreso al servidor: "+(!hasDonorPerm ? "&aActivado": "&cDesactivado")));

                    });

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        return new BrigadierCommand(commandNode);
    }

}
