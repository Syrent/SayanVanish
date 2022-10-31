package ir.syrent.velocityvanish.spigot.hook

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager

object ProtocolLibHook {

    var protocolManager: ProtocolManager
        private set

    init {
        protocolManager = ProtocolLibrary.getProtocolManager()
    }

}