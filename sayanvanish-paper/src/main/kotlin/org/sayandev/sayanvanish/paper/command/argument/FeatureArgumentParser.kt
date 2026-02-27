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
package org.sayandev.sayanvanish.paper.command.argument

import com.charleskorn.kaml.YamlComment
import dev.jorel.commandapi.IStringTooltip
import dev.jorel.commandapi.StringTooltip
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.stickynote.command.paper.suggestTooltip
import java.util.concurrent.ConcurrentHashMap

class FeatureArgumentParser(
    private val notFoundMessage: () -> String,
) {

    private val commentsCache = ConcurrentHashMap<Class<out Feature>, Map<String, String>>()

    fun argument(name: String): CustomArgument<Feature, String> {
        return CustomArgument(StringArgument(name)) { info ->
            Features.getFeatureById(info.input())
                ?: throw CustomArgument.CustomArgumentException.fromString(notFoundMessage())
        }.suggestTooltip { tooltips() }
    }

    private fun tooltips(): Collection<IStringTooltip> {
        return Features.features().map { feature ->
            val description = featureDescription(feature)
            if (description.isNullOrBlank()) {
                StringTooltip.none(feature.id)
            } else {
                StringTooltip.ofString(feature.id, description)
            }
        }
    }

    private fun featureDescription(feature: Feature): String? {
        val comments = commentsByField(feature)
        return feature::class.java.declaredFields
            .asSequence()
            .filter { it.isAnnotationPresent(Configurable::class.java) }
            .mapNotNull { comments[it.name] }
            .firstOrNull()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun commentsByField(feature: Feature): Map<String, String> {
        return commentsCache.computeIfAbsent(feature::class.java) { featureClass ->
            val serializerClass = runCatching {
                Class.forName("${featureClass.name}\$\$serializer")
            }.getOrNull() ?: return@computeIfAbsent emptyMap()

            val serializer = runCatching {
                serializerClass.getField("INSTANCE").get(null) as? KSerializer<*>
            }.getOrNull() ?: return@computeIfAbsent emptyMap()

            val descriptor = serializer.descriptor
            buildMap {
                repeat(descriptor.elementsCount) { index ->
                    val key = descriptor.getElementName(index)
                    val comment = descriptor.getElementAnnotations(index)
                        .firstOrNull { it is YamlComment }
                        ?.let { it as YamlComment }
                        ?.lines
                        ?.asSequence()
                        ?.map(String::trim)
                        ?.filter(String::isNotBlank)
                        ?.joinToString(" ")

                    if (!comment.isNullOrBlank()) {
                        put(key, comment)
                    }
                }
            }
        }
    }
}
