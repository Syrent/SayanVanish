package org.sayandev.sayanvanish.bukkit.command

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.database.DatabaseConfig
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.utils.Paste
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.LanguageConfig
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.utils.ServerUtils
import org.sayandev.stickynote.bukkit.NMSUtils
import org.sayandev.stickynote.bukkit.PacketUtils
import org.sayandev.stickynote.bukkit.command.StickyCommand
import org.sayandev.stickynote.bukkit.command.interfaces.SenderExtension
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.core.math.Vector3
import org.sayandev.stickynote.lib.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.sayandev.stickynote.lib.incendo.cloud.component.CommandComponent
import org.sayandev.stickynote.lib.incendo.cloud.parser.flag.CommandFlag
import org.sayandev.stickynote.lib.incendo.cloud.parser.standard.IntegerParser
import org.sayandev.stickynote.lib.incendo.cloud.parser.standard.StringParser
import org.sayandev.stickynote.lib.incendo.cloud.suggestion.Suggestion
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.io.File
import java.util.concurrent.CompletableFuture

class SayanVanishCommand : StickyCommand("sayanvanish", "vanish", "v") {

    val command = manager.commandBuilder(this.name, *aliases)
        .permission(constructBasePermission("vanish"))
        .optional("player", OfflinePlayerParser.offlinePlayerParser())
        .flag(
            CommandFlag.builder<SenderExtension?>("state").withComponent(
                CommandComponent.builder<SenderExtension, String>("state", StringParser.stringParser())
                    .suggestionProvider { _, _ ->
                        CompletableFuture.completedFuture(listOf("on", "off").map { Suggestion.suggestion(it) })
                    })
        )
        .flag(CommandFlag.builder<SenderExtension?>("silent").withAliases("s"))
        .handler { context ->
            val sender = context.sender().bukkitSender()
            val target = context.optional<OfflinePlayer>("player")
            val state = context.flags().get<String>("state")

            if (!target.isPresent && sender !is Player) {
                sender.sendMessage("<red>You have to provide a player".component())
                return@handler
            }

            val player = if (target.isPresent) context.optional<OfflinePlayer>("player").get() else context.sender().player() ?: return@handler
            val user = player.getOrAddUser()

            val options = VanishOptions.defaultOptions().apply {
                if (context.flags().hasFlag("silent")) {
                    this.sendMessage = false
                }
            }

            when (state) {
                "on" -> user.vanish(options)
                "off" -> user.unVanish(options)
                else -> user.toggleVanish(options)
            }

            if (target.isPresent) {
                if (!player.isOnline) {
                    sender.sendMessage(language.vanish.offlineOnVanish.component(Placeholder.unparsed("player", player.name ?: "N/A"), Placeholder.parsed("state", user.stateText())))
                }
            }
        }

    init {
        manager.command(command.build())

        manager.command(builder
            .literal("help")
            .permission(constructBasePermission("help"))
            .handler { context ->
                help.queryCommands("$name ${context.getOrDefault("query", "")}", context.sender())
            }
            .build())

        manager.command(builder
            .literal("paste")
            .permission(constructBasePermission("paste"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                sender.sendMessage(language.paste.generating.component())
                runAsync {
                    Paste("yaml", SettingsConfig.settingsFile.readLines()).post().whenComplete { settingsKey, settingsError ->
                        sendPasteError(sender, settingsError)

                        val latestLogFile = File(File(pluginDirectory.parentFile.parentFile, "logs"), "latest.log")
                        if (latestLogFile.exists()) {
                            Paste("log", latestLogFile.readLines()).post().whenComplete { logKey, logError ->
                                sendPasteError(sender, logError)
                                generateMainPaste(sender, mapOf("settings.yml" to "${Paste.PASTE_URL}/$settingsKey", "latest.log" to "${Paste.PASTE_URL}/$logKey"))
                            }
                        } else {
                            generateMainPaste(sender, mapOf("settings.yml" to "${Paste.PASTE_URL}/$settingsKey"))
                        }
                    }
                }
            }
            .build())

        manager.command(builder
            .literal("reload")
            .permission(constructBasePermission("reload"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                language = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()
                Features.features.forEach { feature ->
                    feature.enabled = false
                    feature.disable()
                }
                Features.features.clear()
                RegisteredFeatureHandler.process()
                /*settings.vanish.features.forEach { feature ->
                    feature.enabled = false
                    feature.disable()
                }
                settings.vanish.features.clear()*/
                settings = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()
                /*settings.vanish.features.forEach {
                    if (it.enabled) it.enable()
                }*/
                databaseConfig = DatabaseConfig.fromConfig() ?: DatabaseConfig.defaultConfig()
                sender.sendMessage(language.general.reloaded.component())
            }
            .build())

        val levelLiteral = builder
            .literal("level")
            .permission(constructBasePermission("level"))

        manager.command(levelLiteral
            .literal("set")
            .permission(constructBasePermission("level.set"))
            .required("player", OfflinePlayerParser.offlinePlayerParser())
            .required("level", IntegerParser.integerParser(0))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val target = context.get<OfflinePlayer>("player")

                if (!target.hasPlayedBefore()) {
                    sender.sendMessage(language.general.playerNotFound.component())
                    return@handler
                }

                val user = target.getOrAddUser()
                user.vanishLevel = context.get("level")
                user.save()

                sender.sendMessage(language.vanish.levelSet.component(Placeholder.unparsed("level", user.vanishLevel.toString()), Placeholder.unparsed("player", user.username)))
            }
            .build())

        manager.command(levelLiteral
            .literal("get")
            .permission(constructBasePermission("level.get"))
            .required("player", OfflinePlayerParser.offlinePlayerParser())
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val target = context.get<OfflinePlayer>("player")

                if (!target.hasPlayedBefore()) {
                    sender.sendMessage(language.general.playerNotFound.component())
                    return@handler
                }

                val user = target.user()

                sender.sendMessage(language.vanish.levelGet.component(Placeholder.unparsed("player", target.name ?: "N/A"), Placeholder.unparsed("level", (user?.vanishLevel ?: 0).toString())))
            }
            .build())
    }

    private fun sendPasteError(sender: CommandSender, error: Throwable?) {
        if (error != null) {
            runSync {
                sender.sendMessage(language.paste.failedToGenerate.component())
            }
            error.printStackTrace()
        }
    }

    private fun generateMainPaste(sender: CommandSender, otherKeys: Map<String, String>) {
        Paste("json", listOf(ServerUtils.getServerData(
            mutableMapOf(
                "database-type" to databaseConfig.method.toString(),
            ).apply {
                this.putAll(otherKeys)
            }
        ))
        ).post().whenComplete { key, generalError ->
            sendPasteError(sender, generalError)

            runSync {
                sender.sendMessage(language.paste.use.replace("<key>", key ?: "N/A").component())
            }
        }
    }
}