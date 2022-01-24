package ir.sayandevelopment.bridge;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import ir.sayandevelopment.VanishedPlayer;
import ir.sayandevelopment.VelocityMain;

@SuppressWarnings("UnstableApiUsage")
public class VelocityBridge {

    public static void sendVanishedPlayers(Player player, VanishedPlayer vanishedPlayer, VanishSource source) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
        JsonObject vanishedPlayerJson = new JsonObject();
        vanishedPlayerJson.addProperty("source", source.name());
        vanishedPlayerJson.addProperty("username", vanishedPlayer.getUserName());
        vanishedPlayerJson.addProperty("gamemode", vanishedPlayer.getGameMode());
        vanishedPlayerJson.addProperty("uuid", vanishedPlayer.getUuid().toString());
        vanishedPlayerJson.addProperty("vanished", vanishedPlayer.isVanished());
        byteArrayDataOutput.writeUTF(VelocityMain.GSON.toJson(vanishedPlayerJson));

        player.getCurrentServer().get().sendPluginMessage(VelocityMain.SAYANVANISH_CHANNEL, byteArrayDataOutput.toByteArray());
    }
}
