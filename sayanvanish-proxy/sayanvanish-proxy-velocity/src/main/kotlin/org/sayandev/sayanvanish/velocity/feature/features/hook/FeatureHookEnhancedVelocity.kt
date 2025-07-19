package org.sayandev.sayanvanish.velocity.feature.features.hook

import ir.syrent.enhancedvelocity.api.VanishHook
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.velocity.feature.HookFeature
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.generateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import kotlinx.serialization.Serializable
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.launch
import java.util.UUID

@RegisteredFeature
@Serializable
class FeatureHookEnhancedVelocity : HookFeature("hook_enhancedvelocity", "enhancedvelocity") {

    override fun enable() {
        if (hasPlugin()) {
            EnhancedVelocityImpl().register()
        }
        super.enable()
    }
}

private class EnhancedVelocityImpl : VanishHook {
    override fun setIsVanished(uniqueId: UUID): Boolean {
        return VanishAPI.get().isVanishedBlocking(uniqueId)
    }

    override fun setVanished(uniqueId: UUID) {
        val player = StickyNote.getPlayer(uniqueId) ?: return
        launch {
            player.getVanishUser()?.disappear() ?: player.generateVanishUser().disappear()
        }
    }

    override fun setUnVanished(uniqueId: UUID) {
        val player = StickyNote.getPlayer(uniqueId) ?: return
        launch {
            player.getVanishUser()?.appear() ?: player.generateVanishUser().appear()
        }
    }
}