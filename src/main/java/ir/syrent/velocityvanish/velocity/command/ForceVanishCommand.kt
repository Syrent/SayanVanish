package ir.syrent.velocityvanish.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.proxy.Player
import ir.syrent.velocityvanish.spigot.ruom.Ruom
import ir.syrent.velocityvanish.velocity.VelocityVanish
import me.mohamad82.ruom.VRuom

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

        if (sender is Player) {
            return
        }

        if (!sender.hasPermission("velocityvanish.command.forcevanish")) {
            VRuom.log("You don't have permission to use this command.")
            return
        }

        if (args.isEmpty()) {
            Ruom.log("Usage: /forcevanish <player>")
            return
        }

        plugin.vanishedPlayers.add(args[0])
        Ruom.log("Player ${args[0]} is now in vanished players list!")
    }
}