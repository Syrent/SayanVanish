package org.sayandev.sayanvanish.api

import org.sayandev.sayanvanish.api.storage.TransactionDatabase
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import java.io.File
import java.util.logging.Logger

open class Platform(
    val id: String,
    val pluginName: String,
    val logger: Logger,
    var rootDirectory: File,
    var serverId: String,
    val adapter: PlatformAdapter<out User, out VanishUser>,
) {

    open suspend fun register() { }

    open suspend fun unregister() {
        VanishAPI.get().getDatabase().disconnect().await()
    }

    companion object {
        private var currentPlatform = Platform(
            "sayanvanish",
            "SayanVanish",
            Logger.getGlobal(),
            File(".", "sayanvanish").apply {
                if (!this.exists()) {
                    this.mkdirs()
                }
            },
            "sayanvanish-virtual-server",
            APIPlatformAdapter()
        )

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
            platform.register()

            (VanishAPI.get().getDatabase() as? TransactionDatabase)?.let {
                if (!it.databaseConnected) {
                    return false
                }
            }

            register()
            return true
        }

        @JvmStatic
        fun get() = currentPlatform
    }



}