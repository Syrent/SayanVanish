package ir.syrent.velocityvanish.spigot.ruom.event.packet

import io.netty.channel.*
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.utils.NMSUtils
import me.sayandevelopment.sayanchat.ruom.event.packet.ChatPreviewEvent
import me.sayandevelopment.sayanchat.ruom.event.packet.PlayerActionEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class PacketListenerManager : Listener {
    private val packetEvents: MutableSet<PacketEvent> = HashSet<PacketEvent>()
    private val actionEvents: MutableSet<PlayerActionEvent> = HashSet()
    private val chatPreviewEvents: MutableSet<ChatPreviewEvent> = HashSet()
    fun register(packetEvent: PacketEvent) {
        packetEvents.add(packetEvent)
    }

    fun register(actionEvent: PlayerActionEvent) {
        actionEvents.add(actionEvent)
    }

    fun register(chatPreviewEvent: ChatPreviewEvent) {
        chatPreviewEvents.add(chatPreviewEvent)
    }

    fun unregister(packetEvent: PacketEvent) {
        packetEvents.remove(packetEvent)
    }

    fun unregister(actionEvent: PlayerActionEvent) {
        actionEvents.remove(actionEvent)
    }
    fun unregister(chatPreviewEvent: ChatPreviewEvent) {
        chatPreviewEvents.remove(chatPreviewEvent)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onJoin(event: PlayerJoinEvent) {
        injectPlayer(event.player)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onQuit(event: PlayerQuitEvent) {
        removePlayer(event.player)
    }

    private fun injectPlayer(player: Player) {
        val channelDuplexHandler: ChannelDuplexHandler = object : ChannelDuplexHandler() {
            override fun channelRead(context: ChannelHandlerContext, packet: Any) {
                try {
                    val packetContainer = PacketContainer(packet)
                    var isCancelled = false
                    for (packetEvent in packetEvents) {
                        try {
                            isCancelled = !packetEvent.onServerboundPacket(player, packetContainer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Ruom.error(
                                "An error occured while handling (reading) a packet. Please report this error to the plugin's author(s): " +
                                        Ruom.plugin.getDescription().getAuthors()
                            )
                        }
                    }
                } catch (ignored: IllegalArgumentException) {
                }
            }

            override fun write(context: ChannelHandlerContext, packet: Any, channelPromise: ChannelPromise) {
                try {
                    val packetContainer = PacketContainer(packet)
                    var isCancelled = false
                    for (packetEvent in packetEvents) {
                        try {
                            isCancelled = !packetEvent.onClientboundPacket(player, packetContainer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Ruom.error(
                                "An error occured while handling (writing) a packet. Please report this error to the plugin's author(s): " +
                                        Ruom.plugin.getDescription().getAuthors()
                            )
                        }
                    }
                    if (!isCancelled) {
                        try {
                            super.write(context, packet, channelPromise)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (ignored: IllegalArgumentException) {
                }
            }
        }
        try {
            val pipeline: ChannelPipeline = NMSUtils.getChannel(player)!!.pipeline()
            pipeline.addBefore(
                "packet_handler",
                java.lang.String.format("%s_%s", Ruom.plugin.getDescription().getName(), player.name),
                channelDuplexHandler
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removePlayer(player: Player) {
        try {
            val channel: Channel = NMSUtils.getChannel(player)!!
            channel.eventLoop().submit<Any?> {
                channel.pipeline().remove(
                    java.lang.String.format(
                        "%s_%s",
                        Ruom.plugin.getDescription().getName(),
                        player.name
                    )
                )
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private var INSTANCE: PacketListenerManager? = null
        val instance: PacketListenerManager?
            get() {
                if (INSTANCE == null) initialize()
                return INSTANCE
            }

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = PacketListenerManager()
                Ruom.onlinePlayers.forEach { player: Player ->
                    INSTANCE!!.injectPlayer(
                        player
                    )
                }
                Ruom.registerListener(INSTANCE)
            }
        }

        fun shutdown() {
            if (INSTANCE != null) {
                Ruom.unregisterListener(INSTANCE)
                Ruom.onlinePlayers.forEach { player: Player ->
                    INSTANCE!!.removePlayer(
                        player
                    )
                }
                INSTANCE = null
            }
        }
    }
}