package org.sayandev.sayanvanish.api.storage

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

interface TransactionType {
    val id: String
    val type: DatabaseType

    object Serializer : TypeSerializer<TransactionType> {
        override fun deserialize(
            type: Type,
            node: ConfigurationNode
        ): TransactionType {
            return object : TransactionType {
                override val id: String = node.node("id").string!!
                override val type: DatabaseType = node.node("type").get(DatabaseType::class.java)!!
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
            node.node("type").set(obj.type)
        }
    }
}