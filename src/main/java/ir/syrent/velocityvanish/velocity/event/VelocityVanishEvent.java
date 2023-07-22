package ir.syrent.velocityvanish.velocity.event;

import com.velocitypowered.api.proxy.Player;

import java.util.Optional;

public class VelocityVanishEvent {

    private final Player player;
    private final String playerName;

    public VelocityVanishEvent(Player player, String playerName) {
        this.player = player;
        this.playerName = playerName;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

}
