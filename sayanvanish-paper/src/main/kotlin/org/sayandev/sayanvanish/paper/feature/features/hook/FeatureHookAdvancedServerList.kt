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
package org.sayandev.sayanvanish.paper.feature.features.hook

import kotlinx.serialization.SerialName
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.paper.feature.HookFeature
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("hook_advanced_server_list")
class FeatureHookAdvancedServerList: HookFeature() {

    @Transient override val id = "hook_advanced_server_list"
    override var enabled: Boolean = true
    override val plugin: String = "AdvancedServerList"

    override fun enable() {
        if (hasPlugin()) {
            AdvancedServerListImpl().register()
        }
        super.enable()
    }
}