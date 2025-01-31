package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.event.EventHandler
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.nms.NMSUtils.sendPacket
import org.sayandev.stickynote.bukkit.nms.PacketUtils
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.serialize.TypeSerializer
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.lang.reflect.Type

@RegisteredFeature
@ConfigSerializable
@Suppress("DEPRECATION")
data class FeatureEffect(
    @Comment("""
    All effects will being sent using packets to prevent conflict with other plugins or desyncs.
    List of effects to apply when a player vanishes
    """)
    val effects: List<PotionEffectData> = listOf(
        PotionEffectData(
            ServerVersion.supports(9),
            false,
            PotionEffectType.NIGHT_VISION.name,
            Int.MAX_VALUE,
            0,
            false,
            false,
        ),
        PotionEffectData(
            false,
            false,
            PotionEffectType.WATER_BREATHING.name,
            Int.MAX_VALUE,
            0,
            false,
            false,
        ),
        PotionEffectData(
            false,
            false,
            PotionEffectType.FIRE_RESISTANCE.name,
            Int.MAX_VALUE,
            0,
            false,
            false,
        )
    )
) : ListenedFeature("effect", additionalSerializers = TypeSerializerCollection.builder().register(PotionEffectType::class.java, PotionEffectTypeSerializer()).build()) {

    @EventHandler
    private fun onVanish(event: BukkitUserVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        val player = user.player() ?: return
        for (effect in effects) {
            if (effect.usePacket) {
                player.sendPacket(PacketUtils.getUpdateMobEffectPacket(player, effect.toPotionEffect()))
            } else {
                player.addPotionEffect(effect.toPotionEffect())
            }
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        val user = event.user
        if (!isActive(user)) return
        val player = user.player() ?: return
        for (effect in effects.filter { !it.keepAfterAppear }) {
            if (effect.usePacket) {
                player.sendPacket(PacketUtils.getRemoveMobEffectPacket(player, PotionEffectType.getByName(effect.type)!!))
            } else {
                if (player.activePotionEffects.find { it.type.name == effect.type && it.amplifier == effect.amplifier && it.isAmbient == effect.ambient && it.hasParticles() == effect.particles } != null) {
                    player.removePotionEffect(PotionEffectType.getByName(effect.type)!!)
                }
            }
        }
    }

}

@ConfigSerializable
data class PotionEffectData(
    val usePacket: Boolean,
    val keepAfterAppear: Boolean = false,
    val type: String,
    val duration: Int,
    val amplifier: Int,
    val ambient: Boolean,
    val particles: Boolean,
) {
    fun toPotionEffect() = PotionEffect(PotionEffectType.getByName(type)!!, if (ServerVersion.supports(19) && duration == Int.MAX_VALUE) -1 else duration, amplifier, ambient, particles)
}

class PotionEffectTypeSerializer : TypeSerializer<PotionEffectType> {
    override fun deserialize(type: Type, node: ConfigurationNode): PotionEffectType {
        return PotionEffectType.getByName(node.string!!)!!
    }

    override fun serialize(type: Type, effectType: PotionEffectType?, node: ConfigurationNode) {
        node.set(effectType?.name)
    }
}