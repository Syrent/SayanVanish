package ir.syrent.velocityvanish.spigot.hook.hooks

import ir.syrent.velocityvanish.spigot.hook.Dependency
import xyz.jpenilla.squaremap.api.Squaremap
import xyz.jpenilla.squaremap.api.SquaremapProvider

class SquareMapHook(name: String) : Dependency(name) {

    lateinit var squareMap: Squaremap
        private set

    init {
        if (exists) {
            squareMap = SquaremapProvider.get()
        }
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Hide vanished players from map"
        )
    }

}