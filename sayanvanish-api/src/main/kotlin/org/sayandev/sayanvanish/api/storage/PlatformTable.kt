package org.sayandev.sayanvanish.api.storage

import org.jetbrains.exposed.sql.Table
import org.sayandev.sayanvanish.api.Platform

open class PlatformTable(name: String) : Table("${Platform.get().pluginName.lowercase()}_${name}")