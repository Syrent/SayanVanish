package ir.syrent.velocityvanish.spigot.hook

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.storage.Settings

object DependencyManager {

    var protocolLibHook: ProtocolLibHook
        private set
    var placeholderAPIHook: PlaceholderAPIHook
        private set
    var sayanChatHook: SayanChatHook
        private set
    var essentialsXHook: EssentialsXHook
        private set
    var squareMapHook: SquareMapHook
        private set

    init {
        ProtocolLibHook("ProtocolLib").apply {
            this.register()
            protocolLibHook = this
        }
        SayanChatHook("SayanChat").apply {
            this.register()
            sayanChatHook = this
        }
        EssentialsXHook("Essentials").apply {
            this.register()
            essentialsXHook = this
        }
        EnhancedVelocityHook("EnhancedVelocity").apply {
            if (Settings.velocitySupport) {
                this.register()
            }
        }
        PlaceholderAPIHook(VelocityVanishSpigot.instance, "PlaceholderAPI").apply {
            this.register()
            placeholderAPIHook = this
        }
        SquareMapHook("squaremap").apply {
            this.register()
            squareMapHook = this
        }
    }

}