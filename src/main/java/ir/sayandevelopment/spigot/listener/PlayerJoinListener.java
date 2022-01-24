package ir.sayandevelopment.spigot.listener;

import ir.sayandevelopment.VanishManager;
import me.mohamad82.ruom.Ruom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!player.hasPermission("sayanvanish.seevanished")) {
            for (Player player1 : Ruom.getOnlinePlayers()) {
                Ruom.runAsync(() -> {
                    if (VanishManager.getInstance().isInvisible(player1))
                        Ruom.runSync(() -> player.hidePlayer(Ruom.getPlugin(), player1));
                });
            }
        }

        Ruom.runAsync(() -> {
            if (VanishManager.getInstance().isInvisible(player)) {
                    Ruom.runSync(() -> {
                        VanishManager.getInstance().vanishPlayer(player);
                        VanishManager.getInstance().sendVanishMessageToOthers(player.getName());
                    });
            }
        });
    }
}
