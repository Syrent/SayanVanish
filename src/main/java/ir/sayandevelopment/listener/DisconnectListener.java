package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.VelocityMain;

public class DisconnectListener {

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        try {
            VelocityMain.SQL.setOnline(player.getUniqueId().toString(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
