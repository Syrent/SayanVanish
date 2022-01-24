package ir.sayandevelopment.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.command.VanishCommand;

import java.util.Collection;
import java.util.List;

public class TabCompleteListener {

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        List<String> suggestions = event.getSuggestions();
        Collection<VanishedPlayer> vanishedPlayers = VanishCommand.vanishedPlayers.values();
        for (VanishedPlayer vanishedPlayer : vanishedPlayers) {
            if (vanishedPlayer.isVanished()) {
                suggestions.removeIf(suggestion -> suggestion.equalsIgnoreCase(vanishedPlayer.getUserName()));
            }
        }
    }
}
