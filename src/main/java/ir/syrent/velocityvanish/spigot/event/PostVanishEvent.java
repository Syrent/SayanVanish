package ir.syrent.velocityvanish.spigot.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PostVanishEvent extends Event {

    private Boolean sendQuitMessage;
    private Player player;

    private static final HandlerList HANDLERS = new HandlerList();

    public PostVanishEvent(Player player, Boolean sendQuitMessage) {
        this.player = player;
        this.sendQuitMessage = sendQuitMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public Boolean getSendQuitMessage() {
        return sendQuitMessage;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
