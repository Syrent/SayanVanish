package ir.syrent.velocityvanish.velocity.event;

import com.velocitypowered.api.proxy.Player;

import java.util.Optional;

public class VelocityUnVanishEvent {

    private final Player player;
    private final String playerName;

    public VelocityUnVanishEvent(Player player, String playerName) {
        this.player = player;
        this.playerName = playerName;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

}
