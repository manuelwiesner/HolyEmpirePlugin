package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.msg.Message
import io.github.manuelwiesner.holycraft.feature.msg.SaveFailureCmdMsg
import io.github.manuelwiesner.holycraft.feature.msg.SaveSuccessCmdMsg
import org.bukkit.command.CommandSender
import java.util.logging.Level

class SaveCmd(feature: CmdFeature) : CmdBaseArgs<CmdFeature>(feature, "save-hc") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        kotlin.runCatching {
            this.feature.getPlugin().logger.info("Saving HolyCraft to disk...")
            this.feature.getHolyCraft().saveToDisk()
            this.feature.getPlugin().logger.info("Done - saved HolyCraft!")
        }.onFailure {
            this.feature.getPlugin().logger.log(Level.SEVERE, "Failed to save HolyCraft to disk!", it)
            return SaveFailureCmdMsg(it::class.simpleName ?: "Error", it.localizedMessage)
        }
        return SaveSuccessCmdMsg()
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}