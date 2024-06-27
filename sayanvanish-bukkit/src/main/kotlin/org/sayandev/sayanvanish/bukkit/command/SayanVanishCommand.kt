package org.sayandev.sayanvanish.bukkit.command

import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.database.DatabaseConfig
import org.sayandev.sayanvanish.api.database.databaseConfig
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.utils.Paste
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.LanguageConfig
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureLevel
import org.sayandev.sayanvanish.bukkit.utils.ServerUtils
import org.sayandev.stickynote.bukkit.command.StickyCommand
import org.sayandev.stickynote.bukkit.command.StickySender
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.bukkit.warn
import org.sayandev.stickynote.core.utils.MilliCounter
import org.sayandev.stickynote.lib.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.sayandev.stickynote.lib.incendo.cloud.component.CommandComponent
import org.sayandev.stickynote.lib.incendo.cloud.component.DefaultValue
import org.sayandev.stickynote.lib.incendo.cloud.parser.flag.CommandFlag
import org.sayandev.stickynote.lib.incendo.cloud.parser.standard.IntegerParser
import org.sayandev.stickynote.lib.incendo.cloud.parser.standard.StringParser
import org.sayandev.stickynote.lib.incendo.cloud.suggestion.Suggestion
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.reflect.full.memberProperties

class SayanVanishCommand : StickyCommand("sayanvanish", "vanish", "v") {

    private val command = manager.commandBuilder(this.name, *aliases)
        .permission(constructBasePermission("vanish"))
        .optional("player", OfflinePlayerParser.offlinePlayerParser())
        .flag(
            CommandFlag.builder<StickySender>("state").withComponent(
                CommandComponent.builder<StickySender, String>("state", StringParser.stringParser())
                    .suggestionProvider { _, _ ->
                        CompletableFuture.completedFuture(listOf("on", "off").map { Suggestion.suggestion(it) })
                    })
        )
        .flag(CommandFlag.builder<StickySender?>("silent").withAliases("s"))
        .handler { context ->
            val sender = context.sender().bukkitSender()
            val target = context.optional<OfflinePlayer>("player")
            val state = context.flags().get<String>("state")

            if (!target.isPresent && sender !is Player) {
                sender.sendMessage(language.general.haveToProvidePlayer.component())
                return@handler
            }

            if (target.isPresent && !sender.hasPermission(Permission.VANISH_OTHERS.permission())) {
                sender.sendMessage(language.general.dontHavePermission.component())
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

                                val featurePastes = mutableMapOf<String, List<String>>()
                                for (feature in Features.features()) {
                                    featurePastes[feature.id] = feature.file.readLines()
                                }
                                Paste("yaml", featurePastes.map { "${it.key}:\n     ${it.value.joinToString("\n     ")}" }).post().whenComplete { featureKey, featureError ->
                                    sendPasteError(sender, featureError)
                                    generateMainPaste(sender, mapOf(
                                        "settings.yml" to "${Paste.PASTE_URL}/$settingsKey",
                                        "latest.log" to "${Paste.PASTE_URL}/$logKey",
                                        "features" to "${Paste.PASTE_URL}/$featureKey"
                                    ))
                                }
                                sendPasteError(sender, logError)
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
                    feature.disable()
                }
                Features.features.clear()
                RegisteredFeatureHandler.process()
                settings = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()
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

                if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
                    sender.sendMessage(language.feature.permissionLevelMethodWarning.component(Placeholder.unparsed("method", FeatureLevel.LevelMethod.PERMISSION.name), Placeholder.unparsed("methods", FeatureLevel.LevelMethod.entries.joinToString(", ") { it.name })))
                    return@handler
                }

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

        val featureLiteral = builder
            .literal("feature")
            .permission(constructBasePermission("feature"))
            .required(
                "feature",
                CommandComponent.builder<StickySender, String>("state", StringParser.stringParser())
                    .suggestionProvider { _, _ ->
                        CompletableFuture.completedFuture(Features.features.map { Suggestion.suggestion(it.id) })
                    }
            )

        manager.command(featureLiteral
            .literal("disable")
            .permission(constructBasePermission("feature.disable"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendMessage(language.feature.notFound.component())
                    return@handler
                }

                if (!feature.enabled) {
                    sender.sendMessage(language.feature.alreadyDisabled.component(Placeholder.unparsed("feature", feature.id)))
                    return@handler
                }

                feature.disable()
                feature.save()
                sender.sendMessage(language.feature.disabled.component(Placeholder.unparsed("feature", feature.id)))
            }
            .build())

        manager.command(featureLiteral
            .literal("enable")
            .permission(constructBasePermission("feature.enable"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendMessage(language.feature.notFound.component())
                    return@handler
                }

                if (feature.enabled) {
                    sender.sendMessage(language.feature.alreadyEnabled.component(Placeholder.unparsed("feature", feature.id)))
                    return@handler
                }

                feature.enable()
                feature.save()
                sender.sendMessage(language.feature.enabled.component(Placeholder.unparsed("feature", feature.id)))
            }
            .build())

