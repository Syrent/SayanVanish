package ir.syrent.velocityvanish.spigot.core

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.event.PostUnVanishEvent
import ir.syrent.velocityvanish.spigot.event.PostVanishEvent
import ir.syrent.velocityvanish.spigot.event.PreUnVanishEvent
import ir.syrent.velocityvanish.spigot.event.PreVanishEvent
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.utils.ServerVersion
import ir.syrent.velocityvanish.spigot.utils.Utils
import org.bukkit.entity.Creature
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team


class VanishManager(
    private val plugin: VelocityVanishSpigot
) {

    private val potions = mutableSetOf(
        PotionEffect(PotionEffectType.NIGHT_VISION, Int.MAX_VALUE, 255, false, false),
        PotionEffect(PotionEffectType.FIRE_RESISTANCE, Int.MAX_VALUE, 255, false, false),
    )

    init {
        if (ServerVersion.supports(13)) potions.add(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 255, false, false))
    }

    fun updateTabState(player: Player, state: NativeGameMode) {
        if (Ruom.hasPlugin("ProtocolLib")) {
            /*
            * Players can't receive packets from plugin on join
            * So we need to send packet after 1 tick
            * (2tick incase)
            */
            Ruom.runSync({
                val tabPacket = plugin.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO, true)
                val infoData = tabPacket?.playerInfoDataLists
                val infoAction = tabPacket?.playerInfoAction
                val playerInfo = infoData?.read(0)

                playerInfo?.add(
                    PlayerInfoData(
                        WrappedGameProfile.fromPlayer(player),
                        0,
                        state,
                        WrappedChatComponent.fromText(player.playerListName)
                    )
                )

                infoData?.write(0, playerInfo)
                infoAction?.write(0, EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE)

                val newTabPacket = PacketContainer(PacketType.Play.Server.PLAYER_INFO, tabPacket.handle)

                for (onlinePlayer in Ruom.getOnlinePlayers()) {
                    if (onlinePlayer.hasPermission("velocityvanish.admin.seevanished")) {
                        if (onlinePlayer == player) continue
                        VelocityVanishSpigot.instance.protocolManager.sendServerPacket(onlinePlayer, newTabPacket)
                    }
                }
            }, 2)
        } else {
            hidePlayer(player)
        }
    }

    fun hidePlayer(player: Player) {
        for (onlinePlayer in Ruom.getOnlinePlayers().filter { !it.hasPermission("velocityvanish.admin.seevanished") }) {
            onlinePlayer.hidePlayer(Ruom.getPlugin(), player)
        }
    }

    private fun setMeta(player: Player, meta: Boolean) {
        player.setMetadata("vanished", FixedMetadataValue(Ruom.getPlugin(), meta))
    }

    private fun addPotionEffects(player: Player) {
        for (potionEffect in potions) {
            player.addPotionEffect(potionEffect)
        }
    }

    private fun removePotionEffects(player: Player) {
        for (potionEffect in potions) {
            player.removePotionEffect(potionEffect.type)
        }
    }

    fun denyPush(player: Player) {
        var team = player.scoreboard.getTeam("Vanished")
        if (team == null) {
            team = player.scoreboard.registerNewTeam("Vanished")
        }
        team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER)
        team.addEntry(player.name)
    }

    fun allowPush(player: Player) {
        player.scoreboard.getTeam("Vanished")?.removeEntry(player.name)
    }

    fun vanish(player: Player) {
        vanish(player, true)
    }

    fun vanish(player: Player, sendQuitMessage: Boolean) {
        val preVanishEvent = PreVanishEvent(player, sendQuitMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(preVanishEvent)

        if (preVanishEvent.isCancelled) return

        val postVanishEvent = PostVanishEvent(player, preVanishEvent.sendQuitMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(postVanishEvent)

        setMeta(player, true)

        updateTabState(player, NativeGameMode.SPECTATOR)
        hidePlayer(player)

        player.allowFlight = true

        player.isSleepingIgnored = true

        try {
            player.isCollidable = false
        } catch (e: NoClassDefFoundError) {
            try {
                @Suppress("DEPRECATION")
                player.spigot().collidesWithEntities = false
            } catch (e1: NoClassDefFoundError) {
                e.printStackTrace()
            } catch (e1: NoSuchMethodError) {
                e.printStackTrace()
            }
        } catch (e: NoSuchMethodError) {
            try {
                @Suppress("DEPRECATION")
                player.spigot().collidesWithEntities = false
            } catch (e1: NoClassDefFoundError) {
                e.printStackTrace()
            } catch (e1: NoSuchMethodError) {
                e.printStackTrace()
            }
        }

        player.world.entities.stream()
            .filter { entity -> entity is Creature }
            .map { entity -> entity as Creature }
            .filter { mob -> mob.target != null }
            .filter { mob -> player.uniqueId == mob.target?.uniqueId }
            .forEach { mob -> mob.target = null }

        addPotionEffects(player)

        denyPush(player)
    }

    fun unVanish(player: Player) {
        unVanish(player, true)
    }

    fun unVanish(player: Player, sendJoinMessage: Boolean) {
        val preUnVanishEvent = PreUnVanishEvent(player, sendJoinMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(preUnVanishEvent)

        if (preUnVanishEvent.isCancelled) return

        val postUnVanishEvent = PostUnVanishEvent(player, preUnVanishEvent.sendJoinMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(postUnVanishEvent)

        setMeta(player, false)

        updateTabState(player, NativeGameMode.SURVIVAL)
        for (onlinePlayer in Ruom.getOnlinePlayers()) onlinePlayer.showPlayer(Ruom.getPlugin(), player)

        player.isSleepingIgnored = false

        try {
            player.isCollidable = true
        } catch (e: NoClassDefFoundError) {
            try {
                @Suppress("DEPRECATION")
                player.spigot().collidesWithEntities = true
            } catch (e1: NoClassDefFoundError) {
                e.printStackTrace()
            } catch (e1: NoSuchMethodError) {
                e.printStackTrace()
            }
        } catch (e: NoSuchMethodError) {
            try {
                @Suppress("DEPRECATION")
                player.spigot().collidesWithEntities = true
            } catch (e1: NoClassDefFoundError) {
                e.printStackTrace()
            } catch (e1: NoSuchMethodError) {
                e.printStackTrace()
            }
        }

        removePotionEffects(player)

        Utils.actionbarPlayers.remove(player)

        allowPush(player)
    }

}