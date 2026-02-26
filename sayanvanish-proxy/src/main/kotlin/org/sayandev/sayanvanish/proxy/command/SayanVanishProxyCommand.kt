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
package org.sayandev.sayanvanish.proxy.command

import org.incendo.cloud.CommandManager
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.stickynote.core.command.Command
import org.sayandev.stickynote.core.command.interfaces.SenderExtension

abstract class SayanVanishProxyCommand<S: SenderExtension<*, *>, C: CommandManager<S>>(
    manager: C,
) : Command<S>("sayanvanishproxy", manager, Settings.get().command.name, *Settings.get().command.aliases.toTypedArray())