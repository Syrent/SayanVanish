/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.paper.command

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.executors.CommandArguments
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.message.MessageConfig
import org.sayandev.sayanvanish.api.storage.StorageConfig
import org.sayandev.sayanvanish.api.utils.Paste
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getOrAddVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.getOrCreateVanishUser
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.user
import org.sayandev.sayanvanish.paper.command.argument.FeatureOption
import org.sayandev.sayanvanish.paper.command.argument.FeatureArgumentParser
import org.sayandev.sayanvanish.paper.command.argument.FeatureOptionArgumentParser
import org.sayandev.sayanvanish.paper.command.argument.FeatureValueArgumentParser
import org.sayandev.sayanvanish.paper.command.argument.OfflinePlayerArgumentParser
import org.sayandev.sayanvanish.paper.command.argument.StateArgumentParser
import org.sayandev.sayanvanish.paper.config.LanguageConfig
import org.sayandev.sayanvanish.paper.config.Settings
import org.sayandev.sayanvanish.paper.config.language
import org.sayandev.sayanvanish.paper.feature.features.FeatureLevel
import org.sayandev.sayanvanish.paper.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.paper.utils.PlayerUtils.sendPrefixComponent
import org.sayandev.sayanvanish.paper.utils.ServerUtils
import org.sayandev.stickynote.paper.async
import org.sayandev.stickynote.paper.plugin
import org.sayandev.stickynote.paper.pluginDirectory
import org.sayandev.stickynote.paper.runAsync
import org.sayandev.stickynote.paper.runSync
import org.sayandev.stickynote.paper.unregisterListener
import org.sayandev.stickynote.paper.utils.AdventureUtils.component
import org.sayandev.stickynote.command.paper.PaperCommand
import org.sayandev.stickynote.command.paper.commandNodePermission
import org.sayandev.stickynote.command.paper.command as commandNode
import org.sayandev.stickynote.command.paper.dsl
import org.sayandev.stickynote.command.paper.withGeneratedPermission
import org.sayandev.stickynote.command.paper.withUsePermission
import org.sayandev.stickynote.core.utils.MilliCounter
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class SayanVanishCommand : PaperCommand(
    Settings.get().vanishCommand.name,
    *Settings.get().vanishCommand.aliases.toTypedArray(),
) {

    private val commandRoot = Settings.get().vanishCommand.name.lowercase()

    private val stateArgumentParser = StateArgumentParser(invalidStateMessage = {
        "<red>State must be one of: on, off"
    })
    private val offlinePlayerArgumentParser = OfflinePlayerArgumentParser {
        language.general.playerNotFound
    }
    private val featureArgumentParser = FeatureArgumentParser {
        language.feature.notFound
    }
    private val featureOptionArgumentParser = FeatureOptionArgumentParser(
        featureNotFoundMessage = { language.feature.notFound },
        invalidOptionMessage = { options ->
            language.feature.invalidOption.replace("<options>", options.joinToString(", "))
        },
    )
    private val featureValueArgumentParser = FeatureValueArgumentParser(
        invalidOptionMessage = { language.feature.invalidOption },
        invalidValueMessage = { expected -> language.feature.invalidValue.replace("<values>", expected) },
    )

    init {
        register()
    }

    override fun build(command: CommandAPICommand) {
        command.dsl {
            withUsePermission()
            executesSuspend { sender, _ ->
                handleRootCommand(sender, null, null)
            }

            subcommand(createRootStateCommand("on"))
            subcommand(createRootStateCommand("off"))
            subcommand(createPlayerCommand())
            subcommand(createForceUpdateCommand())
            subcommand(createPasteCommand())
            subcommand(createReloadCommand())
            subcommand(createLevelCommand())
            subcommand(createFeatureCommand())
            subcommand(createTestCommand())
        }

        createToggleFeatureAliasCommand().register(plugin)
    }

    private fun createRootStateCommand(state: String): CommandAPICommand {
        return commandNode(state) {
            withUsePermission()
            executesSuspend { sender, _ ->
                handleRootCommand(sender, null, state)
            }
        }
    }

    private fun createPlayerCommand(): CommandAPICommand {
        return commandNode("player") {
            withUsePermission()
            arguments(offlinePlayerArgumentParser.argument("player"))
            optionalArguments(stateArgumentParser.argument("state"))
            executesSuspend { sender, args ->
                handleRootCommand(
                    sender = sender,
                    targetPlayer = args.required("player", OfflinePlayer::class.java),
                    state = args.optional("state", String::class.java),
                )
            }
        }
    }

    private fun createForceUpdateCommand(): CommandAPICommand {
        var forceUpdateConfirm = false
        return commandNode("forceupdate") {
            withGeneratedPermission(commandRoot, "forceupdate")
            executes { sender, _ ->
                if (!forceUpdateConfirm) {
                    sender.sendPrefixComponent(language.general.confirmUpdate)
                    forceUpdateConfirm = true
                    runSync({
                        forceUpdateConfirm = false
                    }, 100)
                    return@executes
                }

                sender.sendPrefixComponent(language.general.updating)

                runAsync {
                    val updateFeature = Features.getFeature<FeatureUpdate>()
                    updateFeature.updatePlugin().whenComplete { isSuccessful, error ->
                        error?.printStackTrace()

                        runSync {
                            if (isSuccessful) {
                                sender.sendPrefixComponent(
                                    language.general.updated,
                                    Placeholder.unparsed("version", updateFeature.latestVersion()),
                                )
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
    }

    private fun createPasteCommand(): CommandAPICommand {
        return commandNode("paste") {
            withGeneratedPermission(commandRoot, "paste")
            executesSuspend { sender, _ ->
                sender.sendPrefixComponent(language.paste.generating)
                async {
                    try {
                        val blockedWords = listOf("host", "port", "database", "username", "password")
                        val databaseKey = Paste(
                            "yaml",
                            StorageConfig.file.readLines().filter { line ->
                                blockedWords.none { blockedWord -> line.contains(blockedWord) }
                            },
                        ).post().await()
                        val settingsKey = Paste("yaml", Settings.settingsFile.readLines()).post().await()
                        val latestLogFile = File(File(pluginDirectory.parentFile.parentFile, "logs"), "latest.log")
                        if (latestLogFile.exists()) {
                            val logKey = Paste("log", latestLogFile.readLines()).post().await()
                            val featurePastes = Features.features().associate { feature ->
                                feature.id to File(Feature.directory(feature.category), "${feature.id}.yml").readLines()
                            }
                            val featureKey = Paste(
                                "yaml",
                                featurePastes.map { "${it.key}:\n     ${it.value.joinToString("\n     ")}" },
                            ).post().await()
                            generateMainPaste(
                                sender,
                                mapOf(
                                    "storage.yml" to Paste.url(databaseKey),
                                    "settings.yml" to Paste.url(settingsKey),
                                    "latest.log" to Paste.url(logKey),
                                    "features" to Paste.url(featureKey),
                                ),
                            )
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
    }

    private fun createReloadCommand(): CommandAPICommand {
        return commandNode("reload") {
            withGeneratedPermission(commandRoot, "reload")
            executes { sender, _ ->
                Features.features().forEach { feature ->
                    feature.disable(true)
                    if (feature is Listener) {
                        unregisterListener(feature)
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
    }

    private fun createLevelCommand(): CommandAPICommand {
        return commandNode("level") {
            withGeneratedPermission(commandRoot, "level")

            subcommand("set", commandNodePermission(commandRoot, "level", "set")) {
                arguments(
                    offlinePlayerArgumentParser.argument("player"),
                    IntegerArgument("level", 0),
                )
                executesSuspend { sender, args ->
                    val target = args.required("player", OfflinePlayer::class.java) ?: return@executesSuspend
                    val level = args.required("level", Int::class.javaObjectType) ?: return@executesSuspend

                    if (!target.hasPlayedBefore()) {
                        sender.sendPrefixComponent(language.general.playerNotFound)
                        return@executesSuspend
                    }

                    val user = target.getOrAddVanishUser()
                    user.vanishLevel = level
                    user.saveAndSync()

                    if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
                        sender.sendPrefixComponent(
                            language.feature.permissionLevelMethodWarning,
                            Placeholder.unparsed("method", FeatureLevel.LevelMethod.PERMISSION.name),
                            Placeholder.unparsed(
                                "methods",
                                FeatureLevel.LevelMethod.entries.joinToString(", ") { it.name },
                            ),
                        )
                        return@executesSuspend
                    }

                    sender.sendPrefixComponent(
                        language.vanish.levelSet,
                        Placeholder.unparsed("level", user.vanishLevel.toString()),
                        Placeholder.unparsed("player", user.username),
                    )
                }
            }

            subcommand("get", commandNodePermission(commandRoot, "level", "get")) {
                arguments(offlinePlayerArgumentParser.argument("player"))
                executesSuspend { sender, args ->
                    val target = args.required("player", OfflinePlayer::class.java) ?: return@executesSuspend

                    if (!target.hasPlayedBefore()) {
                        sender.sendPrefixComponent(language.general.playerNotFound)
                        return@executesSuspend
                    }

                    val user = target.getOrCreateVanishUser()
                    sender.sendPrefixComponent(
                        language.vanish.levelGet,
                        Placeholder.unparsed("player", target.name ?: "N/A"),
                        Placeholder.unparsed("level", user.vanishLevel.toString()),
                    )
                }
            }
        }
    }

    private fun createFeatureCommand(): CommandAPICommand {
        return commandNode("feature") {
            withGeneratedPermission(commandRoot, "feature")

            subcommand("toggleplayer", commandNodePermission(commandRoot, "feature", "toggleplayer")) {
                arguments(featureArgumentParser.argument("feature"))
                optionalArguments(OnePlayer("player"))
                executesSuspend { sender, args ->
                    toggleFeatureForPlayer(sender, args)
                }
            }

            subcommand("disable", commandNodePermission(commandRoot, "feature", "disable")) {
                arguments(featureArgumentParser.argument("feature"))
                executes { sender, args ->
                    val feature = featureOrNotify(sender, args) ?: return@executes
                    if (!feature.enabled) {
                        sender.sendPrefixComponent(
                            language.feature.alreadyDisabled,
                            Placeholder.unparsed("feature", feature.id),
                        )
                        return@executes
                    }

                    feature.disable()
                    feature.save()
                    sender.sendPrefixComponent(
                        language.feature.disabled,
                        Placeholder.unparsed("feature", feature.id),
                    )
                }
            }

            subcommand("enable", commandNodePermission(commandRoot, "feature", "enable")) {
                arguments(featureArgumentParser.argument("feature"))
                executes { sender, args ->
                    val feature = featureOrNotify(sender, args) ?: return@executes
                    if (feature.enabled) {
                        sender.sendPrefixComponent(
                            language.feature.alreadyEnabled,
                            Placeholder.unparsed("feature", feature.id),
                        )
                        return@executes
                    }

                    feature.enable()
                    feature.save()
                    sender.sendPrefixComponent(language.feature.enabled, Placeholder.unparsed("feature", feature.id))
                }
            }

            subcommand("reset", commandNodePermission(commandRoot, "feature", "reset")) {
                arguments(featureArgumentParser.argument("feature"))
                executes { sender, args ->
                    val feature = featureOrNotify(sender, args) ?: return@executes
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

            subcommand("status", commandNodePermission(commandRoot, "feature", "status")) {
                arguments(featureArgumentParser.argument("feature"))
                executes { sender, args ->
                    val feature = featureOrNotify(sender, args) ?: return@executes
                    sender.sendPrefixComponent(
                        language.feature.status,
                        Placeholder.unparsed("feature", feature.id),
                        Placeholder.parsed(
                            "status",
                            if (feature.enabled) "<green>Enabled</green>" else "<red>Disabled</red>",
                        ),
                    )
                }
            }

            subcommand("update", commandNodePermission(commandRoot, "feature", "update")) {
                arguments(
                    featureArgumentParser.argument("feature"),
                    featureOptionArgumentParser.argument("option"),
                    featureValueArgumentParser.argument("value"),
                )
                executes { sender, args ->
                    val option = args.required("option", FeatureOption::class.java) ?: return@executes
                    val value = args.get("value") ?: return@executes
                    option.field.isAccessible = true
                    option.field.set(option.feature, value)
                    option.feature.save()

                    sender.sendPrefixComponent(
                        language.feature.updated,
                        Placeholder.unparsed("feature", option.feature.id),
                        Placeholder.unparsed("option", option.field.name),
                        Placeholder.unparsed("state", value.toString()),
                    )
                }
            }
        }
    }

    private fun createToggleFeatureAliasCommand(): CommandAPICommand {
        return commandNode("togglefeature") {
            withGeneratedPermission(commandRoot, "feature", "toggleplayer")
            arguments(featureArgumentParser.argument("feature"))
            optionalArguments(OnePlayer("player"))
            executesSuspend { sender, args ->
                toggleFeatureForPlayer(sender, args)
            }
        }
    }

    private fun createTestCommand(): CommandAPICommand {
        return commandNode("test") {
            withGeneratedPermission(commandRoot, "test")

            subcommand("users", commandNodePermission(commandRoot, "test", "users")) {
                executesSuspend { sender, _ ->
                    sender.sendPrefixComponent("<gray>Fetching vanish users from database...")
                    sender.sendPrefixComponent(
                        "<green>Database Vanished Users: <yellow>${
                            VanishAPI.get().getDatabase().getVanishUsers().await()
                                .filter { it.isVanished }
                                .map { it.username }
                        }",
                    )
                    sender.sendPrefixComponent(
                        "<green>Cache Vanished Users: <yellow>${
                            VanishAPI.get().getCacheService().getVanishUsers().values.map { it.username }
                        }",
                    )
                }
            }

            subcommand("database", commandNodePermission(commandRoot, "test", "database")) {
                subcommand("data", commandNodePermission(commandRoot, "test", "database", "data")) {
                    optionalArguments(IntegerArgument("limit", 1, 10000))
                    executesSuspend { sender, args ->
                        val limit = args.optional("limit", Int::class.javaObjectType) ?: 50
                        val database = VanishAPI.get().getDatabase()

                        val counter = MilliCounter().apply { start() }
                        database.getVanishUsers().await().take(limit).forEachIndexed { index, user ->
                            val properties = user::class.memberProperties
                                .filterIsInstance<KProperty1<Any, *>>()
                                .joinToString(" <green>|</green> ") { property ->
                                    runCatching { "<gray>${property.name}: <white>${property.call(user)}" }
                                        .getOrDefault("<gray>${property.name}: <red>Access Error")
                                }

                            sender.sendPrefixComponent("<gold>[${index + 1}] <gray>$properties")
                        }
                        counter.stop()
                        sender.sendPrefixComponent("<gray>Took <green>${counter.get()}ms</green>")
                    }
                }

                subcommand("performance", commandNodePermission(commandRoot, "test", "database", "performance")) {
                    optionalArguments(
                        IntegerArgument("amount", 1, 10000),
                        IntegerArgument("tries", 1, 10),
                    )
                    executesSuspend { sender, args ->
                        val amount = args.optional("amount", Int::class.javaObjectType) ?: 1
                        val tries = args.optional("tries", Int::class.javaObjectType) ?: 1
                        val database = VanishAPI.get().getDatabase()

                        repeat(tries) { iteration ->
                            val counter = MilliCounter().apply { start() }
                            sender.sendPrefixComponent(
                                "<gold>[${iteration + 1}] <gray>Trying <green>${amount} Get Users</green> from data storage",
                            )
                            repeat(amount) {
                                database.getVanishUsers().await()
                            }
                            counter.stop()
                            sender.sendPrefixComponent(
                                "<gold>[${iteration + 1}] <gray>Took <green>${counter.get()}ms</green>",
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun handleRootCommand(sender: CommandSender, targetPlayer: OfflinePlayer?, state: String?) {
        if (targetPlayer == null && sender !is Player) {
            sender.sendPrefixComponent(language.general.haveToProvidePlayer)
            return
        }

        val target = targetPlayer ?: (sender as? Player) ?: return
        val isSelf = sender is Player && sender.uniqueId == target.uniqueId
        if (!isSelf && !sender.hasPermission(Permissions.VANISH_OTHERS.permission())) {
            sender.sendPrefixComponent(language.general.dontHavePermission)
            return
        }

        val user = target.getOrCreateVanishUser()
        if (!user.hasPermission(Permissions.VANISH)) {
            sender.sendPrefixComponent(
                language.vanish.dontHaveUsePermission.component(
                    Placeholder.unparsed("permission", Permissions.VANISH.permission()),
                ),
            )
            return
        }

        val options = VanishOptions.defaultOptions().apply {
            if (!target.isOnline) {
                sender.sendPrefixComponent(
                    language.vanish.offlineOnVanish,
                    Placeholder.unparsed("player", target.name ?: "N/A"),
                    Placeholder.parsed("state", user.stateText()),
                )
                sendMessage = false
            }
        }

        when (state) {
            "on" -> user.disappear(options)
            "off" -> user.appear(options)
            else -> user.toggleVanish(options)
        }
    }

    private suspend fun toggleFeatureForPlayer(sender: CommandSender, args: CommandArguments) {
        val target = args.optional("player", Player::class.java)
        if (target != null && !sender.hasPermission(Permissions.FEATURE_PLAYER_TOGGLE.permission())) {
            sender.sendPrefixComponent(language.feature.togglePlayerOther)
            return
        }

        if (target == null && sender !is Player) {
            sender.sendPrefixComponent(language.general.haveToProvidePlayer)
            return
        }

        val resolvedTarget = target ?: sender as Player
        val feature = featureOrNotify(sender, args) ?: return
        val user = resolvedTarget.user().await() ?: run {
            sender.sendPrefixComponent(
                language.general.userNotFound,
                Placeholder.unparsed("player", resolvedTarget.name),
            )
            return
        }

        val currentlyEnabled = Features.isFeatureEnabled(user, feature)
        Features.setFeatureEnabled(user, feature, !currentlyEnabled)
        sender.sendPrefixComponent(
            language.feature.togglePlayer,
            Placeholder.unparsed("player", resolvedTarget.name),
            Placeholder.unparsed("feature", feature.id),
            Placeholder.unparsed("state", if (!currentlyEnabled) "enabled" else "disabled"),
        )
    }

    private fun featureOrNotify(sender: CommandSender, args: CommandArguments): Feature? {
        val feature = args.required("feature", Feature::class.java)
        if (feature == null) {
            sender.sendPrefixComponent(language.feature.notFound)
        }
        return feature
    }

    private suspend fun generateMainPaste(sender: CommandSender, otherKeys: Map<String, String>) {
        val key = Paste(
            "json",
            listOf(
                ServerUtils.getServerData(
                    mutableMapOf("database-type" to StorageConfig.get().method.toString()).apply {
                        putAll(otherKeys)
                    },
                ),
            ),
        ).post().await()

        val pasteKey = key.ifEmpty { "N/A" }
        val pasteUrl = if (pasteKey == "N/A") "N/A" else Paste.url(pasteKey)
        sender.sendPrefixComponent(
            language.paste.use
                .replace("https://pastes.dev/<key>", pasteUrl)
                .replace("<url>", pasteUrl)
                .replace("<key>", pasteKey),
        )
    }

    private fun <T : Any> CommandArguments.required(name: String, type: Class<T>): T? {
        return getByClass(name, type)
    }

    private fun <T : Any> CommandArguments.optional(name: String, type: Class<T>): T? {
        return getOptionalByClass(name, type).orElse(null)
    }
}
