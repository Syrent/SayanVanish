package org.sayandev.sayanvanish.api.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TransactionType.Serializer::class)
interface TransactionType {
    val id: String
    val type: DatabaseType

    object Serializer : KSerializer<TransactionType> {
        override val descriptor = TransactionTypes.serializer().descriptor
        override fun serialize(encoder: Encoder, value: TransactionType) {
            when (value) {
                is TransactionTypes -> TransactionTypes.serializer().serialize(encoder, value)
            }
        }
        override fun deserialize(decoder: Decoder): TransactionType {
            return TransactionTypes.serializer().deserialize(decoder)
        }
    }
}