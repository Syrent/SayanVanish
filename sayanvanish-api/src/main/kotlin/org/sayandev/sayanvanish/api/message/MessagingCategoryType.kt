package org.sayandev.sayanvanish.api.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MessagingCategoryType.Serializer::class)
interface MessagingCategoryType {
    val id: String
    val type: MessagingTypes

    object Serializer : KSerializer<MessagingCategoryType> {
        override val descriptor = MessagingCategoryTypes.serializer().descriptor
        override fun serialize(encoder: Encoder, value: MessagingCategoryType) {
            when (value) {
                is MessagingCategoryTypes -> MessagingCategoryTypes.serializer().serialize(encoder, value)
            }
        }
        override fun deserialize(decoder: Decoder): MessagingCategoryType {
            return MessagingCategoryTypes.serializer().deserialize(decoder)
        }
    }
}