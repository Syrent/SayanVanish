package org.sayandev.sayanvanish.api

interface LifecycleVanishAPI : VanishAPI {
    fun initialize(enableMessaging: Boolean)
    fun reloadMessaging(enableMessaging: Boolean)
    fun shutdown()
    fun reinitialize(enableMessaging: Boolean)
}
