package ir.syrent.velocityvanish.spigot.hook.hooks

import ir.syrent.velocityvanish.spigot.hook.Dependency

class EnhancedVelocityHook(name: String) : Dependency(name) {

    override fun features(): List<String> {
        return mutableListOf(
            "Hide vanished players from global list <white>(/glist)</white>.",
            "Prevent players to use <white>/find</white> on vanished players."
        )
    }

    override fun description(): List<String> {
        return mutableListOf(
            "<yellow>We highly recommend to install <green>$name</green> on your Velocity plugins if you didn't."
        ).apply {
            if (features().isNotEmpty()) this.add("<white>$name advantages are listed below:")
        }
    }

}