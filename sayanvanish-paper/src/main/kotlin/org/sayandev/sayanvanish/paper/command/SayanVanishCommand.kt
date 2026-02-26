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

import com.charleskorn.kaml.YamlComment
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.IStringTooltip
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.EntitySelectorArgument.OnePlayer
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.GreedyStringArgument
import dev.jorel.commandapi.arguments.IntegerArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandArguments
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.SayanVanishAPI
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.api.feature.RegisteredFeatureHandler
import org.sayandev.sayanvanish.api.message.MessageConfig
import org.sayandev.sayanvanish.api.storage.StorageConfig
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
import org.sayandev.stickynote.bukkit.async
import org.sayandev.stickynote.bukkit.launch
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.pluginDirectory
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.unregisterListener
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.command.bukkit.BukkitCommand
import org.sayandev.stickynote.command.bukkit.executesCommand
import org.sayandev.stickynote.command.bukkit.executesSuspending
import org.sayandev.stickynote.command.bukkit.suggest
import org.sayandev.stickynote.command.bukkit.suggestTooltip
import org.sayandev.stickynote.core.utils.MilliCounter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import java.io.File
import java.lang.reflect.Field
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class SayanVanishCommand : BukkitCommand(
    Settings.get().vanishCommand.name,
    *Settings.get().vanishCommand.aliases.toTypedArray(),
) {

    private val rootPermission = "${plugin.name}.commands.use"
    private val featureCommentCache = ConcurrentHashMap<Class<out Feature>, Map<String, String>>()

    override fun build(command: CommandAPICommand) {
        command
            .withPermission(rootPermission)
            .executesSuspending { sender, _ ->
                handleRootCommand(sender, null, null)
            }

        command.withSubcommand(createRootStateCommand("on"))
        command.withSubcommand(createRootStateCommand("off"))
        command.withSubcommand(createPlayerCommand())
        command.withSubcommand(createForceUpdateCommand())
        command.withSubcommand(createPasteCommand())
        command.withSubcommand(createReloadCommand())
        command.withSubcommand(createLevelCommand())
        command.withSubcommand(createFeatureCommand())
        command.withSubcommand(createTestCommand())

        createToggleFeatureAliasCommand().register(plugin)
    }

    private fun createRootStateCommand(state: String): CommandAPICommand {
        return CommandAPICommand(state)
            .withPermission(rootPermission)
            .executesSuspending { sender, _ ->
                handleRootCommand(sender, null, state)
            }
    }

    private fun createPlayerCommand(): CommandAPICommand {
        return CommandAPICommand("player")
            .withPermission(rootPermission)
            .withArguments(offlinePlayerArgument("player"))
            .withOptionalArguments(stateArgument("state"))
            .executesSuspending { sender, arguments ->
                handleRootCommand(
                    sender,
                    arguments.getByClass("player", OfflinePlayer::class.java),
                    arguments.getOptionalByClass("state", String::class.java).orElse(null),
                )
            }
    }

    private fun createForceUpdateCommand(): CommandAPICommand {
        var forceUpdateConfirm = false

        return CommandAPICommand("forceupdate")
            .withPermission(commandPermission("forceupdate"))
            .executesCommand { sender, _ ->
                if (!forceUpdateConfirm) {
                    sender.sendPrefixComponent(language.general.confirmUpdate)
                    forceUpdateConfirm = true
                    runSync({
                        forceUpdateConfirm = false
                    }, 100)
                    return@executesCommand
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

    private fun createPasteCommand(): CommandAPICommand {
        return CommandAPICommand("paste")
            .withPermission(commandPermission("paste"))
            .executesSuspending { sender, _ ->
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
                        val databaseKey = Paste("yaml", StorageConfig.file.readLines().filter { line -> blockedWords.none { blockedWord -> line.contains(blockedWord) } }).post().await()
                        val settingsKey = Paste("yaml", Settings.settingsFile.readLines()).post().await()
                        val latestLogFile = File(File(pluginDirectory.parentFile.parentFile, "logs"), "latest.log")
                        if (latestLogFile.exists()) {
                            val logKey = Paste("log", latestLogFile.readLines()).post().await()

                            val featurePastes = mutableMapOf<String, List<String>>()
                            for (feature in Features.features()) {
                                featurePastes[feature.id] = File(Feature.directory(feature.category), "${feature.id}.yml").readLines()
                            }
                            val featureKey = Paste("yaml", featurePastes.map { "${it.key}:\n     ${it.value.joinToString("\n     ")}" }).post().await()
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

    private fun createReloadCommand(): CommandAPICommand {
        return CommandAPICommand("reload")
            .withPermission(commandPermission("reload"))
            .executesCommand { sender, _ ->
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

    private fun createLevelCommand(): CommandAPICommand {
        val levelRoot = CommandAPICommand("level")
            .withPermission(commandPermission("level"))

        levelRoot.withSubcommand(
            CommandAPICommand("set")
                .withPermission(commandPermission("level", "set"))
                .withArguments(
                    offlinePlayerArgument("player"),
                    IntegerArgument("level", 0),
                )
                .executesSuspending { sender, arguments ->
                    val target = arguments.getByClass("player", OfflinePlayer::class.java) ?: return@executesSuspending
                    val level = arguments.getByClass("level", Int::class.javaObjectType) ?: return@executesSuspending

                    if (!target.hasPlayedBefore()) {
                        sender.sendPrefixComponent(language.general.playerNotFound)
                        return@executesSuspending
                    }

                    val user = target.getOrAddVanishUser()
                    user.vanishLevel = level
                    user.saveAndSync()

                    if (Features.getFeature<FeatureLevel>().levelMethod == FeatureLevel.LevelMethod.PERMISSION) {
                        sender.sendPrefixComponent(
                            language.feature.permissionLevelMethodWarning,
                            Placeholder.unparsed("method", FeatureLevel.LevelMethod.PERMISSION.name),
                            Placeholder.unparsed("methods", FeatureLevel.LevelMethod.entries.joinToString(", ") { it.name }),
                        )
                        return@executesSuspending
                    }

                    sender.sendPrefixComponent(language.vanish.levelSet, Placeholder.unparsed("level", user.vanishLevel.toString()), Placeholder.unparsed("player", user.username))
                },
        )

        levelRoot.withSubcommand(
            CommandAPICommand("get")
                .withPermission(commandPermission("level", "get"))
                .withArguments(offlinePlayerArgument("player"))
                .executesSuspending { sender, arguments ->
                    val target = arguments.getByClass("player", OfflinePlayer::class.java) ?: return@executesSuspending

                    if (!target.hasPlayedBefore()) {
                        sender.sendPrefixComponent(language.general.playerNotFound)
                        return@executesSuspending
                    }

                    val user = target.getOrCreateVanishUser()
                    sender.sendPrefixComponent(language.vanish.levelGet, Placeholder.unparsed("player", target.name ?: "N/A"), Placeholder.unparsed("level", user.vanishLevel.toString()))
                },
        )

        return levelRoot
    }

    private fun createFeatureCommand(): CommandAPICommand {
        val featureRoot = CommandAPICommand("feature")
            .withPermission(commandPermission("feature"))

        featureRoot.withSubcommand(
            CommandAPICommand("toggleplayer")
                .withPermission(commandPermission("feature", "toggleplayer"))
                .withArguments(featureArgument("feature"))
                .withOptionalArguments(OnePlayer("player"))
                .executesSuspending { sender, arguments ->
                    toggleFeatureForPlayer(sender, arguments)
                },
        )

        featureRoot.withSubcommand(
            CommandAPICommand("disable")
                .withPermission(commandPermission("feature", "disable"))
                .withArguments(featureArgument("feature"))
                .executesCommand { sender, arguments ->
                    val feature = featureOrNotify(sender, arguments) ?: return@executesCommand

                    if (!feature.enabled) {
                        sender.sendPrefixComponent(language.feature.alreadyDisabled, Placeholder.unparsed("feature", feature.id))
                        return@executesCommand
                    }

                    feature.disable()
                    feature.save()
                    sender.sendPrefixComponent(language.feature.disabled, Placeholder.unparsed("feature", feature.id))
                },
        )

        featureRoot.withSubcommand(
            CommandAPICommand("enable")
                .withPermission(commandPermission("feature", "enable"))
                .withArguments(featureArgument("feature"))
                .executesCommand { sender, arguments ->
                    val feature = featureOrNotify(sender, arguments) ?: return@executesCommand

                    if (feature.enabled) {
                        sender.sendPrefixComponent(language.feature.alreadyEnabled, Placeholder.unparsed("feature", feature.id))
                        return@executesCommand
                    }

                    feature.enable()
                    feature.save()
                    sender.sendPrefixComponent(language.feature.enabled, Placeholder.unparsed("feature", feature.id))
                },
        )

        featureRoot.withSubcommand(
            CommandAPICommand("reset")
                .withPermission(commandPermission("feature", "reset"))
                .withArguments(featureArgument("feature"))
                .executesCommand { sender, arguments ->
                    val feature = featureOrNotify(sender, arguments) ?: return@executesCommand

                    feature.disable()
                    Features.removeFeature(feature)
                    val freshFeature = feature::class.java.getDeclaredConstructor().newInstance()
                    if (freshFeature.enabled) {
                        freshFeature.enable()
                    }
                    freshFeature.save()
                    Features.addFeature(freshFeature)
                    sender.sendPrefixComponent(language.feature.reset, Placeholder.unparsed("feature", feature.id))
                },
        )

        featureRoot.withSubcommand(
            CommandAPICommand("status")
                .withPermission(commandPermission("feature", "status"))
                .withArguments(featureArgument("feature"))
                .executesCommand { sender, arguments ->
                    val feature = featureOrNotify(sender, arguments) ?: return@executesCommand

                    sender.sendPrefixComponent(language.feature.status, Placeholder.unparsed("feature", feature.id), Placeholder.parsed("status", if (feature.enabled) "<green>Enabled</green>" else "<red>Disabled</red>"))
                },
        )

        featureRoot.withSubcommand(
            CommandAPICommand("update")
                .withPermission(commandPermission("feature", "update"))
                .withArguments(
                    featureArgument("feature"),
                    featureOptionArgument("option"),
                    featureValueArgument("value"),
                )
                .executesCommand { sender, arguments ->
                    val option = arguments.getByClass("option", FeatureOption::class.java) ?: return@executesCommand
                    val feature = option.feature
                    val value = arguments.get("value") ?: return@executesCommand
                    val field = option.field
                    field.isAccessible = true
                    field.set(feature, value)
                    feature.save()
                    sender.sendPrefixComponent(language.feature.updated, Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("option", field.name), Placeholder.unparsed("state", value.toString()))
                },
        )

        return featureRoot
    }

    private fun createToggleFeatureAliasCommand(): CommandAPICommand {
        return CommandAPICommand("togglefeature")
            .withPermission(commandPermission("feature", "toggleplayer"))
            .withArguments(featureArgument("feature"))
            .withOptionalArguments(OnePlayer("player"))
            .executesSuspending { sender, arguments ->
                toggleFeatureForPlayer(sender, arguments)
            }
    }

    private fun createTestCommand(): CommandAPICommand {
        val testRoot = CommandAPICommand("test")
            .withPermission(commandPermission("test"))

        testRoot.withSubcommand(
            CommandAPICommand("users")
                .withPermission(commandPermission("test", "users"))
                .executesSuspending { sender, _ ->
                    sender.sendPrefixComponent("<gray>Fetching vanish users from database...")
                    sender.sendPrefixComponent("<green>Database Vanished Users: <yellow>${VanishAPI.get().getDatabase().getVanishUsers().await().filter { it.isVanished }.map { it.username }}")
                    sender.sendPrefixComponent("<green>Cache Vanished Users: <yellow>${VanishAPI.get().getCacheService().getVanishUsers().values.map { it.username }}")
                },
        )

        val databaseRoot = CommandAPICommand("database")
            .withPermission(commandPermission("test", "database"))

        databaseRoot.withSubcommand(
            CommandAPICommand("data")
                .withPermission(commandPermission("test", "database", "data"))
                .withOptionalArguments(IntegerArgument("limit", 1, 10000))
                .executesSuspending { sender, arguments ->
                    val limit = arguments.getOptionalByClass("limit", Int::class.javaObjectType).orElse(50)
                    val database = VanishAPI.get().getDatabase()

                    val counter = MilliCounter()
                    counter.start()

                    val users = database.getVanishUsers().await().take(limit)
                    for ((index, user) in users.withIndex()) {
                        val userProperties = user::class.memberProperties
                            .filterIsInstance<KProperty1<Any, *>>()
                            .joinToString(" <green>|</green> ") { prop ->
                                try {
                                    "<gray>${prop.name}: <white>${prop.call(user)}"
                                } catch (_: Exception) {
                                    "<gray>${prop.name}: <red>Access Error"
                                }
                            }

                        sender.sendPrefixComponent("<gold>[${index + 1}] <gray>$userProperties")
                    }
                    counter.stop()
                    sender.sendPrefixComponent("<gray>Took <green>${counter.get()}ms</green>")
                },
        )

        databaseRoot.withSubcommand(
            CommandAPICommand("performance")
                .withPermission(commandPermission("test", "database", "performance"))
                .withOptionalArguments(
                    IntegerArgument("amount", 1, 10000),
                    IntegerArgument("tries", 1, 10),
                )
                .executesSuspending { sender, arguments ->
                    val amount = arguments.getOptionalByClass("amount", Int::class.javaObjectType).orElse(1)
                    val tries = arguments.getOptionalByClass("tries", Int::class.javaObjectType).orElse(1)
                    val database = VanishAPI.get().getDatabase()

                    repeat(tries) { iteration ->
                        val counter = MilliCounter()
                        counter.start()
                        sender.sendPrefixComponent("<gold>[${iteration + 1}] <gray>Trying <green>${amount} Get Users</green> from data storage")
                        repeat(amount) {
                            database.getVanishUsers().await()
                        }
                        counter.stop()
                        sender.sendPrefixComponent("<gold>[${iteration + 1}] <gray>Took <green>${counter.get()}ms</green>")
                    }
                },
        )

        testRoot.withSubcommand(databaseRoot)
        return testRoot
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
            sender.sendPrefixComponent(language.vanish.dontHaveUsePermission.component(Placeholder.unparsed("permission", Permissions.VANISH.permission())))
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

    private suspend fun toggleFeatureForPlayer(sender: CommandSender, arguments: CommandArguments) {
        val targetArg = arguments.getOptionalByClass("player", Player::class.java).orElse(null)

        if (targetArg != null && !sender.hasPermission(Permissions.FEATURE_PLAYER_TOGGLE.permission())) {
            sender.sendPrefixComponent(language.feature.togglePlayerOther)
            return
        }

        if (targetArg == null && sender !is Player) {
            sender.sendPrefixComponent(language.general.haveToProvidePlayer)
            return
        }

        val target = targetArg ?: sender as Player
        val feature = featureOrNotify(sender, arguments) ?: return

        val user = target.user().await() ?: run {
            sender.sendPrefixComponent(language.general.userNotFound, Placeholder.unparsed("player", target.name))
            return
        }

        val currentlyEnabled = Features.isFeatureEnabled(user, feature)
        Features.setFeatureEnabled(user, feature, !currentlyEnabled)
        sender.sendPrefixComponent(language.feature.togglePlayer, Placeholder.unparsed("player", target.name), Placeholder.unparsed("feature", feature.id), Placeholder.unparsed("state", if (!currentlyEnabled) "enabled" else "disabled"))
    }

    private fun featureOrNotify(sender: CommandSender, arguments: CommandArguments): Feature? {
        val feature = arguments.getByClass("feature", Feature::class.java)
        if (feature == null) {
            sender.sendPrefixComponent(language.feature.notFound)
            return null
        }
        return feature
    }

    private fun featureArgument(name: String): CustomArgument<Feature, String> {
        return CustomArgument(StringArgument(name)) { info ->
            Features.getFeatureById(info.input())
                ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.notFound)
        }.suggestTooltip { featureTooltips() }
    }

    private fun featureOptionArgument(name: String): CustomArgument<FeatureOption, String> {
        return CustomArgument(StringArgument(name)) { info ->
            val feature = info.previousArgs().getByClass("feature", Feature::class.java)
                ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.notFound)

            val fields = configurableFields(feature)
            val field = fields.firstOrNull { it.name.equals(info.input(), ignoreCase = true) }
                ?: throw CustomArgument.CustomArgumentException.fromString(
                    language.feature.invalidOption.replace("<options>", fields.joinToString(", ") { it.name }),
                )

            FeatureOption(feature, field)
        }.suggestTooltip { suggestion ->
            val feature = suggestion.previousArgs().getByClass("feature", Feature::class.java) ?: return@suggestTooltip emptyList()
            optionTooltips(feature)
        }
    }

    private fun featureValueArgument(name: String): CustomArgument<Any, String> {
        return CustomArgument(GreedyStringArgument(name)) { info ->
            val option = info.previousArgs().getByClass("option", FeatureOption::class.java)
                ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidOption)
            parseFeatureValue(option.field, info.input())
        }.suggest { suggestion ->
            val option = suggestion.previousArgs().getByClass("option", FeatureOption::class.java) ?: return@suggest emptyList()
            featureValueSuggestions(option.field)
        }
    }

    private fun configurableFields(feature: Feature): List<Field> {
        return feature::class.java.declaredFields
            .filter { it.isAnnotationPresent(Configurable::class.java) }
    }

    private fun featureTooltips(): Collection<IStringTooltip> {
        return Features.features().map { feature ->
            val tooltip = featureDescription(feature)
            if (tooltip.isNullOrBlank()) {
                StringTooltip.none(feature.id)
            } else {
                StringTooltip.ofString(feature.id, tooltip)
            }
        }
    }

    private fun optionTooltips(feature: Feature): Collection<IStringTooltip> {
        val commentsByField = commentsByField(feature)
        return configurableFields(feature).map { field ->
            val tooltip = commentsByField[field.name] ?: fieldDescriptionFromFieldAnnotation(field)
            if (tooltip.isNullOrBlank()) {
                StringTooltip.none(field.name)
            } else {
                StringTooltip.ofString(field.name, tooltip)
            }
        }
    }

    private fun featureDescription(feature: Feature): String? {
        val commentsByField = commentsByField(feature)
        return configurableFields(feature)
            .asSequence()
            .mapNotNull { commentsByField[it.name] ?: fieldDescriptionFromFieldAnnotation(it) }
            .firstOrNull()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun commentsByField(feature: Feature): Map<String, String> {
        return featureCommentCache.computeIfAbsent(feature::class.java) { featureClass ->
            val serializerClass = runCatching {
                Class.forName("${featureClass.name}\$\$serializer")
            }.getOrNull() ?: return@computeIfAbsent emptyMap()

            val serializer = runCatching {
                val instance = serializerClass.getField("INSTANCE").get(null)
                instance as? KSerializer<*>
            }.getOrNull() ?: return@computeIfAbsent emptyMap()

            val descriptor = serializer.descriptor
            buildMap {
                repeat(descriptor.elementsCount) { index ->
                    val optionName = descriptor.getElementName(index)
                    val yamlComment = descriptor.getElementAnnotations(index)
                        .firstOrNull { it is YamlComment } as? YamlComment
                    val commentLine = yamlComment?.lines
                        ?.asSequence()
                        ?.map(String::trim)
                        ?.filter(String::isNotBlank)
                        ?.joinToString(" ")

                    if (!commentLine.isNullOrBlank()) {
                        put(optionName, commentLine)
                    }
                }
            }
        }
    }

    private fun fieldDescriptionFromFieldAnnotation(field: Field): String? {
        val annotation = field.getAnnotation(YamlComment::class.java) ?: return null
        return annotation.lines
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .joinToString(" ")
            .ifBlank { null }
    }

    private fun parseFeatureValue(field: Field, raw: String): Any {
        val type = field.type
        return when {
            type == String::class.java -> raw
            type == Int::class.java || type == Integer::class.java ->
                raw.toIntOrNull() ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidValue.replace("<values>", "integer"))
            type == Long::class.java || type == java.lang.Long::class.java ->
                raw.toLongOrNull() ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidValue.replace("<values>", "long"))
            type == Float::class.java || type == java.lang.Float::class.java ->
                raw.toFloatOrNull() ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidValue.replace("<values>", "float"))
            type == Double::class.java || type == java.lang.Double::class.java ->
                raw.toDoubleOrNull() ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidValue.replace("<values>", "double"))
            type == Boolean::class.java || type == java.lang.Boolean::class.java ->
                raw.toBooleanStrictOrNull() ?: throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidValue.replace("<values>", "true, false"))
            type.isEnum -> {
                val constants = type.enumConstants.map { (it as Enum<*>).name }
                constants.firstOrNull { it.equals(raw, ignoreCase = true) }?.let { constant ->
                    type.enumConstants.first { (it as Enum<*>).name == constant }
                } ?: throw CustomArgument.CustomArgumentException.fromString(
                    language.feature.invalidValue.replace("<values>", constants.joinToString(", ")),
                )
            }
            else -> throw CustomArgument.CustomArgumentException.fromString(language.feature.invalidValue.replace("<values>", type.simpleName ?: "N/A"))
        }
    }

    private fun featureValueSuggestions(field: Field): Collection<String> {
        val type = field.type
        return when {
            type == Boolean::class.java || type == java.lang.Boolean::class.java -> listOf("true", "false")
            type == Int::class.java || type == Integer::class.java -> listOf("0", "1", "5", "10", "50", "100")
            type == Long::class.java || type == java.lang.Long::class.java -> listOf("0", "1", "5", "10", "50", "100")
            type == Float::class.java || type == java.lang.Float::class.java -> listOf("0.0", "0.5", "1.0", "5.0", "10.0")
            type == Double::class.java || type == java.lang.Double::class.java -> listOf("0.0", "0.5", "1.0", "5.0", "10.0")
            type.isEnum -> type.enumConstants.map { (it as Enum<*>).name.lowercase() }
            type == String::class.java -> listOf("\"value\"")
            else -> listOf("value")
        }
    }

    private fun offlinePlayerArgument(name: String): CustomArgument<OfflinePlayer, String> {
        return CustomArgument(StringArgument(name)) { info ->
            val player = offlinePlayer(info.input())
            if (!player.hasPlayedBefore() && !player.isOnline) {
                throw CustomArgument.CustomArgumentException.fromString(language.general.playerNotFound)
            }
            player
        }.suggest { suggestion ->
            val search = suggestion.currentArg().lowercase()
            Bukkit.getOfflinePlayers()
                .asSequence()
                .mapNotNull { it.name }
                .filter { it.startsWith(search, ignoreCase = true) }
                .take(30)
                .toList()
        }
    }

    private fun stateArgument(name: String): CustomArgument<String, String> {
        return CustomArgument(StringArgument(name)) { info ->
            val state = info.input().lowercase()
            if (state == "on" || state == "off") {
                state
            } else {
                throw CustomArgument.CustomArgumentException.fromString("<red>State must be one of: on, off")
            }
        }.suggest(listOf("on", "off"))
    }

    private fun offlinePlayer(input: String): OfflinePlayer {
        return runCatching {
            Bukkit.getOfflinePlayer(UUID.fromString(input))
        }.getOrElse {
            Bukkit.getOfflinePlayer(input)
        }
    }

    private suspend fun generateMainPaste(sender: CommandSender, otherKeys: Map<String, String>) {
        val key = Paste(
            "json",
            listOf(
                ServerUtils.getServerData(
                    mutableMapOf(
                        "database-type" to StorageConfig.get().method.toString(),
                    ).apply {
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

    private fun commandPermission(vararg nodes: String): String {
        val root = Settings.get().vanishCommand.name.lowercase()
        return "${plugin.name.lowercase()}.commands.${listOf(root, *nodes).joinToString(".")}"
    }

    private data class FeatureOption(val feature: Feature, val field: Field)

}
