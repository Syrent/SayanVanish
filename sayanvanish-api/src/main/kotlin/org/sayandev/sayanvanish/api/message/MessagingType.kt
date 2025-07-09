package org.sayandev.sayanvanish.api.message

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

interface MessagingType {
    val id: String

    object Serializer : TypeSerializer<MessagingType> {
        override fun deserialize(
            type: Type,
            node: ConfigurationNode
        ): MessagingType {
            return object : MessagingType {
                override val id: String = node.node("id").string!!
            }
        }

        override fun serialize(
            type: Type,
            obj: MessagingType?,
            node: ConfigurationNode
        ) {
            if (obj == null) {
                node.raw(null)
                return
            }
            node.node("id").set(obj.id)
        }
    }
}