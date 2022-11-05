package ir.syrent.velocityvanish.spigot.hook

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.storage.Settings

object DependencyManager {

    var protocolLibHook: ProtocolLibHook
        private set
    var proCosmeticsHook: ProCosmeticsHook
        private set
    var placeholderAPIHook: PlaceholderAPIHook
        private set

    init {
        ProtocolLibHook("ProtocolLib").apply {
            this.register()
            protocolLibHook = this
        }
        SayanChatHook("SayanChat").apply {
            this.register()
        }
        EnhancedVelocityHook("EnhancedVelocity").apply {
            if (Settings.velocitySupport) {
                this.register()
            }
        }
        ProCosmeticsHook("ProCosmetics").apply {
            this.register()
            proCosmeticsHook = this
        }
        PlaceholderAPIHook(VelocityVanishSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
    }

}