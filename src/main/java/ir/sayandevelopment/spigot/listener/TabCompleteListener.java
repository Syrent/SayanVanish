package ir.sayandevelopment.spigot.listener;

import ir.sayandevelopment.spigot.SpigotMain;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.List;

public class TabCompleteListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        List<String> completions = event.getCompletions();
        for (String username : SpigotMain.vanishedPlayers) {
            completions.removeIf(suggestion -> suggestion.equalsIgnoreCase(username));
        }
    }
}
