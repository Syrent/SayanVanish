package ir.syrent.velocityvanish.spigot.hook

class PlaceholderAPIHook constructor(name: String) : Dependency(name) {

    override fun features(): List<String> {
        return mutableListOf(
            "Access to all placeholders in all plugin messages."
        )
    }

}