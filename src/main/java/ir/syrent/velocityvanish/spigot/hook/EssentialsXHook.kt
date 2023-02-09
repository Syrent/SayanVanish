package ir.syrent.velocityvanish.spigot.hook

import com.earth2me.essentials.Essentials
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class EssentialsXHook constructor(name: String) : Dependency(name) {

    val essentials: Essentials = Bukkit.getPluginManager().getPlugin("Essentials") as Essentials

    override fun features(): List<String> {
        return mutableListOf(
            "Prevent vanished player afk status change."
        )
    }

    fun vanish(player: Player, vanish: Boolean) {
        if (exists) {
            val user = essentials.getUser(player)
            user?.isVanished = vanish
            user?.isHidden = vanish
        }
    }

}