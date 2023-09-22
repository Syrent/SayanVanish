package ir.syrent.velocityvanish.spigot.hook

import ir.syrent.velocityvanish.spigot.ruom.Ruom
import xyz.jpenilla.squaremap.api.Squaremap
import xyz.jpenilla.squaremap.api.SquaremapProvider

class SquareMapHook(name: String) : Dependency(name) {

    lateinit var squareMap: Squaremap
        private set

    init {
        Ruom.runSync({
            if (exists) {
                squareMap = SquaremapProvider.get()
            }
        }, 20)
    }

    override fun features(): List<String> {
        return mutableListOf(
            "Hide vanished players from map"
        )
    }

}