package org.sayandev.sayanvanish.api

import com.mysql.cj.jdbc.exceptions.OperationNotSupportedException
import net.kyori.adventure.text.Component
import org.sayandev.sayanvanish.api.storage.TransactionDatabase
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import java.io.File
import java.util.logging.Logger

data class Platform(
    val id: String,
    val pluginName: String,
    val logger: Logger,
    var rootDirectory: File,
    var serverId: String,
    val adapter: PlatformAdapter<in User, out VanishUser>,
) {

    companion object {
        private var currentPlatform = Platform("default", "SayanVanish", Logger.getGlobal(), File("."), "unknown", object : PlatformAdapter<User, VanishUser> {
            override fun adapt(user: VanishUser): VanishUser {
                throw OperationNotSupportedException("Default platform doesn't support vanish user adapt")
            }

            override fun sendMessage(user: User, message: Component) {
                throw OperationNotSupportedException("Default platform doesn't support sending message to user")
            }
        })

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