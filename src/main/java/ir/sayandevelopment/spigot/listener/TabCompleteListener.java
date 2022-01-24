package ir.sayandevelopment.spigot.listener;

import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.command.VanishCommand;
import ir.sayandevelopment.spigot.SpigotMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.Collection;
import java.util.List;

public class TabCompleteListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        List<String> completions = event.getCompletions();
        Collection<VanishedPlayer> vanishedPlayers = SpigotMain.vanishedPlayers.values();
        for (VanishedPlayer vanishedPlayer : vanishedPlayers) {
            if (vanishedPlayer.isVanished()) {
                completions.removeIf(suggestion -> suggestion.equalsIgnoreCase(vanishedPlayer.getUserName()));
            }
        }
    }
}
