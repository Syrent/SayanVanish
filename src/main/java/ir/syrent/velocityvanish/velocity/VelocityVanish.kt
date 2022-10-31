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
import ir.syrent.velocityvanish.velocity.listener.PrivateMessageListener
import ir.syrent.velocityvanish.velocity.listener.TabCompleteListener
import me.mohamad82.ruom.VRUoMPlugin
import me.mohamad82.ruom.VRuom
import me.mohamad82.ruom.messaging.VelocityMessagingEvent
import org.slf4j.Logger
import java.io.File
import java.lang.Exception
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class VelocityVanish @Inject constructor(
    server: ProxyServer,
    logger: Logger,
    @DataDirectory dataDirectory: Path
) : VRUoMPlugin(server, logger) {

    lateinit var bridgeManager: VelocityBridgeManager
        private set
    val dataDirectory: Path

    var vanishedPlayers = mutableSetOf<String>()

    init {
        this.dataDirectory = dataDirectory
    }

    @Subscribe
    private fun onProxyInitialization(event: ProxyInitializeEvent) {
        instance = this

        initializeMessagingChannels()
        initializeListeners()
        createFolder()
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
                    for (player in registeredServer.playersConnected) {
                        bridgeManager.sendVanishedPlayers(registeredServer)
                    }
                }
            }
        }, 0, TimeUnit.SECONDS, 1, TimeUnit.SECONDS)
    }

    private fun initializeListeners() {
        try {
            Class.forName("me.sayandevelopment.sayanchat.SayanChat")
            PrivateMessageListener(this)
            VRuom.log("SayanChat found! hook enabled.")
        } catch (_: Exception) {
            VRuom.log("SayanChat not found! hook disabled.")
        }
        TabCompleteListener(this)
    }

    private fun createFolder() {
        val dataFile = dataDirectory.toFile()
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
            private set
    }

}