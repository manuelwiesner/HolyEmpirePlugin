package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.Message
import io.github.manuelwiesner.holycraft.feature.msg.ResolveNoSuchWorldCmdMsg
import io.github.manuelwiesner.holycraft.feature.msg.ResolvePlayerCmdMsg
import io.github.manuelwiesner.holycraft.feature.msg.ResolveWorldCmdMsg
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class ResolveUUIDCmd(feature: CmdFeature) : CmdBaseRoot<CmdFeature>(
    feature, "resolve",
    ResolveUUIDCmdWorld(feature), ResolveUUIDCmdPlayer(feature)
) {

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

class ResolveUUIDCmdWorld(feature: CmdFeature) : CmdBaseArgs<CmdFeature>(feature, "world",
    CmdArg.CUSTOM({ feature.getPlugin().server.worlds.map { it.name } })
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val worldName = args.text(0)
        val worldUUID = Bukkit.getWorld(worldName)?.uid

        return worldUUID?.let { ResolveWorldCmdMsg(worldName, worldUUID) } ?: ResolveNoSuchWorldCmdMsg(worldName)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

class ResolveUUIDCmdPlayer(feature: CmdFeature) : CmdBaseArgs<CmdFeature>(feature, "player", CmdArg.OFFLINE_PLAYER) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = args.offlinePlayer(0)
        return ResolvePlayerCmdMsg(player, player.uniqueId)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}