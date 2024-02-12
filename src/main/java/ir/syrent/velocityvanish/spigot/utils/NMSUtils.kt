package ir.syrent.velocityvanish.spigot.utils

import com.cryptomorin.xseries.ReflectionUtils
import io.netty.channel.Channel
import ir.syrent.nms.accessors.ConnectionAccessor
import ir.syrent.nms.accessors.ServerCommonPacketListenerImplAccessor
import ir.syrent.nms.accessors.ServerGamePacketListenerImplAccessor
import ir.syrent.nms.accessors.ServerPlayerAccessor
import org.bukkit.entity.Player
import java.lang.reflect.Method


object NMSUtils {

    private var CRAFT_PLAYER: Class<*>? = null

    private var CRAFT_PLAYER_GET_HANDLE_METHOD: Method? = null

    init {
        try {
            CRAFT_PLAYER = ReflectionUtils.getCraftClass("entity.CraftPlayer")
            CRAFT_PLAYER_GET_HANDLE_METHOD = CRAFT_PLAYER!!.getMethod("getHandle")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getServerPlayer(player: Player?): Any? {
        return try {
            CRAFT_PLAYER_GET_HANDLE_METHOD!!.invoke(player)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getServerGamePacketListener(player: Player?): Any? {
        return try {
            ServerPlayerAccessor.getFieldConnection()[getServerPlayer(player)]
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sendPacket(player: Player, vararg packets: Any) {
        try {
            val connection = getServerGamePacketListener(player)
            for (packet in packets) {
                if (ServerVersion.supports(20) && ServerVersion.getPatchNumber() >= 2) {
                    ServerCommonPacketListenerImplAccessor.getMethodSend1().invoke(connection, packet)
                } else {
                    ServerGamePacketListenerImplAccessor.getMethodSend1().invoke(connection, packet)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Error(e)
        }
    }

    fun getConnection(player: Player): Any? {
        try {
            return ServerGamePacketListenerImplAccessor.getFieldConnection().get(getServerGamePacketListener(player))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getChannel(player: Player): Channel? {
        try {
            return ConnectionAccessor.getFieldChannel().get(getConnection(player)!!) as Channel
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}