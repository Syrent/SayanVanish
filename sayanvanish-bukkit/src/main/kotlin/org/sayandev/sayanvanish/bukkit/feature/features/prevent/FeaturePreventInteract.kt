package org.sayandev.sayanvanish.bukkit.feature.features.prevent

import com.cryptomorin.xseries.XMaterial
import org.bukkit.block.Container
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.stickynote.bukkit.utils.ServerVersion
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("prevent_interact_event")
class FeaturePreventInteract(
    override var enabled: Boolean = true,
    @YamlComment("Prevent players from activating pressure plates while vanished")
    @Configurable val pressurePlateTrigger: Boolean = true,
    @YamlComment("Prevent players from interacting with big dripleaf while vanished")
    @Configurable val dripLeaf: Boolean = true,
    @YamlComment("Prevent players from interacting")
    @Configurable val interact: Boolean = false,
    @Configurable val tripwire: Boolean = true,
    @Configurable val button: Boolean = true,
) : ListenedFeature("prevent_interact_event", enabled, category = FeatureCategories.PREVENTION) {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun cancelInteract(event: PlayerInteractEvent) {
        if (ServerVersion.supports(13)) {
            if (event.clickedBlock?.state is Container) return
        }
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            val isPressurePlate = pressurePlateTrigger && event.action == Action.PHYSICAL && event.clickedBlock?.type?.name?.contains("PLATE") == true
            val isDripLeaf = dripLeaf && event.action == Action.PHYSICAL && event.clickedBlock?.type?.name?.equals("BIG_DRIPLEAF") == true
            if (interact || (isPressurePlate && pressurePlateTrigger) || (isDripLeaf && dripLeaf)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun cancelTripwireInteract(event: PlayerInteractEvent) {
        if (!tripwire) return
        val block = event.clickedBlock ?: return
        if (event.action != Action.PHYSICAL) return
        if (block.type != XMaterial.TRIPWIRE.get()!! && block.type != XMaterial.STRING.get()!!) return
        val player = event.player
        val user = player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun cancelButtonInteract(event: PlayerInteractEvent) {
        if (!button) return
        val block = event.clickedBlock ?: return
        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        if (!block.type.name.contains("BUTTON")) return
        val player = event.player
        val user = player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (!user.isVanished) return
        event.isCancelled = true
    }

}