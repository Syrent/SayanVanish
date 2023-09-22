package ir.syrent.velocityvanish.spigot.hook

import github.scarsz.discordsrv.DiscordSRV
import ir.syrent.velocityvanish.spigot.ruom.Ruom

class DiscordSRVHook(name: String) : Dependency(name) {

    lateinit var discordSRV: DiscordSRV

    init {
        Ruom.runSync({
            if (exists) {
                discordSRV = DiscordSRV.getPlugin()
            }
        }, 20)
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Send join/leave message on vanish toggle."
        )
    }

}