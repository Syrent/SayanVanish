package ir.syrent.velocityvanish.spigot.hook

import com.earth2me.essentials.Essentials
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class EssentialsXHook constructor(name: String) : Dependency(name) {

    var essentials: Essentials? = null

    override fun features(): List<String> {
        return mutableListOf(
            "Prevent vanished player afk status change.",
            "Better compatibility with other plugins"
        )
    }

    fun vanish(player: Player, vanish: Boolean) {
        if (exists) {
            essentials = Bukkit.getPluginManager().getPlugin("Essentials") as? Essentials
            val user = essentials?.getUser(player)
            user?.isVanished = vanish
            user?.isHidden = vanish
        }
    }

}