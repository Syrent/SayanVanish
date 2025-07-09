package org.sayandev.sayanvanish.api.message

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

interface MessagingCategoryType {
    val id: String
    val type: MessagingTypes

    object Serializer : TypeSerializer<MessagingCategoryType> {
        override fun deserialize(
            type: Type,
            node: ConfigurationNode
        ): MessagingCategoryType {
            return object : MessagingCategoryType {
                override val id: String = node.node("id").string!!
                override val type: MessagingTypes = node.node("type").get(MessagingTypes::class.java)!!
            }
        }

        override fun serialize(
            type: Type,
            obj: MessagingCategoryType?,
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