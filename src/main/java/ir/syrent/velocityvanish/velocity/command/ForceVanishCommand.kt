package ir.syrent.velocityvanish.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import ir.syrent.velocityvanish.utils.component
import ir.syrent.velocityvanish.velocity.VelocityVanish
import ir.syrent.velocityvanish.velocity.vruom.VRuom

class ForceVanishCommand(
    private val plugin: VelocityVanish
) : SimpleCommand {

    init {
        VRuom.registerCommand(
            "forcevanish",
            emptyList(),
            this
        )
    }

    override fun execute(invocation: SimpleCommand.Invocation) {
        val sender = invocation.source()
        val args = invocation.arguments()

        if (!sender.hasPermission("velocityvanish.command.forcevanish")) {
            sender.sendMessage("<dark_red>You don't have permission to use this command!".component())
            return
        }

        if (args.isEmpty()) {
            sender.sendMessage("<red>Usage: <gold>/forcevanish <player>".component())
            return
        }

        plugin.vanishedPlayers.add(args[0])
        sender.sendMessage("<green>Player <gold>${args[0]}</gold> is now in vanished players list!".component())
    }
}