package ir.syrent.velocityvanish.spigot.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreVanishEvent extends Event implements Cancellable {

    private Boolean cancelled = false;
    private Boolean sendQuitMessage;
    public Player player;

    private static final HandlerList HANDLERS = new HandlerList();

    public PreVanishEvent(Player player, Boolean sendQuitMessage) {
        this.player = player;
        this.sendQuitMessage = sendQuitMessage;
    }

    public Boolean getSendQuitMessage() {
        return sendQuitMessage;
    }

    public void setSendQuitMessage(Boolean sendQuitMessage) {
        this.sendQuitMessage = sendQuitMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public Player getPlayer() {
        return player;
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
