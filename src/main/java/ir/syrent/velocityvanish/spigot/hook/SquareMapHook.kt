package ir.syrent.velocityvanish.spigot.hook

import xyz.jpenilla.squaremap.api.Squaremap
import xyz.jpenilla.squaremap.api.SquaremapProvider

class SquareMapHook constructor(name: String) : Dependency(name) {

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