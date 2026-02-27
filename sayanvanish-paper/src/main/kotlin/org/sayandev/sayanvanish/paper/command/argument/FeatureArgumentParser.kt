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
import org.sayandev.stickynote.command.bukkit.suggestTooltip
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
