package org.sayandev.sayanvanish.api.command

import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.context.CommandInput
import org.incendo.cloud.parser.ArgumentParseResult
import org.incendo.cloud.parser.ArgumentParser
import org.incendo.cloud.parser.ParserDescriptor
import org.incendo.cloud.suggestion.BlockingSuggestionProvider
import org.sayandev.sayanvanish.api.Platform
import org.sayandev.sayanvanish.api.User
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.Features

class FeatureParser<C> : ArgumentParser<C, Feature>, BlockingSuggestionProvider.Strings<C> {
    override fun parse(
        commandContext: CommandContext<C & Any>,
        commandInput: CommandInput
    ): ArgumentParseResult<Feature> {
        val input = commandInput.readString()
        val feature = Features.getFeatureById(input)
        return if (feature != null) {
            ArgumentParseResult.success(feature)
        } else {
            ArgumentParseResult.failure(Throwable("Feature with name `$input` doesn't exist"))
        }
    }

    override fun stringSuggestions(
        commandContext: CommandContext<C?>,
        input: CommandInput
    ): Iterable<String> {
        val input = input.readString()
        return Features.features().map { it.id }.filter { it.startsWith(input, ignoreCase = true) }
    }

    companion object {
        fun <C> featureParser(): ParserDescriptor<C, Feature> {
            return ParserDescriptor.of(FeatureParser(), Feature::class.java)
        }
    }
}