package org.sayandev.sayanvanish.velocity.command

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.incendo.cloud.component.CommandComponent
import org.incendo.cloud.context.CommandContext
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.MutableCommandBuilder
import org.incendo.cloud.parser.standard.StringParser
import org.incendo.cloud.suggestion.Suggestion
import org.incendo.cloud.velocity.parser.PlayerParser
import org.sayandev.sayanvanish.api.Permission
import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.api.feature.Features
import org.sayandev.sayanvanish.proxy.config.language
import org.sayandev.sayanvanish.proxy.config.settings
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.generateVanishUser
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser.Companion.getVanishUser
import org.sayandev.sayanvanish.velocity.feature.features.FeatureUpdate
import org.sayandev.sayanvanish.velocity.utils.PlayerUtils.sendComponent
import org.sayandev.stickynote.velocity.StickyNote
import org.sayandev.stickynote.velocity.command.VelocityCommand
import org.sayandev.stickynote.velocity.command.VelocitySender
import org.sayandev.stickynote.velocity.launch
import org.sayandev.stickynote.velocity.plugin
import org.sayandev.stickynote.velocity.utils.AdventureUtils.component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.jvm.optionals.getOrNull

class SayanVanishProxyCommandVelocity : VelocityCommand(settings.command.name, *settings.command.aliases.toTypedArray()) {

    override fun rootBuilder(builder: MutableCommandBuilder<VelocitySender>) {
        builder.permission("${plugin.container.description.name.get().lowercase()}.commands.use")
        builder.optional("player", PlayerParser.playerParser())
        builder.flag(
            "state",
            emptyArray(),
            Description.empty(),
            CommandComponent.builder<VelocitySender, String>("state", StringParser.stringParser())
                .suggestionProvider { _, _ ->
                    CompletableFuture.completedFuture(listOf("on", "off").map { Suggestion.suggestion(it) })
                }
        )
        builder.flag("silent", arrayOf("s"))
    }

    override fun rootHandler(context: CommandContext<VelocitySender>) {
        val sender = context.sender().platformSender()
        val target = context.optional<Player>("player")
        val state = context.flags().get<String>("state")

        if (!target.isPresent && sender !is Player) {
            sender.sendComponent(language.general.haveToProvidePlayer.component())
            return
        }

        if (target.isPresent && !sender.hasPermission(Permission.VANISH_OTHERS.permission())) {
            sender.sendComponent(language.general.dontHavePermission.component())
            return
        }

        val player = if (target.isPresent) context.optional<Player>("player").get() else context.sender().player() ?: return

        launch {
            val user = player.getVanishUser() ?: player.generateVanishUser()

            if (!user.hasPermission(Permission.VANISH)) {
                user.sendMessage(language.general.dontHavePermission, Placeholder.unparsed("permission", Permission.VANISH.permission()))
                return@launch
            }

            val options = VanishOptions.defaultOptions().apply {
                if (context.flags().hasFlag("silent")) {
                    this.sendMessage = false
                }
            }

            val targetPlayer = target.getOrNull() ?: sender as? Player ?: let {
                sender.sendMessage(language.general.haveToProvidePlayer.component())
                return@launch
            }

            when (state) {
                "on" -> user.disappear(options)
                "off" -> user.appear(options)
                else -> user.toggleVanish(options)
            }
            context.sender().platformSender().sendComponent(language.vanish.vanishToggle.component(Placeholder.unparsed("player", targetPlayer.username), Placeholder.parsed("state", user.stateText(!user.isVanished))))
        }
    }

    init {
        var forceUpdateConfirm = false
        rawCommandBuilder().registerCopy {
            literalWithPermission("forceupdate")
            handler { context ->
                val sender = context.sender().platformSender()
                if (!forceUpdateConfirm) {
                    sender.sendComponent(language.general.confirmUpdate.component())
                    forceUpdateConfirm = true
                    StickyNote.run({
                        forceUpdateConfirm = false
                    }, 5, TimeUnit.SECONDS)
                    return@handler
                }

                sender.sendComponent(language.general.updating.component())

                StickyNote.run {
                    val updateFeature = Features.getFeature<FeatureUpdate>()
                    updateFeature.updatePlugin().whenComplete { isSuccessful, error ->
                        error?.printStackTrace()

                        StickyNote.run {
                            if (isSuccessful) {
                                sender.sendComponent(language.general.updated.component(Placeholder.unparsed("version", updateFeature.latestVersion())))
                            } else {
                                sender.sendComponent(language.general.updateFailed.component())
                            }
                        }
                    }
                }
            }
        }
    }
}