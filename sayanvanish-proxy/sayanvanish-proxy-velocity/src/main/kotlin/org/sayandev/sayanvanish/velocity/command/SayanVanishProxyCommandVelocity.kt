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
package org.sayandev.sayanvanish.velocity.command

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.StringArgument
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.sayandev.sayanvanish.api.Permissions
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.generateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import org.sayandev.sayanvanish.velocity.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendComponent
import org.sayandev.stickynote.command.velocity.VelocityCommand
import org.sayandev.stickynote.command.velocity.executesCommand
import org.sayandev.stickynote.command.velocity.executesSuspending
import org.sayandev.stickynote.command.velocity.suggest
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.concurrent.TimeUnit

class SayanVanishProxyCommandVelocity : VelocityCommand(Settings.get().command.name, *Settings.get().command.aliases.toTypedArray()) {

    private val rootPermission = "${plugin.container.description.name.get().lowercase()}.commands.use"

    override fun build(command: CommandAPICommand) {
        command
            .withPermission(rootPermission)
            .executesSuspending { sender, _ ->
                handleVanish(sender, null, null)
            }

        command.withArguments(playerArgument("player"))
            .executesSuspending { sender, arguments ->
                handleVanish(sender, arguments.getByClass("player", String::class.java), null)
            }

        command.withSubcommand(stateCommand("on"))
        command.withSubcommand(stateCommand("off"))
        command.withSubcommand(createForceUpdateCommand())
    }

    private fun stateCommand(state: String): CommandAPICommand {
        return CommandAPICommand(state)
            .withPermission(rootPermission)
            .executesSuspending { sender, _ ->
                handleVanish(sender, null, state)
            }
            .withArguments(playerArgument("player"))
            .executesSuspending { sender, arguments ->
                handleVanish(sender, arguments.getByClass("player", String::class.java), state)
            }
    }

    private fun createForceUpdateCommand(): CommandAPICommand {
        var forceUpdateConfirm = false

        return CommandAPICommand("forceupdate")
            .withPermission(commandPermission("forceupdate"))
            .executesCommand { sender, _ ->
                if (!forceUpdateConfirm) {
                    sender.sendComponent(language.general.confirmUpdate.component())
                    forceUpdateConfirm = true
                    StickyNote.run({
                        forceUpdateConfirm = false
                    }, 5, TimeUnit.SECONDS)
                    return@executesCommand
                }

                sender.sendComponent(language.general.updating.component())

                StickyNote.run {
                    val updateFeature = Features.getFeature<FeatureUpdate>()
                    updateFeature.updatePlugin().whenComplete { isSuccessful, error ->
                        error?.printStackTrace()

                        StickyNote.run {
                            if (isSuccessful) {
                                sender.sendComponent(language.general.updated.component(Placeholder.unparsed("version", updateFeature.latestVersion())))
                            } else {
                                sender.sendComponent(language.general.updateFailed.component())
                            }
                        }
                    }
                }
            }
    }

    private suspend fun handleVanish(sender: CommandSource, targetName: String?, state: String?) {
        if (targetName == null && sender !is Player) {
            sender.sendComponent(language.general.haveToProvidePlayer.component())
            return
        }

        if (targetName != null && !sender.hasPermission(Permissions.VANISH_OTHERS.permission())) {
            sender.sendComponent(language.general.dontHavePermission.component())
            return
        }

        val targetPlayer = targetName?.let { plugin.server.getPlayer(it).orElse(null) } ?: sender as? Player ?: run {
            sender.sendComponent(language.general.haveToProvidePlayer.component())
            return
        }

        val user = targetPlayer.getVanishUser() ?: targetPlayer.generateVanishUser()

        if (!user.hasPermission(Permissions.VANISH)) {
            user.sendMessage(language.general.dontHavePermission, Placeholder.unparsed("permission", Permissions.VANISH.permission()))
            return
        }

        val options = VanishOptions.defaultOptions()

        when (state) {
            "on" -> user.disappear(options)
            "off" -> user.appear(options)
            else -> user.toggleVanish(options)
        }

        sender.sendComponent(
            language.vanish.vanishToggle.component(
                Placeholder.unparsed("player", targetPlayer.username),
                Placeholder.parsed("state", user.stateText(!user.isVanished)),
            ),
        )
    }

    private fun playerArgument(name: String): StringArgument {
        return StringArgument(name).suggest {
            plugin.server.allPlayers.map { player -> player.username }
        }
    }

    private fun commandPermission(vararg nodes: String): String {
        val root = Settings.get().command.name.lowercase()
        return "${plugin.container.description.name.get().lowercase()}.commands.${listOf(root, *nodes).joinToString(".")}"
    }
}
