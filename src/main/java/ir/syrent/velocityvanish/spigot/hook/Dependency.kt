package ir.syrent.velocityvanish.spigot.hook

import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.spigot.ruom.adventure.AdventureApi
import ir.syrent.velocityvanish.spigot.storage.Settings
import ir.syrent.velocityvanish.utils.component

abstract class Dependency(val name: String) {

    val exists = Ruom.hasPlugin(name) && Settings.settings.config.getBoolean("hooks.${name.lowercase()}.enabled", false)

    fun register() {
        if (Settings.showDependencySuggestions) {
            sendDescription()
            sendFeatures()
        }
    }

    fun sendDescription() {
        description().map {
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} $it".component())
        }
    }

    fun sendFeatures() {
        features().map {
            AdventureApi.get().console().sendMessage("${Settings.getConsolePrefix()} ${formatFeature(it)}".component())
        }
    }

    open fun features(): List<String> {
       return emptyList()
    }

    open fun description(): List<String> {
        return if (exists) {
            mutableListOf(
                "<green>$name found! dependency hook activated."
            )
        } else {
            mutableListOf(
                "<yellow>You may need to install <green>$name</green> to take full advantage of the plugin features."
            )
        }.apply {
            if (features().isNotEmpty()) this.add("<white>$name advantages are listed below:")
        }
    }

    open fun formatFeature(feature: String): String {
        return "<white>⬤ <gray>$feature"
    }
}