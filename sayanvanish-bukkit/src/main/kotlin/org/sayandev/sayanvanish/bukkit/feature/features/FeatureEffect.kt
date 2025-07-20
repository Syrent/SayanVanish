package org.sayandev.sayanvanish.bukkit.feature.features

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
import org.sayandev.stickynote.core.configuration.Config
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("effect")
@Suppress("DEPRECATION")
data class FeatureEffect(
    override var enabled: Boolean = true,
    @YamlComment(
        "All effects will being sent using packets to prevent conflict with other plugins or desyncs.",
        "List of effects to apply when a player vanishes"
    )
    @Contextual val effects: List<PotionEffectData> = listOf(
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
) : ListenedFeature("effect", enabled) {

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

    companion object {
        init {
            Config.registerSerializer(PotionEffectTypeSerializer)
        }
    }
}

@Serializable
class PotionEffectData(
    val usePacket: Boolean = true,
    val keepAfterAppear: Boolean = false,
    val type: String = PotionEffectType.NIGHT_VISION.name,
    val duration: Int = Int.MAX_VALUE,
    val amplifier: Int = 0,
    val ambient: Boolean = false,
    val particles: Boolean = false,
) {
    fun toPotionEffect() = PotionEffect(PotionEffectType.getByName(type)!!, if (ServerVersion.supports(19) && duration == Int.MAX_VALUE) -1 else duration, amplifier, ambient, particles)
}

object PotionEffectTypeSerializer : KSerializer<PotionEffectType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("name", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PotionEffectType {
        val name = decoder.decodeString()
        return PotionEffectType.getByName(name) ?: throw IllegalArgumentException("Unknown PotionEffectType: $name")
    }

    override fun serialize(encoder: Encoder, value: PotionEffectType) {
        encoder.encodeString(value.name)
    }
}