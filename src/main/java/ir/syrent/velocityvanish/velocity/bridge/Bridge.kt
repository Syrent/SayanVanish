package ir.syrent.velocityvanish.velocity.bridge

interface Bridge {

    fun sendPluginMessage(sender: Any, messageByte: ByteArray)

    fun sendPluginMessage(messageByte: ByteArray)

}