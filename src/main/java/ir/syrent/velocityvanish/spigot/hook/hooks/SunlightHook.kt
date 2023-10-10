package ir.syrent.velocityvanish.spigot.hook.hooks

import ir.syrent.velocityvanish.spigot.hook.Dependency
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import su.nightexpress.sunlight.SunLight
import su.nightexpress.sunlight.data.impl.settings.UserSetting

class SunlightHook(name: String) : Dependency(name) {

    lateinit var sunLight: SunLight
        private set

    init {
        if (exists) {
            sunLight = Bukkit.getPluginManager().getPlugin("SunLight") as SunLight
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Better compatibility with other plugins"
        )
    }

    fun vanish(player: Player, vanish: Boolean) {
        if (exists) {
            sunLight.userManager.getUserData(player).settings.set(UserSetting.asBoolean("vanish", false, false), vanish)
        }
    }

}
