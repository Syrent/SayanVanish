package org.sayandev.sayanvanish.proxy.command

import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.stickynote.core.command.Command
import org.sayandev.stickynote.core.command.interfaces.SenderExtension

abstract class SayanVanishProxyCommand<S: SenderExtension<*, *>, C: CommandManager<S>>(
    manager: C,
) : Command<S>("sayanvanish", manager, settings.command.name, *settings.command.aliases.toTypedArray()) {

    override fun rootBuilder(builder: MutableCommandBuilder<S>) {
        builder.permission("sayanvanish.commands.use")
    }
}