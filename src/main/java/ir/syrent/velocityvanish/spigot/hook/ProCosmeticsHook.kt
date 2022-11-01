package ir.syrent.velocityvanish.spigot.hook

import ir.syrent.velocityvanish.spigot.ruom.Ruom
import org.bukkit.scheduler.BukkitRunnable
import se.file14.procosmetics.ProCosmetics
import se.file14.procosmetics.api.ProCosmeticsProvider

class ProCosmeticsHook constructor(name: String) : Dependency(name) {

    lateinit var proCosmetics: ProCosmetics
        private set

    init {
        if (exists) {
            proCosmetics = ProCosmeticsProvider.get()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Unequip player cosmetics when player is vanished",
            "Equip player cosmetics when player is unvanished"
        )
    }

}