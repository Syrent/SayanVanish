package ir.sayandevelopment.listener;

import ir.sayandevelopment.spigot.SpigotMain;
import me.mohamad82.ruom.utils.StringUtils;
import me.sayandevelopment.sayanchat.api.event.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AsyncPlayerChatListener implements Listener {

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        if (SpigotMain.vanishedPlayers.contains(player.getName())) {
            if (!message.startsWith("-")) {
                event.setCancelled(true);
                player.sendMessage(StringUtils.colorize("&cYou are in vanish mode. If you want want to chat please make sure to start your message with \"-\""));
            } else {
                event.setMessage(message.substring(1));
            }
        }
    }
}
