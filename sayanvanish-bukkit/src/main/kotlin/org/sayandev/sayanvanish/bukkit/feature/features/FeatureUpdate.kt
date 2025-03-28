package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.utils.DownloadUtils
import org.sayandev.sayanvanish.api.utils.HangarUtils
import org.sayandev.sayanvanish.api.utils.VersionInfo
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.sayandev.sayanvanish.bukkit.config.settings
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.sayanvanish.bukkit.sayanvanish
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendComponent
import org.sayandev.sayanvanish.bukkit.utils.PlayerUtils.sendRawComponent
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.log
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.bukkit.runSync
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import java.io.File
import java.util.concurrent.CompletableFuture

@RegisteredFeature
@ConfigSerializable
class FeatureUpdate(
    @Comment("The interval to check for updates in minutes")
    @Configurable val checkEveryXMinutes: Int = 60 * 24,
    @Comment("The permission required to receive update notifications")
    @Configurable val notifyPermission: String = "${plugin.name.lowercase()}.feature.update.notify",
    @Comment("Whether to notify players if an update is available when they join the server")
    @Configurable val notifyOnJoin: Boolean = true,
    @Comment("Whether to notify players if an update is available for snapshot builds")
    @Configurable val notifyForSnapshotBuilds: Boolean = true,
    @Comment("Weather to ask players to do an automatic update when they join the server")
    @Configurable val autoUpdateNotification: Boolean = true,
    @Comment("The content of the update notification message")
    val updateNotificationContent: List<String> = listOf(
        "<green>A new version of <white>SayanVanish</white> is available!",
        "<gold> - Latest release: <white><latest_release_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_release_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_release_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_release_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_release_name>'>Click to see full changelog",
        "<gold> - Latest snapshot: <white><latest_snapshot_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_snapshot_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_snapshot_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_snapshot_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_snapshot_name>'>Click to see full changelog"
    ),
    @Comment("The content of the update request message")
    val updateRequestContent: List<String> = listOf(
        "<green>A new version of <white>SayanVanish</white> is available!",
        "<hover:show_text:'<red>Click to update'><click:run_command:'/${settings.command.name} forceupdate'><aqua>You can install version <version> by clicking on this message</click></hover>",
        "<red>Make sure to read the changelog before doing any update to prevent unexpected behaviors",
    )
) : ListenedFeature("update") {

    @Transient var latestRelease: VersionInfo? = null
    @Transient var latestSnapshot: VersionInfo? = null

    override fun enable() {
        runAsync({
            if (!isActive()) return@runAsync
            log("Checking for updates...")
            HangarUtils.getLatestRelease().whenComplete { latestRelease, releaseError ->
                releaseError?.printStackTrace()

                log("Checked latest release")
                this.latestRelease = latestRelease

                HangarUtils.getLatestSnapshot().whenComplete { latestSnapshot, snapshotError ->
                    snapshotError?.printStackTrace()

                    log("Checked latest snapshot")
                    this.latestSnapshot = latestSnapshot

                    runSync {
                        sendUpdateNotification(Bukkit.getConsoleSender())
                    }
                }
            }
        }, 0, checkEveryXMinutes * 60 * 20L)
        super.enable()
    }

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val user = player.user() ?: return
        if (!isActive(user)) return
        if (notifyOnJoin && player.hasPermission(notifyPermission) && latestRelease != null && latestSnapshot != null) {
            if (!settings.general.proxyMode) {
                sendUpdateNotification(player)
            }

            if (autoUpdateNotification) {
                sendUpdateRequest(player)
            }
        }
    }

    private fun sendUpdateNotification(sender: CommandSender) {
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds) || settings.general.proxyMode) return

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

    private fun sendUpdateRequest(sender: CommandSender) {
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) return

        for (line in updateRequestContent) {
            sender.sendComponent(line.replace("<version>", latestVersion()))
        }
    }

    private fun isNewerVersionAvailable(includeSnapshots: Boolean): Boolean {
        if (latestRelease == null || latestSnapshot == null) return false
        val currentVersion = plugin.description.version // eg: 1.1.0-SNAPSHOT-build.121-ed8f2b2
        val commitHash = currentVersion.split("-").last()
        val snapshotVersion = latestSnapshot!!.name // eg: 1.1.0-SNAPSHOT-build.121
        if (currentVersion.removeSuffix("-${commitHash}") == snapshotVersion) return false
        if (includeSnapshots) {
            val releaseVersion = latestRelease!!.name // eg: 1.0.1-263f0bf
            if (currentVersion.removeSuffix("-${commitHash}") == releaseVersion) return false
        }
        return true
    }

    fun updatePlugin(): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (!isNewerVersionAvailable(notifyForSnapshotBuilds)) future.complete(false)

        val updateDirectory = File(sayanvanish.dataFolder.parentFile, "update")
        val updatedFile = if (StickyNote.isPaper) {
            if (!updateDirectory.exists()) { updateDirectory.mkdirs() }
            File(updateDirectory, sayanvanish.pluginFile().name)
        } else sayanvanish.pluginFile()

        if (plugin.description.version.contains("SNAPSHOT")) {
            latestSnapshot?.let { snapshot ->
                DownloadUtils.download(snapshot.downloads.PAPER!!.downloadUrl!!, updatedFile).whenComplete { result, error ->
                    error?.printStackTrace()
                    future.complete(result)
                }
            } ?: let {
                future.complete(false)
            }
        } else {
            latestRelease?.let { release ->
                DownloadUtils.download(release.downloads.PAPER!!.downloadUrl!!, updatedFile).whenComplete { result, error ->
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

    private val proxyWords = listOf("proxy", "velocity", "bungee")
    fun willAffectProxy(): Boolean {
        return (if (notifyForSnapshotBuilds) proxyWords.any { latestSnapshot?.description?.contains(it) ?: false } else proxyWords.any { latestRelease?.description?.contains(it) ?: false }) ?: false
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