package ir.sayandevelopment.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.VelocityMain;
import ir.sayandevelopment.bridge.VanishSource;
import ir.sayandevelopment.bridge.VelocityBridge;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class VanishCommand implements SimpleCommand {

    public static Map<UUID, VanishedPlayer> vanishedPlayers = new HashMap<>();

    @Override
    public void execute(Invocation invocation) {
        Player player = (Player) invocation.source();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        if (!player.hasPermission("sayanvanish.command.vanish")) {
            player.sendMessage(miniMessage.deserialize(VelocityMain.PREFIX + "<color:#ff6a00>You don't have permission to use this command."));
            return;
        }

        VanishedPlayer vanishedPlayer = new VanishedPlayer(player.getUsername(),
                player.getCurrentServer().get().getServerInfo().getName(), player.getUniqueId(), true);
        vanishedPlayer.setVanished(!vanishedPlayers.containsKey(player.getUniqueId()) || !vanishedPlayers.get(player.getUniqueId()).isVanished());
        vanishedPlayer.setOnline(true);
        vanishedPlayers.put(player.getUniqueId(), vanishedPlayer);


        VelocityBridge.sendVanishedPlayers(player, vanishedPlayer, VanishSource.COMMAND);
        try {
            VelocityMain.SQL.updateVanishedPlayer(vanishedPlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (vanishedPlayer.isVanished()) {
            player.sendMessage(miniMessage.deserialize(VelocityMain.PREFIX + "<color:#63ff00>Successfully vanished!"));
        } else {
            player.sendMessage(miniMessage.deserialize(VelocityMain.PREFIX + "<color:#63ff00>Successfully unvanished!"));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }
}
