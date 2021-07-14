package io.github.manuelwiesner.holycraft.feature.features.spawnprotect

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.msg.Message
import org.bukkit.command.CommandSender

class ActiveCmd(feature: SpawnProtectFeature) : CmdBaseArgs<SpawnProtectFeature>(feature, "voting", CmdArg.ONLINE_PLAYER) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message? {
        val player = args.onlinePlayer(0)
        this.feature.setActive(player)
        sender.sendMessage("Set voting player to " + player.name)
        return null
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}