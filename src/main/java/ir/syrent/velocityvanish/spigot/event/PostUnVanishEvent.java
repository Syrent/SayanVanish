package ir.syrent.velocityvanish.spigot.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PostUnVanishEvent extends Event {

    private Boolean sendJoinMessage;
    private Player player;

    private static final HandlerList HANDLERS = new HandlerList();

    public PostUnVanishEvent(Player player, Boolean sendJoinMessage) {
        this.player = player;
        this.sendJoinMessage = sendJoinMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public Boolean getSendJoinMessage() {
        return sendJoinMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
