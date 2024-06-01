package org.sayandev.sayanvanish.velocity.feature.hook

import ir.syrent.enhancedvelocity.api.VanishHook
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.getOrAddUser
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import org.sayandev.stickynote.velocity.StickyNote
import java.util.UUID

@RegisteredFeature
@ConfigSerializable
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
        return SayanVanishVelocityAPI.getInstance().isVanished(uniqueId, false)
    }

    override fun setVanished(uniqueId: UUID) {
        StickyNote.getPlayer(uniqueId)?.getOrAddUser()?.vanish()
    }

    override fun setUnVanished(uniqueId: UUID) {
        StickyNote.getPlayer(uniqueId)?.getOrAddUser()?.unVanish()
    }
}