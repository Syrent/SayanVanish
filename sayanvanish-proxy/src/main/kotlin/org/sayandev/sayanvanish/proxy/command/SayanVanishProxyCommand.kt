package org.sayandev.sayanvanish.proxy.command

import org.incendo.cloud.CommandManager
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.stickynote.core.command.Command
import org.sayandev.stickynote.core.command.interfaces.SenderExtension

abstract class SayanVanishProxyCommand<S: SenderExtension<*, *>, C: CommandManager<S>>(
    manager: C,
) : Command<S>("sayanvanishproxy", manager, Settings.get().command.name, *Settings.get().command.aliases.toTypedArray())