package ir.syrent.velocityvanish.spigot.hook

class SayanChatHook constructor(name: String) : Dependency(name) {

    override fun features(): List<String> {
        return mutableListOf(
            "Prevent players to send private message to vanished players.",
            "Prevent players from mention vanished players in chat"
        )
    }

}