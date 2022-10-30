package ir.syrent.velocityvanish.spigot.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PostUnVanishEvent extends Event {

    private Player player;

    private static final HandlerList HANDLERS = new HandlerList();

    public PostUnVanishEvent(Player player) {
        this.player = player;
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
