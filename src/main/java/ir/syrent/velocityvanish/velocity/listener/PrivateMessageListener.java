package ir.syrent.velocityvanish.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import ir.syrent.velocityvanish.velocity.VelocityVanish;
import me.mohamad82.ruom.VRuom;
import me.sayandevelopment.sayanchat.bridge.proxy.velocity.VelocityPrivateMessageSendEvent;
import me.sayandevelopment.sayanchat.enums.PrivateMessageResult;

public class PrivateMessageListener {

    VelocityVanish plugin;

    public PrivateMessageListener(VelocityVanish plugin) {
        this.plugin = plugin;
        VRuom.registerListener(this);
    }

    @Subscribe
    private void onPrivateMessage(VelocityPrivateMessageSendEvent event) {
        for (Player player : VRuom.getServer().getAllPlayers()) {
            if (player.getUsername().equals(event.getReceiverName())) {
                if (event.getResult().equals(PrivateMessageResult.SUCCESSFULL) && plugin.getVanishedPlayers().contains(event.getReceiverName())) {
                    Player sender = (Player) event.getSender();
                    if (sender.hasPermission("velocityvanish.admin.privatemessage")) return;

                    event.setResult(PrivateMessageResult.OFFLINE);
                }
            }
        }
    }


}
