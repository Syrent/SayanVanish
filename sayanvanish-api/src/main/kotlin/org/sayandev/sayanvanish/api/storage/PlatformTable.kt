package org.sayandev.sayanvanish.api.storage

import org.jetbrains.exposed.v1.core.Table
import org.sayandev.sayanvanish.api.Platform

open class PlatformTable(name: String) : Table("${Platform.get().id.lowercase()}_${name}")