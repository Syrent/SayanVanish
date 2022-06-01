package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VelocityMain;
import ir.sayandevelopment.bridge.VanishSource;
import ir.sayandevelopment.bridge.VelocityBridge;
import ir.sayandevelopment.command.VanishCommand;

public class ServerConnectedListener {

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        try {
            if (VanishCommand.vanishedPlayers.containsKey(player.getUniqueId())) {
                VelocityBridge.sendVanishedPlayers(player, VanishCommand.vanishedPlayers.get(player.getUniqueId()), VanishSource.SERVER);
            }
            VelocityMain.SQL.setGameMode(player.getUniqueId(), event.getServer().getServerInfo().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
