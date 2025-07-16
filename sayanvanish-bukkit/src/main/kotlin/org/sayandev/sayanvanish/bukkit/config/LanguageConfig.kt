package org.sayandev.sayanvanish.bukkit.config

import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.core.configuration.Config
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

public var language: LanguageConfig = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()

@ConfigSerializable
class LanguageConfig(
    val general: General = General(),
    val vanish: Vanish = Vanish(),
    val feature: Feature = Feature(),
    val paste: Paste = Paste()
) : Config(languageDirectory, "${SettingsConfig.get().general.language}.yml") {

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
    data class Feature(
        val notFound: String = "<red>Feature not found",
        val enabled: String = "<gray><gold><feature></gold> has been enabled.",
        val disabled: String = "<gray><gold><feature></gold> has been disabled.",
        val alreadyDisabled: String = "<gray><gold><feature></gold> is already disabled.",
        val alreadyEnabled: String = "<gray><gold><feature></gold> is already enabled.",
        val updated: String = "<gray><gold><feature> <option></gold> has been updated to <gold><state></gold>.",
        val invalidOption: String = "<red>Invalid option, valid options are <gold><options></gold>.",
        val invalidValue: String = "<red>Invalid value, valid values are <gold><values></gold>.",
        val reset: String = "<gray><gold><feature></gold> has been reset.",
        val status: String = "<gray><gold><feature></gold> feature status is <status>.",
        val togglePlayer: String = "<gray><gold><player></gold> <aqua><feature></aqua> feature state has been updated to <state>.",
        val togglePlayerOther: String = "<red>You don't have permission to change change other players feature state.",
        val permissionLevelMethodWarning: String = "<red>This server uses <method> for vanish levels. To change the vanish level, use a permission system like LuckPerms or another available method. Available methods: <gold><methods></gold>.",
        val flyDisabled: String = "<red>You don't have keep fly after reappear permission, fly has been disabled.",
    )

    @ConfigSerializable
    data class Vanish(
        val placeholderPrefix: String = "&7[Vanished]&r ",
        val placeholderSuffix: String = " &r&7[Vanished]",
        val vanishStateUpdate: String = "<gray>Your vanish state has been updated to <state>.",
        val offlineOnVanish: String = "<gray><gold><player></gold> is currently offline. The vanish state has been updated to <state> and will take effect upon their return.",
        val vanishStateOther: String = "<gray>The vanish state of <gold><player></gold> has been updated to <state>.",
        val leftTheServerWhileVanished: String = "<gray><gold><player></gold> left the server while vanished.",
        val joinedTheServerWhileVanished: String = "<gray><gold><player></gold> joined the server while vanished.",
        val vanishFromQueue: String = "<gray>Your vanish state has been changed to <gold><state></gold> from queue.",
        val cantChatWhileVanished: String = "<gray>You can't chat while you are vanished, add <gold><bold><char></bold></gold> at the beginning of your message to bypass this.",
        val levelSet: String = "<gray><gold><player></gold> vanish level has been set to <gold><level></gold>",
        val levelGet: String = "<gray><gold><player></gold> vanish level is <gold><level></gold>",
        val dontHaveUsePermission: String = "<red>You got vanished but you don't have permission to use vanish <gray>(<permission>)</gray>. your vanish status will not be persistent between sessions.",
        val noPermissionToKeepVanished: String = "<red>You don't have <gray><permission></gray> permission to keep your vanish status.",
    )

    @ConfigSerializable
    data class Paste(
        val use: String = "<gray>Your paste key is <gold><key></gold>, <red>Make sure to check the content before sharing it with others and remove the data you don't want to share. <white><click:open_url:'https://pastes.dev/<key>'>(Click to open in pastes.dev)</click>",
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
            return LanguageConfig().also { it.save() }
        }

        @JvmStatic
        fun fromConfig(): LanguageConfig? {
            return fromConfig<LanguageConfig>(File(languageDirectory, "${SettingsConfig.get().general.language}.yml"))
        }
    }
}