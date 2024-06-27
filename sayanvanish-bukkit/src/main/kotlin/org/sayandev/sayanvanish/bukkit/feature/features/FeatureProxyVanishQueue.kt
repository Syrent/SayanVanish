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
import org.sayandev.stickynote.lib.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.sayandev.stickynote.lib.spongepowered.configurate.objectmapping.ConfigSerializable

@RegisteredFeature
@ConfigSerializable
class FeatureProxyVanishQueue(
    @Configurable val checkEvery: Long = 100
) : Feature("proxy_vanish_queue", category = FeatureCategories.PROXY) {

    override fun enable() {
        runSync({
            if (!isActive()) return@runSync
            for (player in onlinePlayers) {
                SayanVanishBukkitAPI.getInstance().database.isInQueue(player.uniqueId) { inQueue ->
                    if (inQueue) {
                        SayanVanishBukkitAPI.getInstance().database.getFromQueue(player.uniqueId) { isVanished ->
                            SayanVanishBukkitAPI.getInstance().database.removeFromQueue(player.uniqueId)
                            runSync {
                                val user = player.getOrAddUser()
                                user.sendMessage(language.vanish.vanishFromQueue.component(Placeholder.parsed("state", user.stateText(isVanished))))
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