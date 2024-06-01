package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import java.io.File
import java.util.logging.Logger

@JvmRecord
data class Platform(
    val id: String,
    val logger: Logger,
    val rootDirectory: File
) {

    companion object {
        private var currentPlatform = Platform("default", Logger.getGlobal(), File("."))

        @JvmStatic
        fun setPlatform(platform: Platform) {
            currentPlatform = platform
        }

        @JvmStatic
        fun register() {
            RegisteredFeatureHandler.process()
        }

        @JvmStatic
        fun setAndRegister(platform: Platform) {
            setPlatform(platform)
            register()
        }

        @JvmStatic
        fun get() = currentPlatform
    }



}