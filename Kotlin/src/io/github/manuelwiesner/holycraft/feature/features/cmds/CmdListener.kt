package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.msg.UnknownCmdException
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerCommandSendEvent

class CmdListener(feature: CmdFeature) : ListenerBase<CmdFeature>(feature) {

    @EventHandler
    fun onPlayerCommandPreprocessEvent(event: PlayerCommandPreprocessEvent) {
        if (event.player.isOp) return
        val label = event.message.drop(1).split(' ').first()

        if (isForbiddenCommand(event.player, label)) {
            event.isCancelled = true
            UnknownCmdException(label).sendMessage(this.feature, event.player)
        }
    }

    @EventHandler
    fun onPlayerCommandSendEvent(event: PlayerCommandSendEvent) {
        if (event.player.isOp) return
        event.commands.removeIf { isForbiddenCommand(event.player, it) }
    }

    private fun isForbiddenCommand(sender: CommandSender, label: String): Boolean {
        for (cmd in this.feature.getAllCommands()) {
            if (!cmd.acceptsSender(sender)) continue
            if (cmd.name.equals(label, true)) return false
            for (alias in cmd.getPluginCommand().aliases) if (alias.equals(label, true)) return false
        }
        return true
    }
}