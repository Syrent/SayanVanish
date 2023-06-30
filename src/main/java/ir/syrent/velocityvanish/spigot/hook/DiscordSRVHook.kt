package ir.syrent.velocityvanish.spigot.hook

import github.scarsz.discordsrv.DiscordSRV

class DiscordSRVHook(name: String) : Dependency(name) {

    lateinit var discordSRV: DiscordSRV

    init {
        if (exists) {
            discordSRV = DiscordSRV.getPlugin()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Send join/leave message on vanish toggle."
        )
    }

}