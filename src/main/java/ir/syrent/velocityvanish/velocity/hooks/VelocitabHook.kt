package ir.syrent.velocityvanish.velocity.hooks

import ir.syrent.velocityvanish.velocity.VelocityVanish
import ir.syrent.velocityvanish.velocity.vruom.VRuom
import net.william278.velocitab.vanish.VanishIntegration
import kotlin.jvm.optionals.getOrNull

class VelocitabHook : VanishIntegration {


    override fun canSee(name: String, otherName: String): Boolean {
        return if (VelocityVanish.instance.vanishedPlayers.contains(name) && VelocityVanish.instance.vanishedPlayers.contains(otherName)) true
        else if (VRuom.getPlayer(name)?.getOrNull()?.hasPermission("velocityvanish.admin.seevanished") == true) true
        else if (VelocityVanish.instance.vanishedPlayers.contains(otherName)) false
        else true
    }

    override fun isVanished(name: String): Boolean {
        return VelocityVanish.instance.vanishedPlayers.contains(name)
    }

}