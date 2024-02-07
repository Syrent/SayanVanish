package ir.syrent.velocityvanish.velocity.bridge

import com.google.common.io.ByteStreams
import com.google.gson.JsonObject
import com.velocitypowered.api.proxy.server.RegisteredServer
import ir.syrent.velocityvanish.velocity.VelocityVanish
import ir.syrent.velocityvanish.velocity.event.VelocityUnVanishEvent
import ir.syrent.velocityvanish.velocity.event.VelocityVanishEvent
import ir.syrent.velocityvanish.velocity.vruom.VRuom
import ir.syrent.velocityvanish.velocity.vruom.utils.GsonUtils
import net.william278.velocitab.api.VelocitabAPI

@Suppress("UnstableApiUsage")
class VelocityBridgeManager(
    private val plugin: VelocityVanish,
    private val bridge: VelocityBridge
) {

    fun sendVanishedPlayers(server: RegisteredServer) {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "Vanish")
        val vanishedPlayersJson = GsonUtils.get().toJsonTree(plugin.vanishedPlayers).asJsonArray
        val vanishedPlayersOnlineJson = GsonUtils.get().toJsonTree(plugin.vanishedPlayersOnline()).asJsonArray
        messageJson.add("vanished_players", vanishedPlayersJson)
        messageJson.add("vanished_players_online", vanishedPlayersOnlineJson)

        sendPluginMessage(messageJson, server)
    }

    fun sendProxyPlayers() {
        val messageJson = JsonObject()
        messageJson.addProperty("type", "Players")

        val jsonObject = JsonObject()
        for (registeredServer in VRuom.getServer().allServers) {
            jsonObject.add(registeredServer.serverInfo.name.lowercase(), GsonUtils.get().toJsonTree(registeredServer.playersConnected.map { it.username }).asJsonArray)
            messageJson.add("servers", jsonObject)

            sendPluginMessage(messageJson)
        }
    }

    private fun sendPluginMessage(messageJson: JsonObject) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray())
    }

    private fun sendPluginMessage(messageJson: JsonObject, server: RegisteredServer) {
        val byteArrayInputStream = ByteStreams.newDataOutput()
        byteArrayInputStream.writeUTF(GsonUtils.get().toJson(messageJson))

        bridge.sendPluginMessage(byteArrayInputStream.toByteArray(), server)
    }

    fun handleMessage(messageJson: JsonObject) {
        when (messageJson["type"].asString) {
            "Vanish" -> {
                val name = messageJson["name"].asString
                val vanished = messageJson["vanished"].asBoolean

                if (vanished) {
                    plugin.vanishedPlayers.add(name)
                    val player = VRuom.getPlayer(name).orElse(null)
                    if (player != null) {
                        try {
                            VelocitabAPI.getInstance().vanishPlayer(player)
                            VelocitabAPI.getInstance().tabList.updateDisplayNames()
                        } catch (_: NoClassDefFoundError) {}
                    }
                    VRuom.getServer().eventManager.fire(VelocityVanishEvent(player, name))
                } else {
                    plugin.vanishedPlayers.remove(name)
                    val player = VRuom.getPlayer(name).orElse(null)
                    if (player != null) {
                        try {
                            VelocitabAPI.getInstance().unVanishPlayer(player)
                        } catch (_: NoClassDefFoundError) {}
                    }
                    VRuom.getServer().eventManager.fire(VelocityUnVanishEvent(player, name))
                }
            }
            else -> {
                VRuom.warn("Unsupported message type: ${messageJson["type"].asString}")
            }
        }
    }
}