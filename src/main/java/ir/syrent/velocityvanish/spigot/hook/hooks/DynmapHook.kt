package ir.syrent.velocityvanish.spigot.hook.hooks

import ir.syrent.velocityvanish.spigot.hook.Dependency
import org.bukkit.Bukkit
import org.dynmap.DynmapCommonAPI

class DynmapHook(name: String) : Dependency(name) {

    lateinit var dynmap: DynmapCommonAPI
        private set

    init {
        if (exists) {
            dynmap =  Bukkit.getPluginManager().getPlugin("Dynmap") as DynmapCommonAPI;
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Hide vanished players from dynmap"
        )
    }

}