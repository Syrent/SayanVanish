package org.sayandev.sayanvanish.bukkit.feature.features

import org.sayandev.sayanvanish.api.feature.Configurable
import org.sayandev.sayanvanish.api.feature.Feature
import org.sayandev.sayanvanish.api.feature.RegisteredFeature
import org.sayandev.sayanvanish.api.feature.category.FeatureCategories
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.getOrAddUser
import org.sayandev.sayanvanish.bukkit.config.language
import org.sayandev.stickynote.bukkit.onlinePlayers
import org.sayandev.stickynote.bukkit.runSync
import org.sayandev.stickynote.bukkit.utils.AdventureUtils.component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.sayandev.sayanvanish.bukkit.api.SayanVanishBukkitAPI.Companion.user
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@RegisteredFeature
@ConfigSerializable
data class FeatureProxyVanishQueue(
    @Comment("The time in milliseconds between each check for players in the queue. low values may cause performance issues.")
    @Configurable val checkEvery: Long = 100
) : Feature("proxy_vanish_queue", category = FeatureCategories.PROXY) {

    override fun enable() {
        runSync({
            for (player in onlinePlayers) {
                val user = player.user()
                if (((user != null && !isActive(user)) || !isActive())) return@runSync
                SayanVanishBukkitAPI.getInstance().database.isInQueue(player.uniqueId) { inQueue ->
                    if (inQueue) {
                        SayanVanishBukkitAPI.getInstance().database.getFromQueue(player.uniqueId) { isVanished ->
                            SayanVanishBukkitAPI.getInstance().database.removeFromQueue(player.uniqueId)
                            runSync {
                                val user = player.getOrAddUser()
                                user.sendComponent(language.vanish.vanishFromQueue, Placeholder.parsed("state", user.stateText(isVanished)))
                                val options = user.currentOptions.apply {
                                    this.sendMessage = false
                                }
                                if (isVanished) {
                                    user.vanish(options)
                                } else {
                                    user.unVanish(options)
                                }
                            }
                        }
                    }
                }
            }
        }, checkEvery, checkEvery)
        super.enable()
    }
}