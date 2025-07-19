package org.sayandev.sayanvanish.proxy.config

import github.scarsz.discordsrv.dependencies.okhttp3.internal.http2.Settings
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.core.configuration.Config
import kotlinx.serialization.Serializable
import org.sayandev.sayanvanish.api.feature.Feature.Companion.directory
import java.io.File

public var language: LanguageConfig = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()

@Serializable
class LanguageConfig(
    val general: General = General(),
    val vanish: Vanish = Vanish(),
) {

    @Serializable
    data class General(
        val prefix: String = "<#67e8f9>SayanVanish</#67e8f9> <gray>|</gray> <yellow>",
        val reloaded: String = "<green>Plugin successfully reloaded. <red>Please note that some changes may require a server restart to take effect. Subsequent reloads may cause issues.",
        val playerNotFound: String = "<red>Player not found",
        val userNotFound: String = "<red>Couldn't get user data for player <gold><player></gold>",
        val haveToProvidePlayer: String = "<red>You have to provide a player.",
        val dontHavePermission: String = "<red>You don't have permission to do that.",
        val confirmUpdate: String = "<red>Execute update again to update the plugin.",
        val updating: String = "<gray>Updating the plugin... please wait.",
        val updated: String = "<green>Plugin has been successfully updated to version <gold><version></gold>. make sure to restart the server to prevent unexpected behaviour.",
        val proxyUpdateWarning: String = "<red>Please note that this update could impact your proxy server. Be sure to update your proxy server separately to ensure compatibility.",
        val updateFailed: String = "<red>Failed to update the plugin. Please try again later."
    )

    @Serializable
    data class Vanish(
        val placeholderPrefix: String = "&7[Vanished]&r ",
        val placeholderSuffix: String = " &r&7[Vanished]",
        val vanishToggle: String = "<gray>Successfully updated <aqua><player></aqua> vanish state to <state>",
    )

    @Serializable
    enum class Language(val id: String) {
        EN_US("en_US"),
    }

    fun save() {
        Config.save(File(languageDirectory, "${settings.general.language}.yml"), this)
    }

    companion object {
        val languageDirectory = File(Platform.get().rootDirectory, "languages")

        @JvmStatic

        fun defaultConfig(): LanguageConfig {
            return LanguageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): LanguageConfig? {
            return Config.fromFile<LanguageConfig>(File(languageDirectory, "${settings.general.language}.yml"))
        }
    }
}