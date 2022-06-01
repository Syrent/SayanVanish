package ir.sayandevelopment.spigot.dependency;

import ir.sayandevelopment.VanishManager;
import ir.sayandevelopment.spigot.SpigotMain;
import ir.sayandevelopment.spigot.bridge.BukkitBridgeListener;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.mohamad82.ruom.Ruom;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlaceholderAPI extends PlaceholderExpansion {
    @Override
    public String getAuthor() {
        return "Syrent231";
    }

    @Override
    public String getIdentifier() {
        return "sayanvanish";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        int defaultPlayers = 0;
        try {
            defaultPlayers = Ruom.getOnlinePlayers().size() - SpigotMain.SQL.vanishedCount();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (params.equalsIgnoreCase("players")) {
            return String.valueOf(defaultPlayers);
        } else {
            try {
                return String.valueOf(Integer.parseInt(me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(
                        null, "%bungee_" + params + "%")) -
                        (params.equalsIgnoreCase("total") ? SpigotMain.SQL.vanishedCount() : SpigotMain.SQL.vanishedCount(params)));
            } catch (Exception e) {
                e.printStackTrace();
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(null, "%bungee_" + params + "%");
            }
        }

        //return "0"; // Placeholder is unknown by the Expansion
    }
}
