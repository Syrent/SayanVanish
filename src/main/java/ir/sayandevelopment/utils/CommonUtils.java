package ir.sayandevelopment.utils;

import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VelocityMain;
import ir.sayandevelopment.command.VanishCommand;

import java.util.Collection;
import java.util.stream.Collectors;

public class CommonUtils {
    public static boolean isPlayerVanished(Player player) {
        try {
            return VelocityMain.SQL.getVanishedPlayers(player.getUniqueId()).isVanished();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Collection<Player> getNonVanishedPlayers() {
        return VelocityMain.INSTANCE.getServer().getAllPlayers().stream().filter(defaultPlayer ->
                !VanishCommand.vanishedPlayers.containsKey(defaultPlayer.getUniqueId())
                        || !VanishCommand.vanishedPlayers.get(defaultPlayer.getUniqueId()).isVanished()
        ).collect(Collectors.toList());
    }

    public static Collection<Player> getNonVanishedPlayers(Collection<Player> players) {
        return players.stream().filter(defaultPlayer ->
                !VanishCommand.vanishedPlayers.containsKey(defaultPlayer.getUniqueId())
                        || !VanishCommand.vanishedPlayers.get(defaultPlayer.getUniqueId()).isVanished()
        ).collect(Collectors.toList());
    }
}
