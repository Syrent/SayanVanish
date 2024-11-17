package org.sayandev.sayanvanish.proxy.config

import org.sayandev.sayanvanish.api.Platform
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

public var language: LanguageConfig = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()

@ConfigSerializable
data class LanguageConfig(
    val general: General = General(),
    val vanish: Vanish = Vanish(),
) : Config(languageDirectory, "${settings.general.language}.yml") {

    init {
        load()
    }

    @ConfigSerializable
    data class General(
        val prefix: String = "<#67e8f9>SayanVanish</#67e8f9> <gray>|</gray> <yellow>",
        val reloaded: String = "<green>Plugin successfully reloaded. <red>Please note that some changes may require a server restart to take effect. Subsequent reloads may cause issues.",
        val playerNotFound: String = "<red>Player not found",
        val userNotFound: String = "<red>Couldn't get user data for player <gold><player></gold>",
        val haveToProvidePlayer: String = "<red>You have to provide a player.",
        val dontHavePermission: String = "<red>You don't have permission to do that.",
        val confirmUpdate: String = "<red>Execute update again to update the plugin.",
        val updating: String = "<gray>Updating the plugin... please wait.",
        val updated: String = "<green>Plugin has been successfully updated to version <gold><version></gold> and the update will be applied on next server restart.",
        val proxyUpdateWarning: String = "<red>Please note that this update could impact your proxy server. Be sure to update your proxy server separately to ensure compatibility.",
        val updateFailed: String = "<red>Failed to update the plugin. Please try again later."
    )

    @ConfigSerializable
    data class Vanish(
        val vanishToggle: String = "<gray>Successfully updated <aqua><player></aqua> vanish state to <state>",
    )

    enum class Language(val id: String) {
        EN_US("en_US"),
    }

    companion object {
        val languageDirectory = File(Platform.get().rootDirectory, "languages")

        @JvmStatic

        fun defaultConfig(): LanguageConfig {
            return LanguageConfig()
        }

        @JvmStatic
        fun fromConfig(): LanguageConfig? {
            return fromConfig<LanguageConfig>(File(languageDirectory, "${settings.general.language}.yml"))
        }
    }
}