        manager.command(featureLiteral
            .literal("reset")
            .permission(constructBasePermission("feature.reset"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendMessage(language.feature.notFound.component())
                    return@handler
                }

                feature.disable()
                Features.features.remove(feature)
                val freshFeature = feature::class.java.getDeclaredConstructor().newInstance()
                if (freshFeature.enabled) {
                    freshFeature.enable()
                }
                freshFeature.save()
                Features.features.add(freshFeature)
                sender.sendMessage(language.feature.reset.component(Placeholder.unparsed("feature", feature.id)))
            }
            .build())

        manager.command(featureLiteral
            .literal("update")
            .permission(constructBasePermission("feature.update"))
            .required(
                "option",
                CommandComponent.builder<StickySender, String>("state", StringParser.stringParser())
                    .suggestionProvider { context, _ ->
                        val feature = Features.features.find { it.id == context.get<String>("feature") } ?: return@suggestionProvider CompletableFuture.completedFuture(emptyList())
                        CompletableFuture.completedFuture(feature::class.java.declaredFields.filter { it.isAnnotationPresent(Configurable::class.java) }.map { Suggestion.suggestion(it.name) })
                    }
            )
            .required("value",
                CommandComponent.builder<StickySender, String>("value", StringParser.stringParser(StringParser.StringMode.QUOTED))
                    .suggestionProvider { context, _ ->
                        val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                            return@suggestionProvider CompletableFuture.completedFuture(emptyList())
                        }

                        val field = feature::class.java.declaredFields.find { it.name == context.get("option") } ?: let {
                            return@suggestionProvider CompletableFuture.completedFuture(emptyList())
                        }
                        field.isAccessible = true

                        when (field.type) {
                            Boolean::class.java -> CompletableFuture.completedFuture(listOf("true", "false").map { Suggestion.suggestion(it) })
                            Int::class.java -> CompletableFuture.completedFuture(listOf("0", "1", "2", "3", "4", "5").map { Suggestion.suggestion(it) })
                            Double::class.java -> CompletableFuture.completedFuture(listOf("0.0", "0.1", "0.2", "0.3", "0.4", "0.5").map { Suggestion.suggestion(it) })
                            Float::class.java -> CompletableFuture.completedFuture(listOf("0.0", "0.1", "0.2", "0.3", "0.4", "0.5").map { Suggestion.suggestion(it) })
                            else -> CompletableFuture.completedFuture(listOf(field.get(feature).toString()).map { Suggestion.suggestion(it) })
                        }
                    }
                )
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendMessage(language.feature.notFound.component())
                    return@handler
                }

                val field = feature::class.java.declaredFields.find { it.name == context.get("option") }
                if (field == null) {
                    sender.sendMessage(language.feature.invalidOption.component(Placeholder.unparsed("options", feature::class.memberProperties.joinToString(", ") { it.name })))
                    return@handler
                }

                val value = context.get<String>("value")
                field.isAccessible = true
                try {
                    if (value.toDoubleOrNull() != null) {
                        val parsedValue = value.toDouble()
                        if (field.type == Int::class.java) {
                            field.set(feature, parsedValue.toInt())
                        } else if (field.type == Float::class.java) {
                            field.set(feature, parsedValue.toFloat())
                        } else {
                            field.set(feature, parsedValue)
                        }
                    } else {
                        if (value.toBooleanStrictOrNull() != null) {
                            field.set(feature, value.toBoolean())
                        } else {
                            field.set(feature, value)
                        }
                    }
                } catch (_: Exception) {
                    sender.sendMessage(language.feature.invalidValue.component(Placeholder.unparsed("values", field.type.simpleName ?: "N/A")))
                    return@handler
                }
                feature.save()

                sender.sendMessage(language.feature.updated.component(Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("option", field.name), Placeholder.unparsed("state", value)))
            }
            .build())

        val testLiteral = builder
            .literal("test")
            .permission(constructBasePermission("test"))

        manager.command(testLiteral
            .literal("database")
            .permission(constructBasePermission("test.database"))
            .optional("amount", IntegerParser.integerParser(1, 10000), DefaultValue.constant(100))
            .optional("tries", IntegerParser.integerParser(1, 10), DefaultValue.constant(5))
            .flag(CommandFlag.builder("no-cache"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                val amount = context.get<Int>("amount")
                val database = SayanVanishAPI.getInstance().database
                if (context.flags().hasFlag("no-cache")) {
                    database.useCache = false
                }

                repeat(context.get("tries")) {
                    val counter = MilliCounter()
                    counter.start()
                    sender.sendMessage("<gold>[${it + 1}] <gray>Trying <green>${amount} Get Users</green> from data storage".component())
                    repeat(amount) {
                        database.getUsers()
                    }
                    counter.stop()
                    sender.sendMessage("<gold>[${it + 1}] <gray>Took <green>${counter.get()}ms</green>".component())
                }

                database.useCache = true
            }
            .build())

        manager.command(testLiteral
            .literal("performance")
            .permission(constructBasePermission("test.performance"))
            .handler { context ->
                val sender = context.sender().bukkitSender()
                /*val spark = Features.getFeature<FeatureHookSpark>().hook?.spark ?: let {
                    sender.sendMessage("<red><gold>spark</gold> is not installed on this server.".component())
                    return@handler
                }

                spark.gc()?.values?.forEach { gc ->
                    gc.
                }*/
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