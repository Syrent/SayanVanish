package ir.sayandevelopment.spigot.bridge;

import com.google.gson.JsonObject;
import ir.sayandevelopment.VanishManager;
import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.bridge.VanishSource;
import me.mohamad82.ruom.utils.GsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BukkitBridgeListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player client, byte[] bytes) {
        String rawVanishedPlayer = new String(bytes, StandardCharsets.UTF_8);
        String vanishedPlayerJsonString = rawVanishedPlayer.substring(2);

        JsonObject vanishedPlayerJson = GsonUtils.getParser().parse(vanishedPlayerJsonString).getAsJsonObject();
        VanishSource vanishSource = VanishSource.valueOf(vanishedPlayerJson.get("source").getAsString());
        String userName = vanishedPlayerJson.get("username").getAsString();
        String gameMode = vanishedPlayerJson.get("gamemode").getAsString();
        UUID uuid = UUID.fromString(vanishedPlayerJson.get("uuid").getAsString());
        boolean vanished = vanishedPlayerJson.get("vanished").getAsBoolean();

        VanishedPlayer vanishedPlayer = new VanishedPlayer(userName, gameMode, uuid, vanished);
        vanishedPlayer.setVanishSource(vanishSource);

        if (vanishSource.equals(VanishSource.COMMAND)) {
            Player player = Bukkit.getPlayer(userName);

            if (player != null) {
                if (vanished) {
                    VanishManager.getInstance().sendLeaveMessage(player.getName());
                    VanishManager.getInstance().vanishPlayer(player);
                    VanishManager.getInstance().sendVanishMessageToOthers(player.getName());
                } else {
                    VanishManager.getInstance().sendJoinMessage(player.getName());
                    VanishManager.getInstance().unvanishPlayer(player);
                    VanishManager.getInstance().sendUnVanishMessageToOthers(player.getName());
                }
            }
        }
    }
}
