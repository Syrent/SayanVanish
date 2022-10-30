package ir.syrent.velocityvanish.spigot.ruom.messaging;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public abstract class BukkitMessagingEvent {

    private final BukkitMessagingChannel channel;

    public BukkitMessagingEvent(BukkitMessagingChannel channel) {
        this.channel = channel;
        channel.register(this);
    }

    protected abstract void onPluginMessageReceived(Player player, JsonObject message);

    public void unregister() {
        channel.unregister(this);
    }

    public BukkitMessagingChannel getChannel() {
        return channel;
    }

}
