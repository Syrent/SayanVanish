package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.NMSUtils
import org.sayandev.stickynote.bukkit.PacketUtils
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.sayandev.stickynote.lib.spongepowered.configurate.ConfigurationNode
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.lib.spongepowered.configurate.serialize.TypeSerializer
import org.sayandev.stickynote.lib.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

@RegisteredFeature
@ConfigSerializable
class FeatureEffect(
    val effects: List<PotionEffectData> = listOf(
        PotionEffectData(
            true,
            PotionEffectType.NIGHT_VISION,
            Int.MAX_VALUE,
            0,
            false,
            false,
        ),
        PotionEffectData(
            false,
            PotionEffectType.WATER_BREATHING,
            Int.MAX_VALUE,
            0,
            false,
            false,
        ),
        PotionEffectData(
            false,
            PotionEffectType.FIRE_RESISTANCE,
            Int.MAX_VALUE,
            0,
            false,
            false,
        )
    )
) : ListenedFeature("effect", additionalSerializers = TypeSerializerCollection.builder().register(PotionEffectType::class.java, PotionEffectTypeSerializer()).build()) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        if (!isActive()) return
        val player = event.user.player() ?: return
        for (effect in effects) {
            if (effect.usePacket) {
                NMSUtils.sendPacket(player,PacketUtils.getUpdateMobEffectPacket(player, effect.toPotionEffect()))
            } else {
                player.addPotionEffect(effect.toPotionEffect())
            }
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        val player = event.user.player() ?: return
        for (effect in effects) {
            if (effect.usePacket) {
                NMSUtils.sendPacket(player, PacketUtils.getRemoveMobEffectPacket(player, effect.type))
            } else {
                if (player.activePotionEffects.find { it.type == effect.type && it.amplifier == effect.amplifier && it.isAmbient == effect.ambient && it.hasParticles() == effect.particles } != null) {
                    player.removePotionEffect(effect.type)
                }
            }
        }
    }

}

@ConfigSerializable
data class PotionEffectData(
    val usePacket: Boolean,
    val type: PotionEffectType,
    val duration: Int,
    val amplifier: Int,
    val ambient: Boolean,
    val particles: Boolean,
) {
    fun toPotionEffect() = PotionEffect(type, if (ServerVersion.supports(19) && duration == Int.MAX_VALUE) -1 else duration, amplifier, ambient, particles)
}

class PotionEffectTypeSerializer : TypeSerializer<PotionEffectType> {
    override fun deserialize(type: Type, node: ConfigurationNode): PotionEffectType {
        return PotionEffectType.getByName(node.string!!)!!
    }

    override fun serialize(type: Type, effectType: PotionEffectType?, node: ConfigurationNode) {
        node.set(effectType?.name)
    }
}