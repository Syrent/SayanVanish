package org.sayandev.sayanvanish.api.database

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

interface TransactionType {
    val id: String
    val method: DatabaseMethod

    object Serializer : TypeSerializer<TransactionType> {
        override fun deserialize(
            type: Type,
            node: ConfigurationNode
        ): TransactionType {
            return object : TransactionType {
                override val id: String = node.node("id").string!!
                override val method: DatabaseMethod = node.node("method").get(DatabaseMethod::class.java)!!
            }
        }

        override fun serialize(
            type: Type,
            obj: TransactionType?,
            node: ConfigurationNode
        ) {
            if (obj == null) {
                node.raw(null)
                return
            }
            node.node("id").set(obj.id)
            node.node("method").set(obj.method)
        }
    }
}