package org.sayandev.sayanvanish.api

interface VisibilityPolicy {
    fun canSee(viewer: VanishUser?, target: VanishUser): Boolean
}

object DefaultVisibilityPolicy : VisibilityPolicy {
    override fun canSee(viewer: VanishUser?, target: VanishUser): Boolean {
        if (!target.isVanished) return true
        if (viewer == null) return false
        if (viewer.uniqueId == target.uniqueId) return true
        if (!viewer.hasPermission(Permissions.VANISH)) return false
        return viewer.vanishLevel >= target.vanishLevel
    }
}
