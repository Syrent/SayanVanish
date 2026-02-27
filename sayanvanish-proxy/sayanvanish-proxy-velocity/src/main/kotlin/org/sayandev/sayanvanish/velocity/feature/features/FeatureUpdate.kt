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
package org.sayandev.sayanvanish.velocity.feature.features

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.utils.DownloadUtils
import org.sayandev.sayanvanish.api.utils.HangarUtils
import org.sayandev.sayanvanish.api.utils.VersionUtils
import org.sayandev.sayanvanish.api.utils.VersionInfo
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendComponent
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendRawComponent
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.log
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import kotlinx.serialization.Serializable
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Transient
import org.sayandev.sayanvanish.proxy.config.Settings
import org.sayandev.sayanvanish.velocity.SayanVanishPlugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@RegisteredFeature
@Serializable
@SerialName("update")
class FeatureUpdate(
    @YamlComment("The period of time to check for updates.")
    @Configurable val checkEveryXMinutes: Int = 60 * 24,
    @YamlComment("The permission required to bypass update notifications.")
    @Configurable val notifyBypassPermission: String = "sayanvanish.feature.update.notify.exempt",
    @YamlComment("Whether to notify players when they join the server.")
    @Configurable val notifyOnJoin: Boolean = true,
    @YamlComment("Whether to notify players for snapshot builds.")
    @Configurable val notifyForSnapshotBuilds: Boolean = true,
    @YamlComment("Weather to ask players to do an automatic update when they join the server")
    @Configurable val autoUpdateNotification: Boolean = true,
    @YamlComment("The content of the update notification message")
    val updateNotificationContent: List<String> = listOf(
        "<green>A new version of <white>SayanVanish Velocity</white> is available!",
        "<gold> - Latest release: <white><latest_release_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_release_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_release_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_release_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_release_name>'>Click to see full changelog",
        "<gold> - Latest snapshot: <white><latest_snapshot_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_snapshot_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_snapshot_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_snapshot_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_snapshot_name>'>Click to see full changelog"
    ),
    @YamlComment("The content of the update request message")
    val updateRequestContent: List<String> = listOf(
        "<green>A new version of <white>SayanVanish Velocity</white> is available!",
        "<hover:show_text:'<red>Click to update'><click:run_command:'/${Settings.get().command.name} forceupdate'><aqua>You can install version <version> by clicking on this message</click></hover>",
        "<red>Make sure to read the changelog before doing any update to prevent unexpected behaviors",
    )
) : ListenedFeature() {

    @Transient override val id = "update"

    @Transient var latestRelease: VersionInfo? = null
    @Transient var latestSnapshot: VersionInfo? = null

    override fun enable() {
        StickyNote.run({
            if (!isActive()) return@run
            log("Checking for updates...")
            HangarUtils.getLatestRelease().whenComplete { latestRelease, releaseError ->
                releaseError?.printStackTrace()

                log("Checked latest release")
                this.latestRelease = latestRelease

                HangarUtils.getLatestSnapshot().whenComplete { latestSnapshot, snapshotError ->
                    snapshotError?.printStackTrace()

                    log("Checked latest snapshot")
                    this.latestSnapshot = latestSnapshot

                    StickyNote.run {
                        sendUpdateNotification(plugin.server.consoleCommandSource)
                    }
                }
            }
        }, 0, TimeUnit.MILLISECONDS, checkEveryXMinutes.toLong(), TimeUnit.MINUTES)
        super.enable()
    }

    @Subscribe
    private fun onLogin(event: PostLoginEvent) {
        val player = event.player
        launch {
            val user = player.getVanishUser() ?: return@launch
            if (!isActive(user)) return@launch
            if (player.hasPermission(notifyBypassPermission)) return@launch
            if (notifyOnJoin && latestComparableVersion(notifyForSnapshotBuilds) != null) {
                sendUpdateNotification(player)

                if (autoUpdateNotification) {
                    sendUpdateRequest(player)
                }
            }
        }
    }

    private fun sendUpdateNotification(sender: CommandSource) {
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) return

        for (line in updateNotificationContent) {
            sender.sendRawComponent(line
                .replace("<latest_release_name>", latestRelease?.name ?: "Unknown")
                .replace("<latest_release_url_paper>", latestRelease?.downloads?.PAPER?.downloadUrl() ?: "https://hangar.papermc.io/Syrent/SayanVanish")
                .replace("<latest_release_url_velocity>", latestRelease?.downloads?.VELOCITY?.downloadUrl() ?: "https://hangar.papermc.io/Syrent/SayanVanish")
                .replace("<latest_release_url_waterfall>", latestRelease?.downloads?.WATERFALL?.downloadUrl() ?: "https://hangar.papermc.io/Syrent/SayanVanish")
                .replace("<latest_release_changelog>", shortDescription(latestRelease?.description) ?: "Unknown")
                .replace("<latest_snapshot_name>", latestSnapshot?.name ?: "Unknown")
                .replace("<latest_snapshot_url_paper>", latestSnapshot?.downloads?.PAPER?.downloadUrl() ?: "https://hangar.papermc.io/Syrent/SayanVanish")
                .replace("<latest_snapshot_url_velocity>", latestSnapshot?.downloads?.VELOCITY?.downloadUrl() ?: "https://hangar.papermc.io/Syrent/SayanVanish")
                .replace("<latest_snapshot_url_waterfall>", latestSnapshot?.downloads?.WATERFALL?.downloadUrl() ?: "https://hangar.papermc.io/Syrent/SayanVanish")
                .replace("<latest_snapshot_changelog>", shortDescription(latestSnapshot?.description) ?: "Unknown")
            )
        }
    }

    private fun sendUpdateRequest(sender: CommandSource) {
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) return

        for (line in updateRequestContent) {
            sender.sendComponent(line.replace("<version>", latestVersion()).component())
        }
    }

    private fun isNewerVersionAvailable(includeSnapshots: Boolean): Boolean {
        val target = latestComparableVersion(includeSnapshots) ?: return false
        val currentVersion = plugin.container.description.version.get()
        return VersionUtils.isNewer(target.name, currentVersion)
    }

    fun updatePlugin(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) {
            future.complete(false)
            return future
        }

        val pluginFile = SayanVanishPlugin.getInstance().pluginFile() ?: let {
            future.complete(false)
            return future
        }

        val targetVersion = latestComparableVersion(notifyForSnapshotBuilds)
        if (targetVersion == null) {
            future.complete(false)
            return future
        }
        val downloadUrl = targetVersion.downloads.VELOCITY?.downloadUrl()
        if (downloadUrl.isNullOrBlank()) {
            future.complete(false)
            return future
        }

        DownloadUtils.download(downloadUrl, pluginFile).whenComplete { result, error ->
            error?.printStackTrace()
            future.complete(result)
        }

        return future
    }

    fun latestVersion(): String {
        return latestComparableVersion(notifyForSnapshotBuilds)?.name ?: "N/A"
    }

    private fun latestComparableVersion(includeSnapshots: Boolean): VersionInfo? {
        val candidates = buildList {
            latestRelease?.let(::add)
            if (includeSnapshots) {
                latestSnapshot?.let(::add)
            }
        }
        return candidates.maxWithOrNull { first, second ->
            VersionUtils.compare(first.name, second.name)
        }
    }

    private fun shortDescription(description: String?): String? {
        if (description == null) return null
        return if (description.length > 45) {
            val words = description.split(" ")
            var result = ""
            for (word in words) {
                if (result.length + word.length > 45) break
                result += "$word "
            }
            result.trim() + "..."
        } else {
            description
        }
    }


}
