package ir.sayandevelopment.listener;

import ir.sayandevelopment.spigot.SpigotMain;
import me.sayandevelopment.sayanchat.api.event.PrivateMessageReceiveEvent;
import me.sayandevelopment.sayanchat.enums.PrivateMessageResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PrivateMessageListener implements Listener {

    @EventHandler
    public void onPriavateMessage(PrivateMessageReceiveEvent event) {
        Player player = Bukkit.getPlayerExact(event.getSender());
        if (event.getResult().equals(PrivateMessageResult.SUCCESSFULL) &&
                SpigotMain.vanishedPlayers.contains(event.getReceiver().getName())) {
            if (player != null && player.hasPermission("sayanvanish.bypass.privatemessage")) return;
            event.setResult(PrivateMessageResult.OFFLINE);
        }
    }
}
