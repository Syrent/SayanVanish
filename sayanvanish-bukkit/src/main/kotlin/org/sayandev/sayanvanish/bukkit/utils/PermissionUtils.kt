package org.sayandev.sayanvanish.bukkit.utils

import org.bukkit.permissions.PermissionDefault
import org.sayandev.sayanvanish.api.PermissionDefaults

object PermissionUtils {
    fun PermissionDefaults.asBukkitPermissionDefault(): PermissionDefault {
        return when (this) {
            PermissionDefaults.FALSE -> PermissionDefault.FALSE
            PermissionDefaults.TRUE -> PermissionDefault.TRUE
            PermissionDefaults.ADMIN -> PermissionDefault.OP
            PermissionDefaults.NOT_ADMIN -> PermissionDefault.NOT_OP
        }
    }
}