package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBase
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val LAST_INTERACTOR: ConcurrentHashMap<UUID, UUID> = ConcurrentHashMap()

class MsgCmd(feature: CmdFeature) : CmdBase<CmdFeature>(feature, "message") {

    override fun onCommand(sender: CommandSender, args: List<String>): Message {
        if (args.size < 2) return MessageArgsCmdMsg()

        val receiver = CmdArg.ONLINE_PLAYER.parseArg(sender, args.first()) as Player
        val message = args.drop(1).joinToString(" ")

        if (receiver.name == sender.name) return MessageYourselfCmdMsg()

        (sender as? Player)?.let { LAST_INTERACTOR[receiver.uniqueId] = it.uniqueId }
        MessageReceived(sender.name, message).sendMessage(this.feature, receiver)
        return MessageSent(receiver.name, message)
    }

    override fun onTabComplete(sender: CommandSender, args: List<String>): MutableList<String>? {
        return if (args.size in 0..1) CmdArg.ONLINE_PLAYER.completeArg(sender, args.getOrElse(0) { "" }) else null
    }
}

class RCmd(feature: CmdFeature) : CmdBase<CmdFeature>(feature, "r") {

    override fun onCommand(sender: CommandSender, args: List<String>): Message {
        if (args.isEmpty()) return RNoMsgCmdMsg()

        val senderPlayer = ensurePlayer(sender)
        val receiver = LAST_INTERACTOR[senderPlayer.uniqueId]?.let { Bukkit.getPlayer(it) } ?: return RNoInteractorCmdMsg()
        val message = args.joinToString(" ")

        (sender as? Player)?.let { LAST_INTERACTOR[receiver.uniqueId] = it.uniqueId }
        MessageReceived(sender.name, message).sendMessage(this.feature, receiver)
        return MessageSent(receiver.name, message)
    }

    override fun onTabComplete(sender: CommandSender, args: List<String>): MutableList<String>? = null
}