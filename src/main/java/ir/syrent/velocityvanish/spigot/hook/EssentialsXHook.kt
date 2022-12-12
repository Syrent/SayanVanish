package ir.syrent.velocityvanish.spigot.hook

class EssentialsXHook constructor(name: String) : Dependency(name) {

    override fun features(): List<String> {
        return mutableListOf(
            "Prevent vanished player afk status change."
        )
    }

}