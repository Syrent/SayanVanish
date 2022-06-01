package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VelocityMain;
import ir.sayandevelopment.command.VanishCommand;
import me.sayandevelopment.sayanchat.bridge.proxy.velocity.VelocityPrivateMessageSendEvent;
import me.sayandevelopment.sayanchat.enums.PrivateMessageResult;

import java.util.UUID;

public class PrivateMessageListener {

    @Subscribe
    public void onPrivateMessage(VelocityPrivateMessageSendEvent event) {
        UUID receiverUUID = null;
        for (Player proxiedPlayer : VelocityMain.INSTANCE.getServer().getAllPlayers()) {
            if (proxiedPlayer.getUsername().equals(event.getReceiverName())) {
                receiverUUID = proxiedPlayer.getUniqueId();
            }
        }

        if (receiverUUID == null) return;

        if (event.getResult().equals(PrivateMessageResult.SUCCESSFULL) &&
                VanishCommand.vanishedPlayers.containsKey(receiverUUID)) {
            if (VanishCommand.vanishedPlayers.get(receiverUUID).isVanished()) {
                Player sender = (Player) event.getSender();

                if (sender.hasPermission("sayanvanish.bypass.privatemessage")) return;

                event.setResult(PrivateMessageResult.OFFLINE);
            }
        }
    }
}
