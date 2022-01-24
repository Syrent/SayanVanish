package ir.sayandevelopment.spigot.listener;

import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.bridge.VanishSource;
import ir.sayandevelopment.spigot.SpigotMain;
import ir.sayandevelopment.spigot.bridge.BukkitBridgeListener;
import me.mohamad82.ruom.Ruom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Ruom.runAsync(() -> {
            VanishedPlayer vanishedPlayer;
            try {
                vanishedPlayer = SpigotMain.SQL.getVanishedPlayer(player.getUniqueId());
                Ruom.runSync(() -> {
                    if (vanishedPlayer != null && vanishedPlayer.isVanished()) {
                        vanishedPlayer.setVanishSource(VanishSource.SERVER);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }
}
