package org.sayandev.sayanvanish.bukkit.config

import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

public var language: LanguageConfig = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()

@ConfigSerializable
data class LanguageConfig(
    val vanish: Vanish = Vanish(),
    val general: General = General(),
    val paste: Paste = Paste()
) : Config(languageDirectory, "${settings.general.language}.yml") {

    init {
        load()
    }

    @ConfigSerializable
    data class General(
        val reloaded: String = "<green>Plugin successfully reloaded. <red>Please note that some changes may require a server restart to take effect. Subsequent reloads may cause issues.",
        val playerNotFound: String = "<red>Player not found",
    )

    @ConfigSerializable
    data class Vanish(
        val vanishStateUpdate: String = "<gray>Your vanish state has been updated to <state>.",
        val offlineOnVanish: String = "<gray><gold><player></gold> is currently offline. The vanish state has been updated to <state> and will take effect upon their return.",
        val vanishStateOther: String = "<gray>The vanish state of <gold><player></gold> has been updated to <state>.",
        val vanishFromQueue: String = "<gray>Your vanish state has been changed to <gold><state></gold> from queue.",
        val cantChatWhileVanished: String = "<gray>You can't chat while you are vanished, add <gold><bold>!</bold></gold> at the beginning of your message to bypass this.",
        val levelSet: String = "<gray><gold><player></gold> vanish level has been set to <gold><level></gold>",
        val levelGet: String = "<gray><gold><player></gold> vanish level is <gold><level></gold>",
    )

    @ConfigSerializable
    data class Paste(
        val use: String = "<gray>Your paste key is <gold><key></gold>, <red>Make sure to check the content before sharing it with others and remove the data you don't want to share. <white><click:open_url:'https://paste.sayandev.org/<key>'>(Click to open in sayandev paste)</click>",
        val generating: String = "<gold>Generating paste, please wait...",
        val failedToGenerate: String = "<red>Failed to generate paste, please try again later and make sure your machine is connected to internet.",
    )

    enum class Language(val id: String) {
        EN_US("en_US"),
    }

    companion object {
        val languageDirectory = File(pluginDirectory, "languages")

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