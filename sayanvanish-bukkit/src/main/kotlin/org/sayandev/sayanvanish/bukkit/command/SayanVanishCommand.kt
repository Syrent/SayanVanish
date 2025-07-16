package org.sayandev.sayanvanish.bukkit.command

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
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.sayanvanish.api.storage.storageConfig
import org.sayandev.sayanvanish.api.utils.Paste
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddVanishUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrCreateUser
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.LanguageConfig
import org.sayandev.sayanvanish.bukkit.config.SettingsConfig
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureLevel
import org.sayandev.sayanvanish.bukkit.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendComponent
import org.sayandev.sayanvanish.bukkit.utils.ServerUtils
import org.sayandev.stickynote.bukkit.*
import org.sayandev.stickynote.bukkit.command.BukkitCommand
import org.sayandev.stickynote.bukkit.command.BukkitSender
import org.sayandev.stickynote.bukkit.command.required
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.core.utils.MilliCounter
import java.io.File
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class SayanVanishCommand : BukkitCommand(SettingsConfig.get().vanishCommand.name, *SettingsConfig.get().vanishCommand.aliases.toTypedArray()) {

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
            sender.sendComponent(language.general.haveToProvidePlayer)
            return
        }

        if (target.isPresent && !sender.hasPermission(Permission.VANISH_OTHERS.permission())) {
            sender.sendComponent(language.general.dontHavePermission)
            return
        }

        val player = if (target.isPresent) context.optional<OfflinePlayer>("player").get() else context.sender().player() ?: return
        launch {
            val user = player.getOrAddUser()

            if (!user.hasPermission(Permission.VANISH)) {
                user.sendMessage(language.vanish.dontHaveUsePermission.component(Placeholder.unparsed("permission", Permission.VANISH.permission())))
            }

            val vanishUser = player.getOrAddVanishUser()

            val options = VanishOptions.defaultOptions().apply {
                if (context.flags().hasFlag("silent")) {
                    this.sendMessage = false
                }
            }

            if (target.isPresent) {
                if (!player.isOnline) {
                    sender.sendMessage(language.vanish.offlineOnVanish.component(Placeholder.unparsed("player", player.name ?: "N/A"), Placeholder.parsed("state", user.stateText())))
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
        registerHelpLiteral()

        var forceUpdateConfirm = false
        rawCommandBuilder().registerCopy {
            literalWithPermission("forceupdate")
            handler { context ->
                val sender = context.sender().platformSender()
                if (!forceUpdateConfirm) {
                    sender.sendComponent(language.general.confirmUpdate)
                    forceUpdateConfirm = true
                    runSync({
                        forceUpdateConfirm = false
                    }, 100)
                    return@handler
                }

                sender.sendComponent(language.general.updating)

                runAsync {
                    val updateFeature = Features.getFeature<FeatureUpdate>()
                    updateFeature.updatePlugin().whenComplete { isSuccessful, error ->
                        error?.printStackTrace()

                        runSync {
                            if (isSuccessful) {
                                sender.sendComponent(language.general.updated, Placeholder.unparsed("version", updateFeature.latestVersion()))
                                if (settings.general.proxyMode && updateFeature.willAffectProxy()) {
                                    sender.sendComponent(language.general.proxyUpdateWarning)
                                }
                            } else {
                                sender.sendComponent(language.general.updateFailed)
                            }
                        }
                    }
                }
            }
        }

        rawCommandBuilder().registerCopy {
            literalWithPermission("paste")
            handler { context ->
                val sender = context.sender().platformSender()
                sender.sendComponent(language.paste.generating)
                runAsync {
                    val blockedWords = listOf(
                        "host",
                        "port",
                        "database",
                        "username",
                        "password",
                    )
                    Paste("yaml", storageConfig.file.readLines().filter { !blockedWords.any { blockedWord -> it.contains(blockedWord) } }).post().whenComplete { databaseKey, databaseError ->
                        sendPasteError(sender, databaseError)

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
                                            "database.yml" to "${Paste.PASTE_URL}/$databaseKey",
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
            }
        }

        rawCommandBuilder().registerCopy {
            literalWithPermission("reload")
            handler { context ->
                val sender = context.sender().platformSender()
                language = LanguageConfig.fromConfig() ?: LanguageConfig.defaultConfig()
                Features.features.forEach { feature ->
                    feature.disable(true)
                    if (feature::class.java.isAssignableFrom(Listener::class.java)) {
                        unregisterListener(feature as Listener)
                    }
                }
                Features.features.clear()
                Features.userFeatures.clear()
                RegisteredFeatureHandler.process()
                settings = SettingsConfig.fromConfig() ?: SettingsConfig.defaultConfig()
                storageConfig = StorageConfig.fromConfig() ?: StorageConfig.defaultConfig()
                sender.sendComponent(language.general.reloaded)
            }
        }

        val levelLiteral = rawCommandBuilder().registerCopy {
            literalWithPermission("level")
        }

        levelLiteral.registerCopy {
            literalWithPermission("set")
            required("player", OfflinePlayerParser.offlinePlayerParser())
            required("level", IntegerParser.integerParser(0))
            handler { context ->
                val sender = context.sender().platformSender()
                val target = context.get<OfflinePlayer>("player")

                if (!target.hasPlayedBefore()) {
                    sender.sendComponent(language.general.playerNotFound)
                    return@handler
                }

                val user = target.getOrAddUser()
                user.vanishLevel = context.get("level")
                user.save()

                if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
                    sender.sendComponent(language.feature.permissionLevelMethodWarning, Placeholder.unparsed("method", FeatureLevel.LevelMethod.PERMISSION.name), Placeholder.unparsed("methods", FeatureLevel.LevelMethod.entries.joinToString(", ") { it.name }))
                    return@handler
                }

                sender.sendComponent(language.vanish.levelSet, Placeholder.unparsed("level", user.vanishLevel.toString()), Placeholder.unparsed("player", user.username))
            }
        }

        levelLiteral.registerCopy {
            literalWithPermission("get")
            required("player", OfflinePlayerParser.offlinePlayerParser())
            handler { context ->
                val sender = context.sender().platformSender()
                val target = context.get<OfflinePlayer>("player")

                if (!target.hasPlayedBefore()) {
                    sender.sendComponent(language.general.playerNotFound)
                    return@handler
                }

                val user = target.getOrCreateUser()

                sender.sendComponent(language.vanish.levelGet, Placeholder.unparsed("player", target.name ?: "N/A"), Placeholder.unparsed("level", user.vanishLevel.toString()))
            }
        }

        val featureLiteral = rawCommandBuilder().registerCopy {
            literalWithPermission("feature")
            required("feature", Features.features.map { it.id })
        }

        val togglePlayerLiteral = featureLiteral.registerCopy {
            literalWithPermission("toggleplayer")
            optional("player", PlayerParser.playerParser())
            handler { context ->
                val targetArg = context.optional<Player>("player").getOrNull()

                val sender = context.sender().platformSender()
                if (targetArg != null && !sender.hasPermission(Permission.FEATURE_PLAYER_TOGGLE.permission())) {
                    sender.sendComponent(language.feature.togglePlayerOther)
                    return@handler
                }

                if (targetArg == null && sender !is Player) {
                    sender.sendComponent(language.general.haveToProvidePlayer)
                    return@handler
                }

                val target = targetArg ?: sender as Player

                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendComponent(language.feature.notFound)
                    return@handler
                }

                val user = target.user() ?: let {
                    sender.sendComponent(language.general.userNotFound, Placeholder.unparsed("player", target.name))
                    return@handler
                }

                val userFeature = Features.userFeatures(user).find { it.id == feature.id }!!

                userFeature.toggle()
                sender.sendComponent(language.feature.togglePlayer, Placeholder.unparsed("player", target.name), Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("state", if (userFeature.enabled) "enabled" else "disabled"))
                return@handler
            }
        }
        manager.command(manager.commandBuilder("togglefeature").proxies(togglePlayerLiteral.build()))

        featureLiteral.registerCopy {
            literalWithPermission("disable")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendComponent(language.feature.notFound)
                    return@handler
                }

                if (!feature.enabled) {
                    sender.sendComponent(language.feature.alreadyDisabled, Placeholder.unparsed("feature", feature.id))
                    return@handler
                }

                feature.disable()
                feature.save()
                sender.sendComponent(language.feature.disabled, Placeholder.unparsed("feature", feature.id))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("enable")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendComponent(language.feature.notFound)
                    return@handler
                }

                if (feature.enabled) {
                    sender.sendComponent(language.feature.alreadyEnabled, Placeholder.unparsed("feature", feature.id))
                    return@handler
                }

                feature.enable()
                feature.save()
                sender.sendComponent(language.feature.enabled, Placeholder.unparsed("feature", feature.id))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("reset")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendComponent(language.feature.notFound)
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
                sender.sendComponent(language.feature.reset, Placeholder.unparsed("feature", feature.id))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("status")
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendComponent(language.feature.notFound)
                    return@handler
                }

                sender.sendComponent(language.feature.status, Placeholder.unparsed("feature", feature.id), Placeholder.parsed("status", if (feature.enabled) "<green>Enabled</green>" else "<red>Disabled</red>"))
            }
        }

        featureLiteral.registerCopy {
            literalWithPermission("update")
            required(CommandComponent.builder<BukkitSender, String>("option", StringParser.stringParser())
                .suggestionProvider { context, _ ->
                    val feature = Features.features.find { it.id == context.get<String>("feature") } ?: return@suggestionProvider CompletableFuture.completedFuture(emptyList())
                    CompletableFuture.completedFuture(feature::class.java.declaredFields.filter { it.isAnnotationPresent(Configurable::class.java) }.map { Suggestion.suggestion(it.name) })
                })
            required("value", StringParser.stringParser(StringParser.StringMode.QUOTED))
            handler { context ->
                val sender = context.sender().platformSender()
                val feature = Features.features.find { it.id == context.get<String>("feature") } ?: let {
                    sender.sendComponent(language.feature.notFound)
                    return@handler
                }

                val field = feature.javaClass.getDeclaredField(context.get("option"))
                if (field == null) {
                    sender.sendComponent(language.feature.invalidOption, Placeholder.unparsed("options", feature::class.memberProperties.joinToString(", ") { it.name }))
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
                    sender.sendComponent(language.feature.invalidValue, Placeholder.unparsed("values", field.type.simpleName ?: "N/A"))
                    return@handler
                }
                feature.save()

                sender.sendComponent(language.feature.updated, Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("option", field.name), Placeholder.unparsed("state", value))
            }
        }

        val testLiteral = rawCommandBuilder().registerCopy {
            literalWithPermission("test")
        }

        testLiteral.registerCopy {
            literalWithPermission("users")
            handler { context ->
                val sender = context.sender().platformSender()
                sender.sendComponent("<green>Vanished Users: <yellow>${SayanVanishAPI.getVanishedUsers().map { it.username }}")
            }
        }

        val testDatabaseLiteral = testLiteral.registerCopy {
            literalWithPermission("database")
        }

        testDatabaseLiteral.registerCopy {
            literalWithPermission("data")
            optional("limit", IntegerParser.integerParser(1, 10000))
            flag("no-cache")
            handler { context ->
                val sender = context.sender().platformSender()
                val limit = context.get<Int>("limit")
                val database = SayanVanishAPI.getDatabase()
                if (context.flags().hasFlag("no-cache")) {
                    database.useCache = false
                }

                val users = database.getVanishUsers()

                val limitedUsers = users.take(limit)

                val counter = MilliCounter()
                counter.start()
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

                    sender.sendComponent("<gold>[${index + 1}] <gray>$userProperties")
                }
                counter.stop()
                sender.sendComponent("<gray>Took <green>${counter.get()}ms</green>")

                database.useCache = true
            }
        }

        testDatabaseLiteral.registerCopy {
            literalWithPermission("performance")
            optional("amount", IntegerParser.integerParser(1, 10000))
            optional("tries", IntegerParser.integerParser(1, 10))
            flag("no-cache")
            handler { context ->
                val sender = context.sender().platformSender()
                val amount = context.get<Int>("amount")
                val database = SayanVanishAPI.getDatabase()
                if (context.flags().hasFlag("no-cache")) {
                    database.useCache = false
                }

                repeat(context.get("tries")) {
                    val counter = MilliCounter()
                    counter.start()
                    sender.sendComponent("<gold>[${it + 1}] <gray>Trying <green>${amount} Get Users</green> from data storage")
                    repeat(amount) {
                        database.getVanishUsers()
                    }
                    counter.stop()
                    sender.sendComponent("<gold>[${it + 1}] <gray>Took <green>${counter.get()}ms</green>")
                }

                database.useCache = true
            }
        }

    }

    private fun sendPasteError(sender: CommandSender, error: Throwable?) {
        if (error != null) {
            runSync {
                sender.sendComponent(language.paste.failedToGenerate)
            }
            error.printStackTrace()
        }
    }

    private fun generateMainPaste(sender: CommandSender, otherKeys: Map<String, String>) {
        Paste("json", listOf(ServerUtils.getServerData(
            mutableMapOf(
                "database-type" to storageConfig.method.toString(),
            ).apply {
                this.putAll(otherKeys)
            }
        ))
        ).post().whenComplete { key, generalError ->
            sendPasteError(sender, generalError)

            runSync {
                sender.sendComponent(language.paste.use.replace("<key>", key ?: "N/A"))
            }
        }
    }
}