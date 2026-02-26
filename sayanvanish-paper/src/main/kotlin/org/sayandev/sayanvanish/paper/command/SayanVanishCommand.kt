package org.sayandev.sayanvanish.paper.command

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.bukkit.parser.PlayerParser
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.setting.ManagerSetting
import org.incendo.cloud.suggestion.Suggestion
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.command.FeatureParser
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.sayanvanish.api.message.MessageConfig
import org.sayandev.sayanvanish.api.utils.Paste
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getOrAddVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getOrCreateVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.user
import org.sayandev.sayanvanish.paper.config.LanguageConfig
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.features.FeatureLevel
import org.sayandev.sayanvanish.paper.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.paper.utils.PlayerUtils.sendPrefixComponent
import org.sayandev.sayanvanish.paper.utils.ServerUtils
import org.sayandev.stickynote.bukkit.*
import org.sayandev.stickynote.bukkit.command.BukkitCommand
import org.sayandev.stickynote.bukkit.command.BukkitSender
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.core.utils.MilliCounter
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class SayanVanishCommand : BukkitCommand(Settings.get().vanishCommand.name, *Settings.get().vanishCommand.aliases.toTypedArray()) {

    override fun rootBuilder(builder: MutableCommandBuilder<BukkitSender>) {
        builder.permission("${plugin.name}.commands.use")
        builder.optional("player", OfflinePlayerParser.offlinePlayerParser())
        builder.flag(
            "state",
            emptyArray(),
            Description.empty(),
            CommandComponent.builder<BukkitSender, String>("state", StringParser.stringParser())
                .suggestionProvider { _, _ ->
                    CompletableFuture.completedFuture(listOf("on", "off").map { Suggestion.suggestion(it) })
                }
        )
        builder.flag("silent", arrayOf("s"))
    }

    override fun rootHandler(context: CommandContext<BukkitSender>) {
        val sender = context.sender().platformSender()
        val target = context.optional<OfflinePlayer>("player")
        val state = context.flags().get<String>("state")

        if (!target.isPresent && sender !is Player) {
            sender.sendPrefixComponent(language.general.haveToProvidePlayer)
            return
        }

        if (target.isPresent && !sender.hasPermission(Permissions.VANISH_OTHERS.permission())) {
            sender.sendPrefixComponent(language.general.dontHavePermission)
            return
        }

        val player = if (target.isPresent) context.optional<OfflinePlayer>("player").get() else context.sender().player() ?: return
        launch {
            val user = player.getOrCreateVanishUser()

            if (!user.hasPermission(Permissions.VANISH)) {
                sender.sendPrefixComponent(language.vanish.dontHaveUsePermission.component(Placeholder.unparsed("permission", Permissions.VANISH.permission())))
            }

            val options = VanishOptions.defaultOptions().apply {
                if (context.flags().hasFlag("silent")) {
                    this.sendMessage = false
                }
            }

            if (target.isPresent) {
                if (!player.isOnline) {
                    sender.sendPrefixComponent(language.vanish.offlineOnVanish, Placeholder.unparsed("player", player.name ?: "N/A"), Placeholder.parsed("state", user.stateText()))
                    options.sendMessage = false
                }
            }

            when (state) {
                "on" -> user.disappear(options)
                "off" -> user.appear(options)
                else -> user.toggleVanish(options)
            }
        }
    }

    init {
        manager.settings().set(ManagerSetting.OVERRIDE_EXISTING_COMMANDS, true)

        var forceUpdateConfirm = false
        rawCommandBuilder().registerCopy {
            literalWithPermission("forceupdate")
            handler { context ->
                val sender = context.sender().platformSender()
                if (!forceUpdateConfirm) {
                    sender.sendPrefixComponent(language.general.confirmUpdate)
                    forceUpdateConfirm = true
                    runSync({
                        forceUpdateConfirm = false
                    }, 100)
                    return@handler
                }

                sender.sendPrefixComponent(language.general.updating)

                runAsync {
                    val updateFeature = Features.getFeature<FeatureUpdate>()
                    updateFeature.updatePlugin().whenComplete { isSuccessful, error ->
                        error?.printStackTrace()

                        runSync {
                            if (isSuccessful) {
                                sender.sendPrefixComponent(language.general.updated, Placeholder.unparsed("version", updateFeature.latestVersion()))
                                if (Settings.get().general.proxyMode && updateFeature.willAffectProxy()) {
                                    sender.sendPrefixComponent(language.general.proxyUpdateWarning)
                                }
                            } else {
                                sender.sendPrefixComponent(language.general.updateFailed)
                            }
                        }
                    }
                }
            }
        }

        rawCommandBuilder().registerCopy {
            literalWithPermission("paste")
            suspendingHandler { context ->
                val sender = context.sender().platformSender()
                sender.sendPrefixComponent(language.paste.generating)
                async {
                    try {
                        val blockedWords = listOf(
                            "host",
                            "port",
                            "database",
                            "username",
                            "password",
                        )
                        val databaseKey = Paste("yaml", StorageConfig.file.readLines().filter { !blockedWords.any { blockedWord -> it.contains(blockedWord) } }).post().await()
                        val settingsKey = Paste("yaml", Settings.settingsFile.readLines()).post().await()
                        val latestLogFile = File(File(pluginDirectory.parentFile.parentFile, "logs"), "latest.log")
                        if (latestLogFile.exists()) {
                            val logKey = Paste("log", latestLogFile.readLines()).post().await()

                            val featurePastes = mutableMapOf<String, List<String>>()
                            for (feature in Features.features()) {
                                featurePastes[feature.id] = File(Feature.directory(feature.category), "${feature.id}.yml").readLines()
                            }
                            val featureKey = Paste("yaml", featurePastes.map { "${it.key}:\n     ${it.value.joinToString("\n     ")}" }).post().await()
                            generateMainPaste(sender, mapOf(
                                "storage.yml" to Paste.url(databaseKey),
                                "settings.yml" to Paste.url(settingsKey),
                                "latest.log" to Paste.url(logKey),
                                "features" to Paste.url(featureKey)
                            ))
                        } else {
                            generateMainPaste(sender, mapOf("settings.yml" to Paste.url(settingsKey)))
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        sender.sendPrefixComponent(language.paste.failedToGenerate)
                    }
                }
            }
        }

        rawCommandBuilder().registerCopy {
            literalWithPermission("reload")
            handler { context ->
                val sender = context.sender().platformSender()
                Features.features().forEach { feature ->
                    feature.disable(true)
                    if (feature::class.java.isAssignableFrom(Listener::class.java)) {
                        unregisterListener(feature as Listener)
                    }
                }
                Features.clearFeatures()
                Features.resetAllUserFeatureStates()
                Settings.reload()
                StorageConfig.reload()
                MessageConfig.reload()
                SayanVanishAPI.reloadMessaging(Settings.get().general.proxyMode)
                language = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()
                RegisteredFeatureHandler.process()
                sender.sendPrefixComponent(language.general.reloaded)
            }
        }

        val levelLiteral = rawCommandBuilder().registerCopy {
            literalWithPermission("level")
        }

        levelLiteral.registerCopy {
            literalWithPermission("set")
            required("player", OfflinePlayerParser.offlinePlayerParser())
            required("level", IntegerParser.integerParser(0))
            suspendingHandler { context ->
                val sender = context.sender().platformSender()
                val target = context.get<OfflinePlayer>("player")

                if (!target.hasPlayedBefore()) {
                    sender.sendPrefixComponent(language.general.playerNotFound)
                    return@suspendingHandler
                }

                val user = target.getOrAddVanishUser()
                user.vanishLevel = context.get("level")
                user.saveAndSync()

                if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
                    sender.sendPrefixComponent(language.feature.permissionLevelMethodWarning, Placeholder.unparsed("method", FeatureLevel.LevelMethod.PERMISSION.name), Placeholder.unparsed("methods", FeatureLevel.LevelMethod.entries.joinToString(", ") { it.name }))
                    return@suspendingHandler
                }

                sender.sendPrefixComponent(language.vanish.levelSet, Placeholder.unparsed("level", user.vanishLevel.toString()), Placeholder.unparsed("player", user.username))
            }
        }

        levelLiteral.registerCopy {
            literalWithPermission("get")
            required("player", OfflinePlayerParser.offlinePlayerParser())
            suspendingHandler { context ->
                val sender = context.sender().platformSender()
                val target = context.get<OfflinePlayer>("player")

                if (!target.hasPlayedBefore()) {
                    sender.sendPrefixComponent(language.general.playerNotFound)
                    return@suspendingHandler
                }

                val user = target.getOrCreateVanishUser()

                sender.sendPrefixComponent(language.vanish.levelGet, Placeholder.unparsed("player", target.name ?: "N/A"), Placeholder.unparsed("level", user.vanishLevel.toString()))
            }
        }

        val featureLiteral = rawCommandBuilder().registerCopy {
            literalWithPermission("feature")
            required("feature", FeatureParser.featureParser())
        }

        val togglePlayerLiteral = featureLiteral.registerCopy {
            literalWithPermission("toggleplayer")
            optional("player", PlayerParser.playerParser())
            suspendingHandler { context ->
                val targetArg = context.optional<Player>("player").getOrNull()

                val sender = context.sender().platformSender()
                if (targetArg != null && !sender.hasPermission(Permissions.FEATURE_PLAYER_TOGGLE.permission())) {
                    sender.sendPrefixComponent(language.feature.togglePlayerOther)
                    return@suspendingHandler
                }

                if (targetArg == null && sender !is Player) {
                    sender.sendPrefixComponent(language.general.haveToProvidePlayer)
                    return@suspendingHandler
                }

                val target = targetArg ?: sender as Player

                val feature = context.get<Feature>("feature")

                val user = target.user().await() ?: let {
                    sender.sendPrefixComponent(language.general.userNotFound, Placeholder.unparsed("player", target.name))
                    return@suspendingHandler
                }

                val currentlyEnabled = Features.isFeatureEnabled(user, feature)
                Features.setFeatureEnabled(user, feature, !currentlyEnabled)
                sender.sendPrefixComponent(language.feature.togglePlayer, Placeholder.unparsed("player", target.name), Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("state", if (!currentlyEnabled) "enabled" else "disabled"))
                return@suspendingHandler
            }
        }
        manager.command(manager.commandBuilder("togglefeature").proxies(togglePlayerLiteral.build()))

        featureLiteral.registerCopy {
            literalWithPermission("disable")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = context.get<Feature>("feature")

                if (!feature.enabled) {
                    sender.sendPrefixComponent(language.feature.alreadyDisabled, Placeholder.unparsed("feature", feature.id))
                    return@handler
                }

                feature.disable()
                feature.save()
                sender.sendPrefixComponent(language.feature.disabled, Placeholder.unparsed("feature", feature.id))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("enable")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = context.get<Feature>("feature")

                if (feature.enabled) {
                    sender.sendPrefixComponent(language.feature.alreadyEnabled, Placeholder.unparsed("feature", feature.id))
                    return@handler
                }

                feature.enable()
                feature.save()
                sender.sendPrefixComponent(language.feature.enabled, Placeholder.unparsed("feature", feature.id))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("reset")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = context.get<Feature>("feature")

                feature.disable()
                Features.removeFeature(feature)
                val freshFeature = feature::class.java.getDeclaredConstructor().newInstance()
                if (freshFeature.enabled) {
                    freshFeature.enable()
                }
                freshFeature.save()
                Features.addFeature(freshFeature)
                sender.sendPrefixComponent(language.feature.reset, Placeholder.unparsed("feature", feature.id))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("status")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = context.get<Feature>("feature")

                sender.sendPrefixComponent(language.feature.status, Placeholder.unparsed("feature", feature.id), Placeholder.parsed("status", if (feature.enabled) "<green>Enabled</green>" else "<red>Disabled</red>"))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("update")
            required(CommandComponent.builder<BukkitSender, String>("option", StringParser.stringParser())
                .suggestionProvider { context, _ ->
                    val feature = context.get<Feature>("feature")
                    CompletableFuture.completedFuture(feature::class.java.declaredFields.filter { it.isAnnotationPresent(Configurable::class.java) }.map { Suggestion.suggestion(it.name) })
                })
            required("value", StringParser.stringParser(StringParser.StringMode.QUOTED))
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = context.get<Feature>("feature")

                val field = feature.javaClass.getDeclaredField(context.get("option"))
                if (field == null) {
                    sender.sendPrefixComponent(language.feature.invalidOption, Placeholder.unparsed("options", feature::class.memberProperties.joinToString(", ") { it.name }))
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
                    sender.sendPrefixComponent(language.feature.invalidValue, Placeholder.unparsed("values", field.type.simpleName ?: "N/A"))
                    return@handler
                }
                feature.save()

                sender.sendPrefixComponent(language.feature.updated, Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("option", field.name), Placeholder.unparsed("state", value))
            }
        }

        val testLiteral = rawCommandBuilder().registerCopy {
            literalWithPermission("test")
        }

        testLiteral.registerCopy {
            literalWithPermission("users")
            suspendingHandler { context ->
                // TODO: better implementation?
                val sender = context.sender().platformSender()
                sender.sendPrefixComponent("<gray>Fetching vanish users from database...")
                sender.sendPrefixComponent("<green>Database Vanished Users: <yellow>${VanishAPI.get().getDatabase().getVanishUsers().await().filter { it.isVanished }.map { it.username }}")
                sender.sendPrefixComponent("<green>Cache Vanished Users: <yellow>${VanishAPI.get().getCacheService().getVanishUsers().values.map { it.username }}")
            }
        }

        val testDatabaseLiteral = testLiteral.registerCopy {
            literalWithPermission("database")
        }

        testDatabaseLiteral.registerCopy {
            literalWithPermission("data")
            optional("limit", IntegerParser.integerParser(1, 10000))
            suspendingHandler { context ->
                // TODO: better implementation?
                val sender = context.sender().platformSender()
                val limit = context.get<Int>("limit")
                val database = VanishAPI.get().getDatabase()

                val counter = MilliCounter()
                counter.start()

                val users = database.getVanishUsers().await()

                val limitedUsers = users.take(limit)

                for ((index, user) in limitedUsers.withIndex()) {
                    val userProperties = user::class.memberProperties
                        .filterIsInstance<KProperty1<Any, *>>()
                        .joinToString(" <green>|</green> ") { prop ->
                            try {
                                "<gray>${prop.name}: <white>${prop.call(user)}"
                            } catch (e: Exception) {
                                "<gray>${prop.name}: <red>Access Error"
                            }
                        }

                    sender.sendPrefixComponent("<gold>[${index + 1}] <gray>$userProperties")
                }
                counter.stop()
                sender.sendPrefixComponent("<gray>Took <green>${counter.get()}ms</green>")
            }
        }

        testDatabaseLiteral.registerCopy {
            literalWithPermission("performance")
            optional("amount", IntegerParser.integerParser(1, 10000))
            optional("tries", IntegerParser.integerParser(1, 10))
            suspendingHandler { context ->
                // TODO: better implementation?
                val sender = context.sender().platformSender()
                val amount = context.get<Int>("amount")
                val database = VanishAPI.get().getDatabase()

                repeat(context.get("tries")) {
                    val counter = MilliCounter()
                    counter.start()
                    sender.sendPrefixComponent("<gold>[${it + 1}] <gray>Trying <green>${amount} Get Users</green> from data storage")
                    repeat(amount) {
                        database.getVanishUsers().await()
                    }
                    counter.stop()
                    sender.sendPrefixComponent("<gold>[${it + 1}] <gray>Took <green>${counter.get()}ms</green>")
                }
            }
        }

    }

    private suspend fun generateMainPaste(sender: CommandSender, otherKeys: Map<String, String>) {
        val key = Paste("json", listOf(ServerUtils.getServerData(
            mutableMapOf(
                "database-type" to StorageConfig.get().method.toString(),
            ).apply {
                this.putAll(otherKeys)
            }
        ))).post().await()
        val pasteKey = key.ifEmpty { "N/A" }
        val pasteUrl = if (pasteKey == "N/A") "N/A" else Paste.url(pasteKey)
        sender.sendPrefixComponent(
            language.paste.use
                .replace("https://pastes.dev/<key>", pasteUrl)
                .replace("<url>", pasteUrl)
                .replace("<key>", pasteKey)
        )
    }
}
