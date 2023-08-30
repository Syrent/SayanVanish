package ir.syrent.velocityvanish.spigot.command

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.flags.CommandFlag
import cloud.commandframework.arguments.standard.IntegerArgument
import cloud.commandframework.arguments.standard.StringArgument
import ir.syrent.velocityvanish.spigot.command.library.Command
import ir.syrent.velocityvanish.spigot.command.library.interfaces.ISender
import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Message
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.spigot.utils.sendMessage
import ir.syrent.velocityvanish.utils.TextReplacement
import org.bukkit.Bukkit
import kotlin.jvm.optionals.getOrNull

class VanishCommand(
    private val plugin: VelocityVanishSpigot
) : Command("velocityvanish", "velocityvanish.command.vanish", "vanish", "sayanvanish", "v") {

    init {
        val fakeJoinLiteral = addLiteral("fakejoin", ArgumentDescription.of("Send a fake join message"))
            .permission(getPermission("fakejoin"))
            .argument(StringArgument.builder<ISender?>("for").withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } })
            .argument(StringArgument.builder<ISender?>("to").asOptional().withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } })
            .handler { context ->
                val forPlayerName = context.get<String>("for")
                val forPlayer = Bukkit.getPlayerExact(forPlayerName)
                val playerName = context.getOptional<String>("to").getOrNull()
                val player = if (playerName != null) Bukkit.getPlayerExact(playerName) else null
                if (player != null) {
                    player.sendMessage(Message.JOIN_MESSAGE, TextReplacement("player_displayname", forPlayer?.displayName ?: forPlayerName), TextReplacement("player", forPlayer?.name ?: forPlayerName))
                } else {
                    Ruom.onlinePlayers.forEach {
                        it.sendMessage(Message.JOIN_MESSAGE, TextReplacement("player_displayname", forPlayer?.displayName ?: forPlayerName), TextReplacement("player", forPlayer?.name ?: forPlayerName))
                    }
                }
                context.sender.getSender().sendMessage(Message.JOIN_MESSAGE_SENT, TextReplacement("player", forPlayer?.name ?: forPlayerName))
            }
        saveCommand(fakeJoinLiteral)

        val reloadLiteral = addLiteral("reload", ArgumentDescription.of("Reload plugin's configuration files"))
            .permission(getPermission("reload"))
            .handler { context ->
                Settings.load()
                context.sender.getSender().sendMessage(Message.RELOAD_USE)
            }
        saveCommand(reloadLiteral)

        val fakeQuit = addLiteral("fakequit", ArgumentDescription.of("Send a fake quit message"))
            .permission(getPermission("fakequit"))
            .argument(StringArgument.builder<ISender?>("for").withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } })
            .argument(StringArgument.builder<ISender?>("to").asOptional().withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } })
            .handler { context ->
                val forPlayerName = context.get<String>("for")
                val forPlayer = Bukkit.getPlayerExact(forPlayerName)
                val playerName = context.getOptional<String>("to").getOrNull()
                val player = if (playerName != null) Bukkit.getPlayerExact(playerName) else null
                if (player != null) {
                    player.sendMessage(Message.QUIT_MESSAGE, TextReplacement("player_displayname", forPlayer?.displayName ?: forPlayerName), TextReplacement("player", forPlayer?.name ?: forPlayerName))
                } else {
                    Ruom.onlinePlayers.forEach {
                        it.sendMessage(Message.QUIT_MESSAGE, TextReplacement("player_displayname", forPlayer?.displayName ?: forPlayerName), TextReplacement("player", forPlayer?.name ?: forPlayerName))
                    }
                }
                context.sender.getSender().sendMessage(Message.QUIT_MESSAGE_SENT, TextReplacement("player", forPlayer?.name ?: forPlayerName))
            }
        saveCommand(fakeQuit)

        val setLevel = addLiteral("setlevel", ArgumentDescription.of("Set the vanish level of specific player"))
            .permission(getPermission("setlevel"))
            .argument(StringArgument.builder<ISender?>("player").withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } })
            .argument(IntegerArgument.builder<ISender?>("level").withMin(0))
            .handler { context ->
                val player = Bukkit.getPlayerExact(context.get("player")) ?: let {
                    context.sender.getSender().sendMessage(Message.PLAYER_NOT_FOUND)
                    return@handler
                }
                val level = context.get<Int>("level")

                val addAttachment = player.addAttachment(Ruom.plugin)
                addAttachment.setPermission("velocityvanish.level.$level", true)
                context.sender.getSender().sendMessage(Message.LEVEL_SET, TextReplacement("level", level.toString()), TextReplacement("player", player.name))
            }
        saveCommand(setLevel)

        val getLevel = addLiteral("getlevel", ArgumentDescription.of("Get the vanish level of specific player"))
            .permission(getPermission("getlevel"))
            .argument(StringArgument.builder<ISender?>("player").withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } })
            .handler { context ->
                val player = Bukkit.getPlayerExact(context.get("player")) ?: let {
                    context.sender.getSender().sendMessage(Message.PLAYER_NOT_FOUND)
                    return@handler
                }

                player.sendMessage(Message.LEVEL_GET, TextReplacement("player", player.name), TextReplacement("level", plugin.vanishManager.getVanishLevel(player).toString()))
            }
        saveCommand(getLevel)

        val vanishCommand = builder
            .argument(
                StringArgument.builder<ISender?>("player").asOptional().withSuggestionsProvider { _, _ -> Ruom.onlinePlayers.map { it.name } },
                ArgumentDescription.of("The player you want to vanish/unvanish")
            )
            .flag(CommandFlag.builder("state").withAliases("s").withArgument(
                StringArgument.builder<String>("state").withSuggestionsProvider { _, _ -> listOf("off", "on") }
            ))
            .flag(CommandFlag.builder("silent").withAliases("s"))
            .handler { context ->
                val playerName = context.getOptional<String>("player")
                val player = if (playerName.isPresent) Bukkit.getPlayerExact(playerName.get()).let {
                    if (it != null) {
                        it
                    } else {
                        context.sender.getSender().sendMessage(Message.PLAYER_NOT_FOUND)
                        return@handler
                    }
                } else context.sender.player() ?: return@handler

                val state = context.flags().getValue<String>("state")
                val silent = context.flags().hasFlag("silent").or(false)

                if (state.isPresent) {
                    when (state.get()) {
                        "on" -> {
                            plugin.vanishManager.vanish(player, callPostEvent = true, notifyAdmins = true)
                            if (plugin.vanishedNames.contains(player.name)) {
                                player.sendMessage(Message.VANISH_USE_VANISH)
                            }
                            return@handler
                        }
                        "off" -> {
                            plugin.vanishManager.unVanish(player, callPostEvent = true, sendJoinMessage = !silent, notifyAdmins = true)
                            if (plugin.vanishedNames.contains(player.name)) {
                                player.sendMessage(Message.VANISH_USE_UNVANISH)
                            }
                            return@handler
                        }
                    }
                }

                if (plugin.vanishedNames.contains(player.name)) {
                    player.sendMessage(Message.VANISH_USE_UNVANISH)
                    plugin.vanishManager.unVanish(player, callPostEvent = true, sendJoinMessage = !silent, notifyAdmins = true)
                } else {
                    player.sendMessage(Message.VANISH_USE_VANISH)
                    plugin.vanishManager.vanish(player, callPostEvent = true, sendQuitMessage = !silent, notifyAdmins = true)
                }
            }
        saveCommand(vanishCommand)

        val setStateCommand = addLiteral("setstate", ArgumentDescription.of("Set vanish state"))
            .permission(getPermission("setstate"))
            .argument(
                StringArgument.builder<ISender?>("state").withSuggestionsProvider { _, _ -> listOf("on", "off") },
                ArgumentDescription.of("The state of vanish (on/off)")
            )
            .flag(CommandFlag.builder("silent").withAliases("s"))
            .handler { context ->
                val player = context.sender.player() ?: return@handler
                val state = context.get<String>("state")
                val silent = context.flags().hasFlag("silent").or(false)

                if (state == "off") {
                    player.sendMessage(Message.VANISH_USE_UNVANISH)
                    plugin.vanishManager.unVanish(player, callPostEvent = true, sendJoinMessage = !silent, notifyAdmins = true)
                } else {
                    player.sendMessage(Message.VANISH_USE_VANISH)
                    plugin.vanishManager.vanish(player, callPostEvent = true, sendQuitMessage = !silent, notifyAdmins = true)
                }
            }
        saveCommand(setStateCommand)
    }
}