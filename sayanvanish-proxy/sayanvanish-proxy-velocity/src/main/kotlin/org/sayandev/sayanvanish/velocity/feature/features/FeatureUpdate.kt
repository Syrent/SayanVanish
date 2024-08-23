package org.sayandev.sayanvanish.velocity.feature.features

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PostLoginEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.utils.DownloadUtils
import org.sayandev.sayanvanish.api.utils.HangarUtils
import org.sayandev.sayanvanish.api.utils.VersionInfo
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.api.SayanVanishVelocityAPI.Companion.user
import org.sayandev.sayanvanish.velocity.feature.ListenedFeature
import org.sayandev.sayanvanish.velocity.sayanvanish
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.log
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@RegisteredFeature
@ConfigSerializable
class FeatureUpdate(
    @Configurable val checkEveryXMinutes: Int = 60 * 24,
    @Configurable val notifyBypassPermission: String = "sayanvanish.feature.update.notify.exempt",
    @Configurable val notifyOnJoin: Boolean = true,
    @Configurable val notifyForSnapshotBuilds: Boolean = true,
    @Configurable val autoUpdateNotification: Boolean = true,
    val updateNotificationContent: List<String> = listOf(
        "<green>A new version of <white>SayanVanish Velocity</white> is available!",
        "<gold> - Latest release: <white><latest_release_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_release_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_release_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_release_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray>Changelog: <white><latest_release_changelog>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_release_name>'>Click to see full changelog",
        "<gold> - Latest snapshot: <white><latest_snapshot_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_snapshot_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_snapshot_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_snapshot_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray>Changelog: <white><latest_snapshot_changelog>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_snapshot_name>'>Click to see full changelog"
    ),
    val updateRequestContent: List<String> = listOf(
        "<green>A new version of <white>SayanVanish Velocity</white> is available!",
        "<hover:show_text:'<red>Click to update'><click:run_command:'/${settings.command.name} forceupdate'><aqua>You can install version <version> by clicking on this message</click></hover>",
        "<red>Make sure to read the changelog before doing any update to prevent unexpected behaviors",
    )
) : ListenedFeature("update") {

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
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (player.hasPermission(notifyBypassPermission)) return
        if (notifyOnJoin && latestRelease != null && latestSnapshot != null) {
            sendUpdateNotification(player)

            if (autoUpdateNotification) {
                sendUpdateRequest(player)
            }
        }
    }

    private fun sendUpdateNotification(sender: CommandSource) {
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) return

        for (line in updateNotificationContent) {
            sender.sendMessage(line
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
                .component()
            )
        }
    }

    private fun sendUpdateRequest(sender: CommandSource) {
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) return

        for (line in updateRequestContent) {
            sender.sendMessage(line.replace("<version>", latestVersion()).component())
        }
    }

    private fun isNewerVersionAvailable(includeSnapshots: Boolean): Boolean {
        if (latestRelease == null || latestSnapshot == null) return false
        val currentVersion = plugin.container.description.version.get() // eg: 1.1.0-SNAPSHOT-build.121-ed8f2b2
        val commitHash = currentVersion.split("-").last()
        val snapshotVersion = latestSnapshot!!.name // eg: 1.1.0-SNAPSHOT-build.121
        if (currentVersion.removeSuffix("-${commitHash}") == snapshotVersion) return false
        if (includeSnapshots) {
            val releaseVersion = latestRelease!!.name // eg: 1.0.1-263f0bf
            if (currentVersion.removeSuffix("-${commitHash}") == releaseVersion) return false
        }
        return true
    }

    fun update(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) future.complete(false)

        val pluginFile = sayanvanish.pluginFile() ?: let {
            future.complete(false)
            return future
        }

        if (plugin.container.description.version.get().contains("SNAPSHOT")) {
            latestSnapshot?.let { snapshot ->
                DownloadUtils.download(snapshot.downloads.VELOCITY!!.downloadUrl!!, pluginFile).whenComplete { result, error ->
                    error?.printStackTrace()
                    future.complete(result)
                }
            } ?: let {
                future.complete(false)
            }
        } else {
            latestRelease?.let { release ->
                DownloadUtils.download(release.downloads.VELOCITY!!.downloadUrl!!, pluginFile).whenComplete { result, error ->
                    error?.printStackTrace()
                    future.complete(result)
                }
            } ?: let {
                future.complete(false)
            }
        }

        return future
    }

    fun latestVersion(): String {
        return (if (notifyForSnapshotBuilds) latestSnapshot?.name else latestRelease?.name) ?: "N/A"
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