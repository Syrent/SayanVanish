package ir.syrent.velocityvanish.spigot

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerOptions
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.comphenix.protocol.wrappers.WrappedServerPing
import com.google.gson.JsonObject
import com.jeff_media.updatechecker.UpdateCheckSource
import com.jeff_media.updatechecker.UpdateChecker
import io.papermc.lib.PaperLib
import ir.syrent.velocityvanish.command.VanishCommand
import ir.syrent.velocityvanish.spigot.bridge.BukkitBridge
import ir.syrent.velocityvanish.spigot.bridge.BukkitBridgeManager
import ir.syrent.velocityvanish.spigot.core.VanishManager
import ir.syrent.velocityvanish.spigot.hook.DependencyManager
import ir.syrent.velocityvanish.spigot.listener.*
import ir.syrent.velocityvanish.spigot.ruom.RUoMPlugin
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityvanish.spigot.ruom.messaging.BukkitMessagingEvent
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.storage.Settings.bstats
import ir.syrent.velocityvanish.spigot.storage.Settings.velocitySupport
import ir.syrent.velocityvanish.spigot.utils.ServerVersion
import ir.syrent.velocityvanish.spigot.utils.Utils
import ir.syrent.velocityvanish.utils.component
import net.minecraft.world.level.block.entity.SignBlockEntity
import org.bstats.bukkit.Metrics
import org.bukkit.entity.Player
import java.util.*


class VelocityVanishSpigot : RUoMPlugin() {

    var bridgeManager: BukkitBridgeManager? = null
    lateinit var vanishManager: VanishManager
        private set

    val proxyPlayers = mutableMapOf<String, List<String>>()
    val vanishedNames = mutableSetOf<String>()
    val vanishedNamesOnline = mutableSetOf<String>()

    override fun onEnable() {
        instance = this
        dataFolder.mkdir()

        initializeInstances()
        resetData(true)
        sendFiglet()
        sendWarningMessages()
        checkUpdate()
        initializeCommands()
        initializeListeners()

        if (velocitySupport) {
            initializePluginChannels()
        }

        if (bstats) {
            enableMetrics()
        }

        DependencyManager.protocolLibHook.protocolManager.addPacketListener(
            object : PacketAdapter(this, ListenerPriority.NORMAL, listOf(PacketType.Status.Server.SERVER_INFO), ListenerOptions.ASYNC) {
                override fun onPacketSending(event: PacketEvent?) {
                    event?.packet?.serverPings?.let { serverPing ->
                        serverPing.read(0).setPlayers(
                            Ruom.getOnlinePlayers().filter { player -> !vanishedNames.contains(player.name) }.map { player ->
                                WrappedGameProfile(player.uniqueId, player.name)
                            }
                        )
                    }
                }
            }
        )
    }

    private fun sendFiglet() {
        sendConsoleMessage("<dark_purple>__      __  _            _ _      __      __         _     _     ")
        sendConsoleMessage("<dark_purple>\\ \\    / / | |          (_) |     \\ \\    / /        (_)   | |    ")
        sendConsoleMessage("<dark_purple> \\ \\  / /__| | ___   ___ _| |_ _   \\ \\  / /_ _ _ __  _ ___| |__  ")
        sendConsoleMessage("<dark_purple>  \\ \\/ / _ \\ |/ _ \\ / __| | __| | | \\ \\/ / _` | '_ \\| / __| '_ \\ ")
        sendConsoleMessage("<dark_purple>   \\  /  __/ | (_) | (__| | |_| |_| |\\  / (_| | | | | \\__ \\ | | |")
        sendConsoleMessage("<dark_purple>    \\/ \\___|_|\\___/ \\___|_|\\__|\\__, | \\/ \\__,_|_| |_|_|___/_| |_|")
        sendConsoleMessage("<dark_purple>                                __/ |                            ")
        sendConsoleMessage("<dark_purple>                               |___/                             v${Ruom.getServer().pluginManager.getPlugin("VelocityVanish")?.description?.version ?: " Unknown"}")
        sendConsoleMessage(" ")
        sendConsoleMessage("<white>Wiki: <blue><u>https://github.com/Syrent/VelocityVanish/wiki</u></blue>")
        sendConsoleMessage(" ")
    }

    private fun sendWarningMessages() {
        if (!ServerVersion.supports(16)) {
            Ruom.warn("Your running your server on a legacy minecraft version (< 16).")
            Ruom.warn("This plugin is not tested on legacy versions, so it may not work properly.")
            Ruom.warn("Please consider updating your server to 1.16.5 or higher.")
        }

        PaperLib.suggestPaper(this)
        DependencyManager
    }

    private fun checkUpdate() {
        Thread {
            try {
                UpdateChecker(this, UpdateCheckSource.SPIGOT, 105992.toString())
                    .setDownloadLink("https://www.spigotmc.org/resources/velocityvanish-1-8-1-19-3-no-database-required.105992/")
                    .checkEveryXHours(24.0)
                    .setChangelogLink(105992.toString())
                    .setNotifyOpsOnJoin(true)
                    .setNotifyByPermissionOnJoin("velocityvanish.updatechecker")
                    .setTimeout(30 * 1000)
                    .setSupportLink("https://discord.gg/xZyYGU4EG4")
                    .checkNow()
            } catch (_: Exception) {
                Ruom.warn("Could not check for updates, check your connection.")
            }
        }.start()
    }

    private fun resetData(startup: Boolean) {
        try {
            for (player in Ruom.getOnlinePlayers()) {
                if (startup) {
                    Utils.sendVanishActionbar(player)
                }
                vanishManager.unVanish(player)
            }
        } catch (_: Exception) {
            Ruom.warn("Plugin didn't fully complete reset data task on plugin shutdown")
        }
    }

    private fun enableMetrics() {
        val pluginID = 16679
        Metrics(this, pluginID)
    }

    private fun initializeInstances() {
        AdventureApi.initialize()
        vanishManager = VanishManager(this)

        Settings
    }

    private fun initializeCommands() {
        VanishCommand(this)
    }

    private fun initializeListeners() {
        PlayerJoinListener(this)
        PlayerQuitListener(this)
        PlayerInteractListener(this)
        PlayerTeleportListener(this)
        PlayerDeathListener(this)
        EntityDamageListener(this)
        PlayerItemPickupListener(this)
        EntityTargetListener(this)
        PlayerChangedWorldListener(this)
        BlockBreakListener(this)
        BlockPlaceListener(this)
        PlayerGameModeChangeListener(this)
        if (DependencyManager.sayanChatHook.exists) {
            PlayerMentionListener(this)
            if (!velocitySupport) PrivateMessageListener(this)
        }
        if (ServerVersion.supports(12)) TabCompleteListener(this)
        if (ServerVersion.supports(19)) BlockReceiveGameListener(this)
        if (DependencyManager.essentialsXHook.exists) {
            AfkStatusChangeListener(this)
            PrivateMessagePreSendListener(this)
        }
    }

    private fun initializePluginChannels() {
        val bridge = BukkitBridge()
        bridgeManager = BukkitBridgeManager(bridge, this)

        object : BukkitMessagingEvent(bridge) {
            override fun onPluginMessageReceived(player: Player, jsonObject: JsonObject) {
                bridgeManager!!.handleMessage(jsonObject)
            }
        }
    }

    override fun onDisable() {
        Ruom.shutdown()

        resetData(false)
    }

    private fun sendConsoleMessage(message: String) {
        AdventureApi.get().sender(server.consoleSender).sendMessage(message.component())
    }

    companion object {
        lateinit var instance: VelocityVanishSpigot
    }

}