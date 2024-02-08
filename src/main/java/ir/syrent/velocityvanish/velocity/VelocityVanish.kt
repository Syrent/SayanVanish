package ir.syrent.velocityvanish.velocity

import com.google.gson.JsonObject
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.messages.ChannelMessageSource
import ir.syrent.velocityvanish.velocity.bridge.VelocityBridge
import ir.syrent.velocityvanish.velocity.bridge.VelocityBridgeManager
import ir.syrent.velocityvanish.velocity.command.ForceVanishCommand
import ir.syrent.velocityvanish.velocity.hooks.VelocitabHook
import ir.syrent.velocityvanish.velocity.listener.ProxyPingListener
import ir.syrent.velocityvanish.velocity.listener.TabCompleteListener
import ir.syrent.velocityvanish.velocity.vruom.VRUoMPlugin
import ir.syrent.velocityvanish.velocity.vruom.VRuom
import ir.syrent.velocityvanish.velocity.vruom.messaging.VelocityMessagingEvent
import net.minecrell.serverlistplus.core.ServerListPlusCore
import net.minecrell.serverlistplus.core.replacement.LiteralPlaceholder
import net.minecrell.serverlistplus.core.replacement.ReplacementManager
import net.minecrell.serverlistplus.core.status.StatusResponse
import net.william278.velocitab.api.VelocitabAPI
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class VelocityVanish @Inject constructor(
    server: ProxyServer,
    logger: Logger,
    @DataDirectory dataDirectory: Path
) : VRUoMPlugin(server, logger, dataDirectory) {

    lateinit var bridgeManager: VelocityBridgeManager
        private set

    /*
    * Note: This is not the best way to do this, but for time being it's fine.
    * TODO: Create a VanishedPlayer object with serializer and deserializer.
    */
    var vanishedPlayers = mutableSetOf<String>()
    fun vanishedPlayersOnline(): List<String> {
        return vanishedPlayers.filter { getServer().getPlayer(it).isPresent }
    }

    @Subscribe
    private fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this

        try {
            VelocitabAPI.getInstance().vanishIntegration = VelocitabHook()
        } catch (_: ClassNotFoundException) { }
        catch (_: NoClassDefFoundError) { }

        initializeMessagingChannels()
        initializeListeners()
        createFolder()
        initializeCommands()

        try {
            Class.forName("net.minecrell.serverlistplus.core.ServerListPlusCore")
            initializeSLPPlaceholders()
            VRuom.log("ServerListPlus found! hook enabled.")
        } catch (_: Exception) {
            VRuom.log("ServerListPlus not found! hook disabled.")
        }
    }

    private fun initializeSLPPlaceholders() {
        ReplacementManager.getDynamic().add(object : LiteralPlaceholder("%velocityvanish_total%") {
            override fun replace(response: StatusResponse, s: String?): String? {
                return run {
                    replace(s, (VRuom.getOnlinePlayers().size - vanishedPlayersOnline().size).toString())
                }
            }

            override fun replace(core: ServerListPlusCore?, s: String?): String? {
                return replace(s, "0")
            }
        })

        for (server in VRuom.getServer().allServers) {
            ReplacementManager.getDynamic().add(object : LiteralPlaceholder("%velocityvanish_${server.serverInfo.name.lowercase()}%") {
                override fun replace(response: StatusResponse, s: String?): String? {
                    return run {
                        replace(s, server.playersConnected.filter { !vanishedPlayersOnline().contains(it.username) }.size.toString())
                    }
                }

                override fun replace(core: ServerListPlusCore?, s: String?): String? {
                    return replace(s, "0")
                }
            })
        }

        ServerListPlusCore.getInstance().reload()
        VRuom.log("ServerListPlus placeholders have been initialized.")
    }

    private fun initializeCommands() {
        ForceVanishCommand(this)
    }

    private fun initializeMessagingChannels() {
        val bridge = VelocityBridge()
        bridgeManager = VelocityBridgeManager(this, bridge)
        object : VelocityMessagingEvent(bridge) {
            override fun onPluginMessageReceived(channelMessageSource: ChannelMessageSource, jsonObject: JsonObject) {
                bridgeManager.handleMessage(jsonObject)
            }
        }

        VRuom.runAsync({
            for (registeredServer in getServer().allServers) {
                if (registeredServer.playersConnected.isNotEmpty()) {
                    bridgeManager.sendVanishedPlayers(registeredServer)
                }
            }
            bridgeManager.sendProxyPlayers()
        }, 0, TimeUnit.SECONDS, 1, TimeUnit.SECONDS)
    }

    private fun initializeListeners() {
        try {
            Class.forName("me.sayandevelopment.sayanchat.proxy.velocity.VelocitySayanChat")
            // TODO: Fix SayanChat kotlin shade
//            PrivateMessageListener(this)
            VRuom.log("SayanChat found! hook enabled.")
        } catch (_: Exception) {
            VRuom.log("SayanChat not found! hook disabled.")
        }
        TabCompleteListener(this)
        ProxyPingListener(this)
    }

    private fun createFolder() {
        val dataFile = getDataDirectory().toFile()
        if (!dataFile.exists()) {
            dataFile.mkdir()
        }
        val noteFile = File(dataFile, "! CONFIG FILES WILL GENERATE ON SPIGOT SERVERS !")
        if (!noteFile.exists()) {
            noteFile.createNewFile()
        }
    }

    companion object {
        lateinit var instance: VelocityVanish
    }

}