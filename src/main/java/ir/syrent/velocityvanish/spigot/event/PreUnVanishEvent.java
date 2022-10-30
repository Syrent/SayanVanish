package ir.syrent.velocityvanish.spigot.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PreUnVanishEvent extends Event implements Cancellable {

    private Boolean cancelled = false;
    private Boolean sendJoinMessage;
    public Player player;

    private static final HandlerList HANDLERS = new HandlerList();

    public PreUnVanishEvent(Player player, Boolean sendJoinMessage) {
        this.player = player;
        this.sendJoinMessage = sendJoinMessage;
    }

    public Boolean getSendJoinMessage() {
        return sendJoinMessage;
    }

    public void setSendJoinMessage(Boolean sendJoinMessage) {
        this.sendJoinMessage = sendJoinMessage;
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
