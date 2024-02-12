package ir.syrent.velocityvanish.spigot.storage

import com.cryptomorin.xseries.XSound
import ir.syrent.velocityvanish.spigot.configuration.YamlConfig
import ir.syrent.velocityvanish.spigot.hook.DependencyManager
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityvanish.utils.TextReplacement
import ir.syrent.velocityvanish.utils.component
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import java.io.File
import java.nio.file.Files
import java.time.LocalDate

object Settings {

    const val latestSettingsConfigVersion = 9

    lateinit var settings: YamlConfig
    lateinit var language: YamlConfig
    private lateinit var settingsConfig: FileConfiguration
    private lateinit var languageConfig: FileConfiguration

    private val messages = mutableMapOf<Message, String>()

    var settingsConfigVersion = 1

    lateinit var defaultLanguage: String
    var velocitySupport = true

    var showDependencySuggestions = true
    var supportLegacyColorCodes = false
    var forceVanishIfFirst = true
    var bstats = true

    var commandSound: Sound? = null
    var vanishSound: Sound? = null
    var unVanishSound: Sound? = null

    var actionbar = true
    var remember = false
    var seeAsSpectator = true
    var invincible = true
    var silentOpenContainer = true
    var fakeJoinLeaveMessage = true
    var disableCollision = false

    var forcePreventPrivateMessages = true

    var preventPickup = true
    var preventBlockBreak = false
    var preventBlockPlace = false
    var preventInteract = false
    var preventAdvancement = false

    init {
        load()
    }

    fun load() {
        settings = YamlConfig(Ruom.plugin.dataFolder, "settings.yml")
        settingsConfig = settings.config

        settingsConfigVersion = settingsConfig.getInt("config_version", 1)

        if (settingsConfigVersion < latestSettingsConfigVersion) {
            val backupFileName = "settings.yml-bak-${LocalDate.now()}"
            val settingsFile = File(Ruom.plugin.dataFolder, "settings.yml")
            val backupFile = File(Ruom.plugin.dataFolder, backupFileName)
            if (backupFile.exists()) backupFile.delete()
            Files.copy(settingsFile.toPath(), backupFile.toPath())
            settingsFile.delete()
            settings = YamlConfig(Ruom.plugin.dataFolder, "settings.yml")
            settingsConfig = settings.config
            sendBackupMessage(backupFileName)
        }

        defaultLanguage = settingsConfig.getString("default_language") ?: "en_US"
//        velocitySupport = settingsConfig.getBoolean("velocity_support")
        showDependencySuggestions = settingsConfig.getBoolean("show_dependency_suggestions")
        supportLegacyColorCodes = settingsConfig.getBoolean("support_legacy_color_codes")
        forceVanishIfFirst = settingsConfig.getBoolean("force_vanish_if_first")
        bstats = settingsConfig.getBoolean("bstats")

        commandSound = settingsConfig.getString("sounds.command")?.let {
            runCatching { XSound.valueOf(it).parseSound() }.getOrNull()
        }
        vanishSound = settingsConfig.getString("sounds.vanish")?.let {
            runCatching { XSound.valueOf(it).parseSound() }.getOrNull()
        }
        unVanishSound = settingsConfig.getString("sounds.unvanish")?.let {
            runCatching { XSound.valueOf(it).parseSound() }.getOrNull()
        }

        actionbar = settingsConfig.getBoolean("vanish.actionbar")
        remember = settingsConfig.getBoolean("vanish.remember")
        seeAsSpectator = settingsConfig.getBoolean("vanish.see_as_spectator")
        invincible = settingsConfig.getBoolean("vanish.invincible")
        silentOpenContainer = settingsConfig.getBoolean("vanish.silent_open_container")
        fakeJoinLeaveMessage = settingsConfig.getBoolean("vanish.fake_join_leave_message")
        disableCollision = settingsConfig.getBoolean("vanish.disable_collision")

        preventPickup = settingsConfig.getBoolean("vanish.prevent.pickup")
        preventBlockBreak = settingsConfig.getBoolean("vanish.prevent.block_break")
        preventBlockPlace = settingsConfig.getBoolean("vanish.prevent.block_place")
        preventInteract = settingsConfig.getBoolean("vanish.prevent.interact")
        preventAdvancement = settingsConfig.getBoolean("vanish.prevent.advancement")

        forcePreventPrivateMessages = settingsConfig.getBoolean("hooks.essentials.force_prevent_private_messages")

        language = YamlConfig(Ruom.plugin.dataFolder, "languages/$defaultLanguage.yml")
        languageConfig = language.config

        messages.apply {
            this.clear()
            for (message in Message.values()) {
                if (message == Message.EMPTY) {
                    this[message] = ""
                    continue
                }

                this[message] = languageConfig.getString(message.path) ?: languageConfig.getString(Message.UNKNOWN_MESSAGE.path) ?: "Cannot find message: ${message.name}"
            }
        }

        settings.saveConfig()
        settings.reloadConfig()
        language.saveConfig()
        language.reloadConfig()
    }


    fun formatMessage(message: String, placeholderTarget: Player, vararg replacements: TextReplacement): String {
        var formattedMessage = formatMessage(message, *replacements)
        if (DependencyManager.placeholderAPIHook.exists) {
            formattedMessage = PlaceholderAPI.setPlaceholders(placeholderTarget, formattedMessage)
        }
        return formattedMessage
    }


    fun formatMessage(message: String, vararg replacements: TextReplacement): String {
        var formattedMessage = message
            .replace("\$prefix", getMessage(Message.PREFIX))
            .replace("\$successful_prefix", getMessage(Message.SUCCESSFUL_PREFIX))
            .replace("\$warn_prefix", getMessage(Message.WARN_PREFIX))
            .replace("\$error_prefix", getMessage(Message.ERROR_PREFIX))
        for (replacement in replacements) {
            formattedMessage = formattedMessage.replace("\$${replacement.from}", replacement.to)
        }
        return formattedMessage
    }

    fun formatMessage(player: Player, message: Message, placeholderTarget: Player, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), placeholderTarget, *replacements)
    }

    fun formatMessage(message: Message, vararg replacements: TextReplacement): String {
        return formatMessage(getMessage(message), *replacements)
    }

    fun formatMessage(messages: List<String>, vararg replacements: TextReplacement): List<String> {
        val messageList = mutableListOf<String>()
        for (message in messages) {
            messageList.add(formatMessage(message, *replacements))
        }

        return messageList
    }

    private fun getMessage(message: Message): String {
        return messages[message] ?: messages[Message.UNKNOWN_MESSAGE]?.replace(
            "\$error_prefix",
            messages[Message.ERROR_PREFIX] ?: ""
        ) ?: "Unknown message ($message)"
    }

    fun getConsolePrefix(): String {
        return getMessage(Message.CONSOLE_PREFIX)
    }

    private fun sendBackupMessage(fileName: String) {
        AdventureApi.get().console().sendMessage("<red>=============================================================".component())
        AdventureApi.get().console().sendMessage("<red>Config version updated to $latestSettingsConfigVersion. Please set your preferred values again.".component())
        AdventureApi.get().console().sendMessage("<gray>Previous values are still accessible via $fileName in plugin folder.".component())
        AdventureApi.get().console().sendMessage("<red>=============================================================".component())
    }
}