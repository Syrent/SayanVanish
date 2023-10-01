package ir.syrent.velocityvanish.spigot.hook.hooks

import ir.syrent.velocityvanish.spigot.VelocityVanishSpigot
import ir.syrent.velocityvanish.spigot.hook.Dependency
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.storage.Settings
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class PlaceholderAPIHook(plugin: VelocityVanishSpigot, name: String) : Dependency(name) {

    init {
        if (exists) {
            VanishExpansion(plugin).register()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Access to all placeholders in all plugin messages.",
            "Add plugin placeholders like %velocityvanish_online_total% to PlaceholderAPI."
        )
    }

    class VanishExpansion(
        private val plugin: VelocityVanishSpigot
    ) : PlaceholderExpansion() {
        override fun getIdentifier(): String {
            return Ruom.plugin.description.name.lowercase()
        }

        override fun getAuthor(): String {
            return Ruom.plugin.description.authors.joinToString(", ")
        }

        override fun getVersion(): String {
            return Ruom.plugin.description.version
        }

        override fun persist(): Boolean {
            return true
        }

        override fun canRegister(): Boolean {
            return true
        }

        override fun onRequest(player: OfflinePlayer?, params: String): String? {
            if (params.equals("vanished", true)) {
                return if (plugin.vanishedNames.contains(player?.name)) "true" else "false"
            }

            if (params.equals("count", true)) {
                return plugin.vanishedNamesOnline.size.toString()
            }

            if (params.startsWith("online_")) {
                val type = params.substring(7)

                return if (type.equals("here", true)) {
                    Ruom.onlinePlayers.filter { !plugin.vanishedNamesOnline.contains(it.name) }.size.toString()
                } else if (type.equals("total", true)) {
                    val players = mutableListOf<String>()
                    for (serverPlayers in plugin.proxyPlayers.values.filter { it.isNotEmpty() }) {
                        players.addAll(serverPlayers)
                    }
                    players.filter { !plugin.vanishedNamesOnline.contains(it) }.size.toString()
                } else {
                    if (!Settings.velocitySupport) {
                        warning("Plugin trying to get online players from other servers but Velocity support is disabled.")
                        Ruom.onlinePlayers.filter { !plugin.vanishedNamesOnline.contains(it.name) }.size.toString()
                    } else {
                        (plugin.proxyPlayers[type.lowercase()]?.filter { !plugin.vanishedNamesOnline.contains(it) }?.size ?: 0).toString()
                    }
                }
            }

            return null
        }
    }

}