package ir.syrent.velocityvanish.spigot.hook

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import ir.syrent.velocityvanish.spigot.ruom.Ruom

class ProtocolLibHook constructor(name: String) : Dependency(name) {

    var protocolManager: ProtocolManager? = null

    init {
        if (exists) {
            try {
                protocolManager = ProtocolLibrary.getProtocolManager()
            } catch (_: NoClassDefFoundError) {
                Ruom.warn("Could not initialize ProtocolLib hook. If you want to use its features you may need to upgrade your server and plugin to latest build.")
            }
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Change player name type to spectator whenever player vanishes (Applies to tab and player character)"
        )
    }

}