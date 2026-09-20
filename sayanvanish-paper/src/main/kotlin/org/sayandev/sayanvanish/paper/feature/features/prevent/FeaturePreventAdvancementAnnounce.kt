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
package org.sayandev.sayanvanish.paper.feature.features.prevent

import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.paper.api.SayanVanishPaperAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.paper.feature.ListenedFeature
import org.sayandev.stickynote.paper.StickyNote
import org.sayandev.stickynote.paper.utils.ServerVersion
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient

@RegisteredFeature
@Serializable
@SerialName("prevent_advancement_announce")
class FeaturePreventAdvancementAnnounce(
    @YamlComment("Whether to disable the advancement announce message when the player is vanished.")
    @Configurable val disableMessage: Boolean = true,
    @YamlComment("Whether to revoke the criteria when the player is vanished.")
    @Configurable val revokeCriteria: Boolean = false
): ListenedFeature() {

    @Transient override val id = "prevent_advancement_announce"
    override var enabled: Boolean = true
    @Transient override val category: FeatureCategories = FeatureCategories.PREVENTION

    @Transient
    override var condition: Boolean = StickyNote.isPaper && ServerVersion.supports(13)

    @EventHandler
    private fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val user = event.player.cachedVanishUser() ?: return
        if (!isActive(user)) return
        if (user.isVanished) {
            if (disableMessage) {
                event.message(null)
            }
            if (revokeCriteria) {
                for (criteria in event.advancement.criteria) {
                    event.player.getAdvancementProgress(event.advancement).revokeCriteria(criteria)
                }
            }
        }
    }

}