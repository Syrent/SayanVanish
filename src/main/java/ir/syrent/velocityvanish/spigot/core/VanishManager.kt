package ir.syrent.velocityvanish.spigot.core

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.*
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import ir.syrent.nms.accessors.*
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.event.PostUnVanishEvent
import ir.syrent.velocityvanish.spigot.event.PostVanishEvent
import ir.syrent.velocityvanish.spigot.event.PreUnVanishEvent
import ir.syrent.velocityvanish.spigot.event.PreVanishEvent
import ir.syrent.velocityvanish.spigot.hook.DependencyManager
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.NMSUtils
import ir.syrent.velocityvanish.spigot.utils.ServerVersion
import ir.syrent.velocityvanish.spigot.utils.Utils
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.level.ServerPlayer
import org.bukkit.GameMode
import org.bukkit.entity.Creature
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.Team
import java.util.*


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

    val flyPlayers = mutableSetOf<UUID>()
    val invulnerablePlayers = mutableSetOf<UUID>()

    fun updateTabState(player: Player, state: GameMode) {
        if (DependencyManager.protocolLibHook.exists && Settings.seeAsSpectator) {
            /*
            * Players can't receive packets from plugin on join
            * So we need to send packet after 1 tick
            * (2tick incase)
            */
            Ruom.runSync({
                try {
                    val tabPacket = DependencyManager.protocolLibHook.protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO, true)
                    val infoData = tabPacket?.playerInfoDataLists
                    val infoAction = tabPacket?.playerInfoAction

                    val playerInfo = infoData?.read(0)

                    playerInfo?.add(
                        PlayerInfoData(
                            WrappedGameProfile.fromPlayer(player),
                            0,
                            NativeGameMode.valueOf(state.name),
                            WrappedChatComponent.fromText(player.playerListName)
                        )
                    )

                    infoData?.write(0, playerInfo)
                    infoAction?.write(0, EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE)

                    val newTabPacket = PacketContainer(PacketType.Play.Server.PLAYER_INFO, tabPacket?.handle)

                    for (onlinePlayer in Ruom.getOnlinePlayers().filter { it.hasPermission("velocityvanish.admin.seevanished") }.filter { it != player }) {
                        DependencyManager.protocolLibHook.protocolManager.sendServerPacket(onlinePlayer, newTabPacket)
                    }
                } catch (e: Exception) {
                    val removePacket = ClientboundPlayerInfoRemovePacketAccessor.getConstructor0().newInstance(
                        listOf(EntityAccessor.getMethodGetUUID1().invoke(NMSUtils.getServerPlayer(player))),
                    )

                    val addPacket = ClientboundPlayerInfoUpdatePacketAccessor.getMethodCreatePlayerInitializing1().invoke(
                        null,
                        listOf(NMSUtils.getServerPlayer(player))
                    )

                    val updateGamemodePacket = ClientboundPlayerInfoUpdatePacketAccessor.getConstructor0().newInstance(
                        ClientboundPlayerInfoUpdatePacket_i_ActionAccessor.getFieldUPDATE_GAME_MODE(),
                        NMSUtils.getServerPlayer(player)
                    )
                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE


//                    val updateGamemodePacket = ClientboundPlayerInfoUpdatePacket_i_ActionAccessor.getFieldWriter().

                    for (onlinePlayer in Ruom.getOnlinePlayers().filter { it.hasPermission("velocityvanish.admin.seevanished") }.filter { it != player }) {
                        NMSUtils.sendPacket(onlinePlayer, updateGamemodePacket)
                    }

                    for (onlinePlayer in Ruom.getOnlinePlayers().filter { !it.hasPermission("velocityvanish.admin.seevanished") }.filter { it != player }) {
                        NMSUtils.sendPacket(onlinePlayer, removePacket)
                    }
                }
            }, 2)
        } else {
            hidePlayer(player)
        }
    }

    fun hidePlayer(player: Player) {
        val vanishLevel = player.effectivePermissions.map { it.permission }
            .filter { it.startsWith("velocityvanish.level.") }.maxOfOrNull { it.split(".")[2].toInt() } ?: 0

        for (onlinePlayer in Ruom.getOnlinePlayers().filter { !it.hasPermission("velocityvanish.admin.seevanished") }) {

            val onlinePlayerVanishLevel = onlinePlayer.effectivePermissions.map { it.permission }
                .filter { it.startsWith("velocityvanish.level.") }.maxOfOrNull { it.split(".")[2].toInt() } ?: 0

            if (onlinePlayerVanishLevel > vanishLevel) continue

            @Suppress("DEPRECATION")
            onlinePlayer.hidePlayer(player)
        }
    }

    private fun setMeta(player: Player, meta: Boolean) {
        player.setMetadata("vanished", FixedMetadataValue(Ruom.getPlugin(), meta))
    }

    fun addPotionEffects(player: Player) {
        Ruom.runSync({
            for (potionEffect in potions) {
                try {
                    val mobEffect = MobEffectInstanceAccessor.getConstructor0().newInstance(
                        MobEffectAccessor.getMethodById1().invoke(null, potionEffect.type.id),
                        Int.MAX_VALUE,
                        potionEffect.amplifier,
                        potionEffect.isAmbient,
                        potionEffect.hasParticles(),
                        potionEffect.hasIcon()
                    )
                    NMSUtils.sendPacket(player, ClientboundUpdateMobEffectPacketAccessor.getConstructor0().newInstance(player.entityId, mobEffect))
                } catch (e: Exception) {
                    player.addPotionEffect(potionEffect)
                }
            }
        }, 2)
    }

    fun removePotionEffects(player: Player) {
        Ruom.runSync({
            for (potionEffect in potions) {
                try {
                    NMSUtils.sendPacket(player, ClientboundRemoveMobEffectPacketAccessor.getConstructor0().newInstance(player.entityId, MobEffectAccessor.getMethodById1().invoke(null, potionEffect.type.id)))
                } catch (e: Exception) {
                    player.removePotionEffect(potionEffect.type)
                }
            }
        }, 2)
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

    fun vanish(player: Player, sendQuitMessage: Boolean = true, callPostEvent: Boolean = false) {
        val preVanishEvent = PreVanishEvent(player, sendQuitMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(preVanishEvent)

        if (preVanishEvent.isCancelled) return

        setMeta(player, true)

        updateTabState(player, GameMode.SPECTATOR)
        hidePlayer(player)

        if (player.allowFlight) {
            flyPlayers.add(player.uniqueId)
        }

        if (player.hasPermission("velocityvanish.action.fly.onvanish")) {
            player.allowFlight = true
            player.isFlying = true
        }

        if (ServerVersion.supports(9)) {
            if (player.isInvulnerable) {
                invulnerablePlayers.add(player.uniqueId)
            }

            if (Settings.invincible) {
                player.isInvulnerable = true
            }
        }

        player.isSleepingIgnored = true

        try {
            player.isCollidable = false
        } catch (_: NoClassDefFoundError) {
        } catch (_: NoSuchMethodError) { }

        player.world.entities.stream()
            .filter { entity -> entity is Creature }
            .map { entity -> entity as Creature }
            .filter { mob -> mob.target != null }
            .filter { mob -> player.uniqueId == mob.target?.uniqueId }
            .forEach { mob -> mob.target = null }

        addPotionEffects(player)

        if (DependencyManager.essentialsXHook.exists) {
            DependencyManager.essentialsXHook.vanish(player, true)
        }

        if (DependencyManager.sunlightHook.exists) {
            DependencyManager.sunlightHook.vanish(player, true)
        }

        if (ServerVersion.supports(9)) {
            denyPush(player)
        }

        if (DependencyManager.proCosmeticsHook.exists) {
            Ruom.runSync({
                try {
                    DependencyManager.proCosmeticsHook.proCosmetics.userManager?.getUser(player.uniqueId)?.unequipCosmetics(true)
                } catch (e: Exception) {
                    Ruom.warn("Failed to un-equip cosmetics for player ${player.name}, is ProCosmetics up to date?")
                }
            }, 20)
        }

        if (DependencyManager.squareMapHook.exists) {
            DependencyManager.squareMapHook.squareMap.playerManager().hide(player.uniqueId, true)
        }

        Settings.vanishSound.let {
            if (it != null) {
                player.playSound(player.location, it, 1f, 1f)
            }
        }

        Utils.sendVanishActionbar(player)
        plugin.vanishedNames.add(player.name)
        plugin.bridgeManager?.updateVanishedPlayersRequest(player, true)

        val quitMessage = Utils.getSerializedMessage(Settings.formatMessage(player, Message.QUIT_MESSAGE, TextReplacement("player", player.name), TextReplacement("play_displayname", player.displayName)))
        if (quitMessage.isNotBlank() && quitMessage.isNotEmpty() && sendQuitMessage) {
            Ruom.broadcast(quitMessage.component())
        }

        if (callPostEvent) {
            val postVanishEvent = PostVanishEvent(player, preVanishEvent.sendQuitMessage)
            VelocityVanishSpigot.instance.server.pluginManager.callEvent(postVanishEvent)
        }
    }

    fun unVanish(player: Player, sendJoinMessage: Boolean = true, callPostEvent: Boolean = false) {
        val preUnVanishEvent = PreUnVanishEvent(player, sendJoinMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(preUnVanishEvent)

        if (preUnVanishEvent.isCancelled) return

        setMeta(player, false)

        updateTabState(player, GameMode.SURVIVAL)

        if (!player.isOp) {
            player.allowFlight = flyPlayers.contains(player.uniqueId)
            player.isFlying = flyPlayers.contains(player.uniqueId)
        }

        if (ServerVersion.supports(9)) {
            player.isInvulnerable = false /*invulnerablePlayers.contains(player.uniqueId)*/
        }

        for (onlinePlayer in Ruom.getOnlinePlayers()) {
            @Suppress("DEPRECATION")
            onlinePlayer.showPlayer(player)
        }

        player.isSleepingIgnored = false

        try {
            player.isCollidable = true
        } catch (_: NoClassDefFoundError) {
        } catch (_: NoSuchMethodError) { }

        removePotionEffects(player)

        if (DependencyManager.essentialsXHook.exists) {
            DependencyManager.essentialsXHook.vanish(player, false)
        }

        if (DependencyManager.sunlightHook.exists) {
            DependencyManager.sunlightHook.vanish(player, false)
        }

        Utils.actionbarPlayers.remove(player)

        if (ServerVersion.supports(9)) {
            allowPush(player)
        }

        if (DependencyManager.proCosmeticsHook.exists) {
            try {
                DependencyManager.proCosmeticsHook.proCosmetics.userManager?.getUser(player.uniqueId)?.equipLastCosmetics(true)
            } catch (e: Exception) {
                Ruom.warn("Failed to equip cosmetics for player ${player.name}, is ProCosmetics up to date?")
            }
        }

        if (DependencyManager.squareMapHook.exists) {
            DependencyManager.squareMapHook.squareMap.playerManager().show(player.uniqueId, true)
        }

        Settings.unVanishSound.let {
            if (it != null) {
                player.playSound(player.location, it, 1f, 1f)
            }
        }

        Utils.sendVanishActionbar(player)
        plugin.vanishedNames.remove(player.name)
        plugin.bridgeManager?.updateVanishedPlayersRequest(player, false)

        val joinMessage = Utils.getSerializedMessage(Settings.formatMessage(player, Message.JOIN_MESSAGE, TextReplacement("player_displayname", player.displayName), TextReplacement("player", player.name)))
        if (joinMessage.isNotBlank() && joinMessage.isNotEmpty() && sendJoinMessage) {
            Ruom.broadcast(joinMessage.component())
        }

        if (callPostEvent) {
            val postUnVanishEvent = PostUnVanishEvent(player, preUnVanishEvent.sendJoinMessage)
            VelocityVanishSpigot.instance.server.pluginManager.callEvent(postUnVanishEvent)
        }
    }

}