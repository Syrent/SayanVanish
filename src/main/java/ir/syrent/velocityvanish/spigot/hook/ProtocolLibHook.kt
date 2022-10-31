package ir.syrent.velocityvanish.spigot.hook

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager

class ProtocolLibHook constructor(name: String) : Dependency(name) {

    lateinit var protocolManager: ProtocolManager
        private set

    init {
        if (exists) {
            protocolManager = ProtocolLibrary.getProtocolManager()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Change player name type to spectator whenever player vanishes (Applies to tab and player character)"
        )
    }

}