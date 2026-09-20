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
import org.sayandev.stickynote.command.paper.suggestTooltip
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

class FeatureOptionArgumentParser(
    private val featureNotFoundMessage: () -> String,
    private val invalidOptionMessage: (Collection<String>) -> String,
) {

    private val commentsCache = ConcurrentHashMap<Class<out Feature>, Map<String, String>>()

    fun argument(name: String): CustomArgument<FeatureOption, String> {
        return CustomArgument(StringArgument(name)) { info ->
            val feature = info.previousArgs().getByClass("feature", Feature::class.java)
                ?: throw CustomArgument.CustomArgumentException.fromString(featureNotFoundMessage())
            val fields = configurableFields(feature)
            val field = fields.firstOrNull { it.name.equals(info.input(), ignoreCase = true) }
                ?: throw CustomArgument.CustomArgumentException.fromString(
                    invalidOptionMessage(fields.map(Field::getName)),
                )
            FeatureOption(feature, field)
        }.suggestTooltip { suggestion ->
            val feature = suggestion.previousArgs().getByClass("feature", Feature::class.java)
                ?: return@suggestTooltip emptyList()
            optionTooltips(feature)
        }
    }

    private fun optionTooltips(feature: Feature): Collection<IStringTooltip> {
        val comments = commentsByField(feature)
        return configurableFields(feature).map { field ->
            val description = comments[field.name]
            if (description.isNullOrBlank()) {
                StringTooltip.none(field.name)
            } else {
                StringTooltip.ofString(field.name, description)
            }
        }
    }

    private fun configurableFields(feature: Feature): List<Field> {
        return feature::class.java.declaredFields
            .filter { it.isAnnotationPresent(Configurable::class.java) }
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
