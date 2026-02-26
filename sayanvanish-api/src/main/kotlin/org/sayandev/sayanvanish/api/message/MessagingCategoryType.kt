/*
 * This file is part of SayanVanish, licensed under the GNU General Public License v3.0.
 *
 * Copyright (c) 2026 Sayan Development and contributors
 *
 * SayanVanish is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SayanVanish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.sayandev.sayanvanish.api.message

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
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
