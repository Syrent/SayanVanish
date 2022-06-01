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

        for (Player onlinePlayer : Ruom.getOnlinePlayers()) {
            if (player == onlinePlayer) continue;
            if (VanishManager.getInstance().isInvisible(onlinePlayer)) {
                VanishManager.getInstance().vanishPlayer(onlinePlayer);
            }
        }

        if (VanishManager.getInstance().isInvisible(player)) {
            VanishManager.getInstance().vanishPlayer(player);
            VanishManager.getInstance().sendVanishMessageToOthers(player.getName());
        }
    }
}
