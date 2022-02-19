package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import ir.sayandevelopment.VelocityMain;

public class ServerConnectedListener {

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        try {
            VelocityMain.SQL.setGameMode(event.getPlayer().getUniqueId(), event.getServer().getServerInfo().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
