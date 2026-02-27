package org.sayandev.sayanvanish.paper.command.argument

import org.sayandev.sayanvanish.api.feature.Feature
import java.lang.reflect.Field

data class FeatureOption(
    val feature: Feature,
    val field: Field,
)
