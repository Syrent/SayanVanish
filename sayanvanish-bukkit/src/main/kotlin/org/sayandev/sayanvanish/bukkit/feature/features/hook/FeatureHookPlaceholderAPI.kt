package org.sayandev.sayanvanish.bukkit.feature.features.hook

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.sayandev.sayanvanish.api.VanishAPI
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.cachedVanishUser
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.sayanvanish.bukkit.feature.HookFeature
import org.sayandev.stickynote.bukkit.StickyNote
import org.sayandev.stickynote.bukkit.hook.PlaceholderAPIHook
import org.sayandev.stickynote.bukkit.onlinePlayers
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName

@RegisteredFeature
@Serializable
@SerialName("hook_placeholderapi")
class FeatureHookPlaceholderAPI(
    @YamlComment(
    "Inject placeholders into PlaceholderAPI",
    "Available placeholders:",
    "- %sayanvanish_vanished% - Returns true if the player is vanished",
    "- %sayanvanish_level% - Returns the vanish level of the player",
    "- %sayanvanish_count% - Returns the count of vanished players",
    "- %sayanvanish_online_here% - Returns the count of online players that are not vanished",
    "- %sayanvanish_online_total% - Returns the count of online players that are not vanished in the total network",
    "- %sayanvanish_online_<server_id>% - Returns the count of online players that are not vanished in the specified server",
    )
    @Configurable val injectPlaceholders: Boolean = true
): HookFeature("hook_placeholderapi", "PlaceholderAPI") {

    @Transient private var hook: Any? = null

    override fun enable() {
        if (hasPlugin()) {
            var hook = hook as? HookPlaceholderAPI?
            if (hook == null || hook.isRegistered() == false) {
                hook = HookPlaceholderAPI()
                hook.register()
                this.hook = hook
            }
            if (injectPlaceholders) {
                PlaceholderAPIHook.injectComponent(true)
            }
        }
        super.enable()
    }

    override fun disable(reload: Boolean) {
        if (hasPlugin()) {
            val hook = this.hook as? HookPlaceholderAPI?
            if (hook?.isRegistered() == true) {
                hook.unregister()
            }
            PlaceholderAPIHook.injectComponent(false)
        }
        super.disable(reload)
    }
}

private class HookPlaceholderAPI : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return StickyNote.plugin().description.name.lowercase()
    }

    override fun getAuthor(): String {
        return StickyNote.plugin().description.authors.joinToString(", ")
    }

    override fun getVersion(): String {
        return StickyNote.plugin().description.version
    }

    override fun persist(): Boolean {
        return true
    }

    override fun canRegister(): Boolean {
        return true
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (params.equals("vanished", true)) {
            if (player == null) return "false"
            return if (VanishAPI.get().getCacheService().getVanishUsers().getVanished().map { it.username }.contains(player.name)) "true" else "false"
        }

        if (params.equals("level", true)) {
            if (player == null) return "0"
            return player.cachedVanishUser()?.vanishLevel?.toString() ?: "0"
        }

        if (params.equals("count", true)) {
            return VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { user -> user.isOnline }.size.toString()
        }

        if (params.equals("vanish_prefix", true)) {
            if (player == null) return ""
            return if (player.cachedVanishUser()?.isVanished == true) language.vanish.placeholderPrefix else ""
        }

        if (params.equals("vanish_suffix", true)) {
            if (player == null) return ""
            return if (player.cachedVanishUser()?.isVanished == true) language.vanish.placeholderSuffix else ""
        }

        if (params.startsWith("online_")) {
            val type = params.removePrefix("online_")
            val vanishedOnlineUsers = VanishAPI.get().getCacheService().getVanishUsers().getVanished().filter { user -> user.isOnline }

            return if (type.equals("here", true)) {
                onlinePlayers.filter { onlinePlayer -> !vanishedOnlineUsers.map { vanishedOnlineUser -> vanishedOnlineUser.username }.contains(onlinePlayer.name) }.size.toString()
            } else if (type.equals("total", true)) {
                return VanishAPI.get().getCacheService().getUsers().values.filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
            } else {
                return VanishAPI.get().getCacheService().getUsers().getByServer(type).filter { !vanishedOnlineUsers.map { vanishUser -> vanishUser.username }.contains(it.username) }.size.toString()
            }
        }

        return null
    }
}