package ir.sayandevelopment.listener;

import ir.sayandevelopment.spigot.SpigotMain;
import me.sayandevelopment.sayanchat.api.event.PlayerMentionEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerMentionListener implements Listener {

    @EventHandler
    public void onMention(PlayerMentionEvent event) {
        Player target = event.getMentioned();
        if (SpigotMain.vanishedPlayers.containsKey(target.getUniqueId()) && SpigotMain.vanishedPlayers.get(target.getUniqueId()).isVanished()) {
            event.setCancelled(true);
        }
    }
}
