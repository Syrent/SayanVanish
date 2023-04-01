package ir.syrent.velocityvanish.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import ir.syrent.velocityvanish.velocity.VelocityVanish;
import ir.syrent.velocityvanish.velocity.vruom.VRuom;

public class PrivateMessageListener {

    VelocityVanish plugin;

    public PrivateMessageListener(VelocityVanish plugin) {
        this.plugin = plugin;
        VRuom.registerListener(this);
    }
}