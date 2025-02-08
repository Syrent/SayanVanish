package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import java.io.File
import java.util.logging.Logger

data class Platform(
    val id: String,
    val logger: Logger,
    var rootDirectory: File,
    var serverId: String,
) {

    companion object {
        private var currentPlatform = Platform("default", Logger.getGlobal(), File("."), "unknown")

        @JvmStatic
        fun setPlatform(platform: Platform) {
            currentPlatform = platform
        }

        @JvmStatic
        fun register() {
            RegisteredFeatureHandler.process()
        }

        @JvmStatic
        fun setAndRegister(platform: Platform): Boolean {
            setPlatform(platform)

            if (!SayanVanishAPI.getInstance().databaseConnected) {
                return false
            }

            register()
            return true
        }

        @JvmStatic
        fun get() = currentPlatform
    }



}