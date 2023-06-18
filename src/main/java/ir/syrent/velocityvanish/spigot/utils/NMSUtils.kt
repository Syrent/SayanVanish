package ir.syrent.velocityvanish.spigot.utils

import com.cryptomorin.xseries.ReflectionUtils
import com.mojang.authlib.GameProfile
import ir.syrent.nms.accessors.ServerGamePacketListenerImplAccessor
import ir.syrent.nms.accessors.ServerPlayerAccessor
import me.sayandevelopment.sayanchat.ruom.nmsaccessors.ServerPlayerGameModeAccessor
import org.bukkit.World
import org.bukkit.entity.Player
import java.lang.reflect.Method
import java.util.*


object NMSUtils {

    private var CRAFT_PLAYER: Class<*>? = null

    private var CRAFT_PLAYER_GET_HANDLE_METHOD: Method? = null
    private var CRAFT_WORLD_GET_HANDLE_METHOD: Method? = null

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
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sendPacket(player: Player?, vararg packets: Any?) {
        try {
            val connection = getServerGamePacketListener(player)
            for (packet in packets) {
                ServerGamePacketListenerImplAccessor.getMethodSend1().invoke(connection, packet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw Error(e)
        }
    }

    fun getServerLevel(world: World): Any? {
        return try {
            CRAFT_WORLD_GET_HANDLE_METHOD!!.invoke(world)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createServerPlayerObject(name: String, world: World, skin: Optional<MinecraftSkin>): Any? {
        return try {
            val serverLevel: Any = getServerLevel(world)
            val profile = GameProfile(UUID.randomUUID(), name)
            val entity: Any
            entity = ServerPlayerAccessor.getConstructor2().newInstance(
                    NMSUtils.getDedicatedServer(),
                    serverLevel,
                    profile,
                    null
                )
            if (skin.isPresent()) {
                skin.get().apply(entity)
            }
            entity
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }
}