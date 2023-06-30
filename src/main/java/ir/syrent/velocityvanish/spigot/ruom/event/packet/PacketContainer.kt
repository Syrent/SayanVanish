package ir.syrent.velocityvanish.spigot.ruom.event.packet

class PacketContainer(
    /**
     * Returns the packet object.
     * @return The packet object
     */
    val packet: Any
) {

    /**
     * Returns the name of the packet.
     * Example of returning string: "PacketPlayOutEntityMetadata"
     * @return The packet name
     */
    var name: String? = null

    init {
        try {
            val rawNameSplit = packet.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val rawNameSplit2 = rawNameSplit[rawNameSplit.size - 1].split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            name = rawNameSplit2[0]
        } catch (e: Exception) {
            throw IllegalArgumentException("Given object is not a packet instance.")
        }
    }
}