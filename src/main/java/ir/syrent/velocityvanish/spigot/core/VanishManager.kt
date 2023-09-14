package ir.syrent.velocityvanish.spigot.core

import com.Zrips.CMI.CMI
import com.Zrips.CMI.commands.list.vanishedit
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.mojang.authlib.GameProfile
import io.papermc.paper.threadedregions.scheduler.EntityScheduler
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
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
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
        PotionEffect(PotionEffectType.INVISIBILITY, Int.MAX_VALUE, 255, false, false),
    )

    init {
        if (ServerVersion.supports(13)) potions.add(PotionEffect(PotionEffectType.WATER_BREATHING, Int.MAX_VALUE, 255, false, false))
    }

    val invulnerablePlayers = mutableSetOf<UUID>()

    fun updateTabState(player: Player, state: GameMode) {
        if (Settings.seeAsSpectator) {
            if (ServerVersion.supports(19)) {
                updateTabStateViaPacket(player, state)
            } else {
                if (DependencyManager.protocolLibHook.exists) {
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

                        for (onlinePlayer in Ruom.onlinePlayers.filter { it.hasPermission("velocityvanish.admin.seevanished") }.filter { it != player }) {
                            DependencyManager.protocolLibHook.protocolManager.sendServerPacket(onlinePlayer, newTabPacket)
                        }
                    } catch (_: Exception) {
                        Ruom.warn("Couldn't vanish player using ProtocolLib, Is you server/plugins up-to-date?")
                    }
                }
            }
        }
    }

    fun hidePlayer(player: Player) {
        for (onlinePlayer in Ruom.onlinePlayers.filter { !it.hasPermission("velocityvanish.admin.seevanished") }) {
            val onlinePlayerVanishLevel = getVanishLevel(onlinePlayer)
            if (onlinePlayerVanishLevel >= getVanishLevel(player) && getVanishLevel(player) != 0) continue

            @Suppress("DEPRECATION")
            onlinePlayer.hidePlayer(player)
        }
    }

    fun getVanishLevel(player: Player): Int {
        return player.effectivePermissions.map { it.permission }
            .filter { it.startsWith("velocityvanish.level.") }.maxOfOrNull { it.split(".")[2].toInt() } ?: 0
    }

    private fun setMeta(player: Player, meta: Boolean) {
        player.setMetadata("vanished", FixedMetadataValue(plugin, meta))
    }

    fun addPotionEffects(player: Player) {
        Ruom.runSync({
            for (potionEffect in potions) {
                try {
                    @Suppress("DEPRECATION") val mobEffect = MobEffectInstanceAccessor.getConstructor0().newInstance(
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
                    @Suppress("DEPRECATION") NMSUtils.sendPacket(player, ClientboundRemoveMobEffectPacketAccessor.getConstructor0().newInstance(player.entityId, MobEffectAccessor.getMethodById1().invoke(null, potionEffect.type.id)))
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
        vanish(player, sendQuitMessage, callPostEvent, false)
    }

    fun vanish(player: Player, sendQuitMessage: Boolean = true, callPostEvent: Boolean = false, notifyAdmins: Boolean = false) {
        val preVanishEvent = PreVanishEvent(player, sendQuitMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(preVanishEvent)

        if (preVanishEvent.isCancelled) return

        setMeta(player, true)

        updateTabState(player, GameMode.SPECTATOR)
        hidePlayer(player)

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

        if (Ruom.isFolia) {
            player.world.entities
                .filterIsInstance<Creature>()
                .forEach { creature ->
                    creature.scheduler.run(Ruom.plugin, {
                        if (creature.target?.uniqueId == player.uniqueId) {
                            creature.target = null
                        }
                    }, {})
                }
        } else {
            player.world.entities.stream()
                .filter { entity -> entity is Creature }
                .map { entity -> entity as Creature }
                .filter { mob -> mob.target != null }
                .filter { mob -> player.uniqueId == mob.target?.uniqueId }
                .forEach { mob -> mob.target = null }
        }

        addPotionEffects(player)

        if (DependencyManager.cmiHook.exists) {
            val cmiUser = CMI.getInstance().playerManager.getUser(player)
            cmiUser.isVanished = true
            CMI.getInstance().vanishManager.addPlayer(cmiUser)
            cmiUser.vanish.set(vanishedit.VanishAction.isVanished, true)
            cmiUser.vanish.set(vanishedit.VanishAction.informOnJoin, false)
            cmiUser.updateVanishMode()
        }

        if (DependencyManager.essentialsXHook.exists) {
            DependencyManager.essentialsXHook.vanish(player, true)
        }

        if (DependencyManager.sunlightHook.exists) {
            DependencyManager.sunlightHook.vanish(player, true)
        }

        if (ServerVersion.supports(9)) {
            denyPush(player)
        }

        /*if (DependencyManager.proCosmeticsHook.exists) {
            Ruom.runSync({
                try {
                    DependencyManager.proCosmeticsHook.proCosmetics.userManager?.getUser(player.uniqueId)?.unequipCosmetics(true)
                } catch (e: Exception) {
                    Ruom.warn("Failed to un-equip cosmetics for player ${player.name}, is ProCosmetics up to date?")
                }
            }, 20)
        }*/

        if (DependencyManager.squareMapHook.exists) {
            DependencyManager.squareMapHook.squareMap.playerManager().hide(player.uniqueId, true)
        }

        if (sendQuitMessage) {
            if (DependencyManager.discordSRVHook.exists) {
                DependencyManager.discordSRVHook.discordSRV.sendLeaveMessage(player, Settings.formatMessage(Message.DISCORDSRV_QUIT_MESSAGE))
            }
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
        if (quitMessage.isNotBlank() && quitMessage.isNotEmpty() && sendQuitMessage && Settings.fakeJoinLeaveMessage) {
            Ruom.broadcast(quitMessage.component())
        }

        if (callPostEvent) {
            val postVanishEvent = PostVanishEvent(player, preVanishEvent.sendQuitMessage)
            VelocityVanishSpigot.instance.server.pluginManager.callEvent(postVanishEvent)
        }

        if (notifyAdmins) {
            for (staff in Ruom.onlinePlayers.filter { it.hasPermission("velocityvanish.admin.notify") && it != player }) {
                staff.sendMessage(Message.VANISH_NOTIFY, TextReplacement("player", player.name))
            }
        }
    }


    fun unVanish(player: Player, sendJoinMessage: Boolean = true, callPostEvent: Boolean = false) {
        unVanish(player, sendJoinMessage, callPostEvent, false)
    }

    fun unVanish(player: Player, sendJoinMessage: Boolean = true, callPostEvent: Boolean = false, notifyAdmins: Boolean = false) {
        val preUnVanishEvent = PreUnVanishEvent(player, sendJoinMessage)
        VelocityVanishSpigot.instance.server.pluginManager.callEvent(preUnVanishEvent)

        if (preUnVanishEvent.isCancelled) return

        setMeta(player, false)

        updateTabState(player, GameMode.SURVIVAL)

        val canFly = (player.allowFlight && player.hasPermission("velocityvanish.action.fly.onvanish")) || player.isOp
        player.allowFlight = canFly
        player.isFlying = canFly

        if (ServerVersion.supports(9)) {
            player.isInvulnerable = false /*invulnerablePlayers.contains(player.uniqueId)*/
        }

        for (onlinePlayer in Ruom.onlinePlayers) {
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

        if (DependencyManager.cmiHook.exists) {
            val cmiUser = CMI.getInstance().playerManager.getUser(player)
            cmiUser.isVanished = false
            CMI.getInstance().vanishManager.removePlayer(cmiUser)
            cmiUser.vanish.set(vanishedit.VanishAction.isVanished, false)
            cmiUser.vanish.set(vanishedit.VanishAction.informOnJoin, true)
            cmiUser.updateVanishMode()
        }

        Utils.actionbarPlayers.remove(player)

        if (ServerVersion.supports(9)) {
            allowPush(player)
        }

        /*if (DependencyManager.proCosmeticsHook.exists) {
            try {
                DependencyManager.proCosmeticsHook.proCosmetics.userManager?.getUser(player.uniqueId)?.equipLastCosmetics(true)
            } catch (e: Exception) {
                Ruom.warn("Failed to equip cosmetics for player ${player.name}, is ProCosmetics up to date?")
            }
        }*/

        if (DependencyManager.squareMapHook.exists) {
            DependencyManager.squareMapHook.squareMap.playerManager().show(player.uniqueId, true)
        }

        if (sendJoinMessage) {
            if (DependencyManager.discordSRVHook.exists) {
                DependencyManager.discordSRVHook.discordSRV.sendJoinMessage(player, Settings.formatMessage(Message.DISCORDSRV_JOIN_MESSAGE))
            }
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
        if (joinMessage.isNotBlank() && joinMessage.isNotEmpty() && sendJoinMessage && Settings.fakeJoinLeaveMessage) {
            Ruom.broadcast(joinMessage.component())
        }

        if (callPostEvent) {
            val postUnVanishEvent = PostUnVanishEvent(player, preUnVanishEvent.sendJoinMessage)
            VelocityVanishSpigot.instance.server.pluginManager.callEvent(postUnVanishEvent)
        }

        if (notifyAdmins) {
            for (staff in Ruom.onlinePlayers.filter { it.hasPermission("velocityvanish.admin.notify") && it != player }) {
                staff.sendMessage(Message.UNVANISH_NOTIFY, TextReplacement("player", player.name))
            }
        }
    }

    fun updateTabStateViaPacket(player: Player, gameMode: GameMode, exceptSelf: Boolean = true) {
        try {
            val profile = GameProfile(player.uniqueId, player.name)
            val serverPlayer = NMSUtils.getServerPlayer(player)

            val packet = ClientboundPlayerInfoUpdatePacketAccessor.getConstructor0().newInstance(
                ClientboundPlayerInfoUpdatePacket_i_ActionAccessor.getFieldUPDATE_GAME_MODE(),
                serverPlayer
            )
            val list: MutableList<Any> = (ClientboundPlayerInfoUpdatePacketAccessor.getMethodEntries1().invoke(packet) as List<Any>).toMutableList()
            list.add(
                ClientboundPlayerInfoUpdatePacket_i_EntryAccessor.getConstructor0().newInstance(
                    player.uniqueId,
                    profile,
                    true,
                    player.ping,
                    GameTypeAccessor.getMethodByName1().invoke(null, gameMode.name.lowercase()),
                    null,
                    null
                )
            )
            ClientboundPlayerInfoUpdatePacketAccessor.getFieldEntries().set(
                packet,
                list.toList()
            )

            for (onlinePlayer in Ruom.onlinePlayers.filter { it.hasPermission("velocityvanish.admin.seevanished") }) {
                if (exceptSelf && onlinePlayer == player) continue
                NMSUtils.sendPacket(onlinePlayer, packet)
            }

            if (gameMode == GameMode.SPECTATOR) {
                for (onlinePlayer in Ruom.onlinePlayers.filter { !it.hasPermission("velocityvanish.admin.seevanished") }) {
                    if (onlinePlayer == player) continue
                    NMSUtils.sendPacket(onlinePlayer, ClientboundPlayerInfoRemovePacketAccessor.getConstructor0().newInstance(listOf(EntityAccessor.getMethodGetUUID1().invoke(serverPlayer))))
                }
            } else {
                for (onlinePlayer in Ruom.onlinePlayers.filter { !it.hasPermission("velocityvanish.admin.seevanished") }) {
                    if (onlinePlayer == player) continue
                    NMSUtils.sendPacket(onlinePlayer, ClientboundPlayerInfoUpdatePacketAccessor.getMethodCreatePlayerInitializing1().invoke(null, listOf(serverPlayer)))
                }
            }
        } catch (_: Exception) {
            Ruom.warn("Couldn't update player state in tab via packets.")
        }
    }

}