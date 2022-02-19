package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VelocityMain;

public class DisconnectListener {

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        VelocityMain.INSTANCE.getServer().getScheduler().buildTask(VelocityMain.INSTANCE, () -> {
            try {
                VelocityMain.SQL.setOnline(player.getUniqueId(), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).schedule();
    }
}
