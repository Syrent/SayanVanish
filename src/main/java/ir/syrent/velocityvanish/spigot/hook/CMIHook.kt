package ir.syrent.velocityvanish.spigot.hook

class CMIHook(name: String) : Dependency(name) {

    override fun features(): List<String> {
        return mutableListOf(
            "Prevent join and quit message for vanished players"
        )
    }

}
