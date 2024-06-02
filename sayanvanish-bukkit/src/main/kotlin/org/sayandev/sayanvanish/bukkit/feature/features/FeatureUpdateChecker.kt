package org.sayandev.sayanvanish.bukkit.feature.features

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.event.BukkitUserUnVanishEvent
import org.sayandev.sayanvanish.bukkit.feature.ListenedFeature
import org.sayandev.sayanvanish.bukkit.utils.HangarUtils
import org.sayandev.sayanvanish.bukkit.utils.VersionInfo
import org.sayandev.stickynote.bukkit.log
import org.sayandev.stickynote.bukkit.plugin
import org.sayandev.stickynote.bukkit.runAsync
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.sendMessage
import org.sayandev.stickynote.lib.kyori.adventure.text.Component
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureUpdateChecker(
    val checkEveryXMinutes: Int = 60,
    val notifyPermission: String = "sayanvanish.feature.updatechecker.notify",
    val notifyOnJoin: Boolean = true,
    val content: List<String> = listOf(
        "<green>A new version of <white>SayanVanish</white> is available!",
        "<gold> - Latest release: <white><latest_release_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_release_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_release_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_release_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray>Changelog: <white><latest_release_changelog>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_release_name>'>Click to see full changelog",
        "<gold> - Latest snapshot: <white><latest_snapshot_name>",
        "  <yellow>- <gray>Click to download: <blue><click:open_url:'<latest_snapshot_url_paper>'>Paper</click> <gray>|</gray> <aqua><click:open_url:'<latest_snapshot_url_velocity>'>Velocity</click> <gray>|</gray> <blue><click:open_url:'<latest_snapshot_url_waterfall>'>Waterfall</click>",
        "  <yellow>- <gray>Changelog: <white><latest_snapshot_changelog>",
        "  <yellow>- <gray><click:open_url:'https://hangar.papermc.io/Syrent/SayanVanish/versions/<latest_snapshot_name>'>Click to see full changelog"
    )
) : ListenedFeature("update_checker") {

    @Transient var latestRelease: VersionInfo? = null
    @Transient var latestSnapshot: VersionInfo? = null

    init {
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
                        send(Bukkit.getConsoleSender())
                    }
                }
            }
        }, 0, checkEveryXMinutes * 60 * 20L)
    }

    @EventHandler
    private fun onVanish(event: PlayerJoinEvent) {
        if (!isActive()) return
        val player = event.player
        if (notifyOnJoin && player.hasPermission(notifyPermission) && latestRelease != null && latestSnapshot != null) {
            send(player)
        }
    }

    @EventHandler
    private fun onUnVanish(event: BukkitUserUnVanishEvent) {
        if (!isActive()) return
        val user = event.user
        user.sendActionbar(Component.empty())
    }

    private fun send(sender: CommandSender) {
        if (latestRelease == null || latestSnapshot == null) return
        if (latestRelease!!.name == plugin.description.version && latestSnapshot!!.name == plugin.description.version) return

        for (line in content) {
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