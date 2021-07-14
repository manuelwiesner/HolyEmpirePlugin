package io.github.manuelwiesner.holycraft.feature.features.tablist

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.DeathCounterAdjustedCmdMsg
import io.github.manuelwiesner.holycraft.feature.msg.DeathCounterResetCmdMsg
import io.github.manuelwiesner.holycraft.feature.msg.DeathCounterSetCmdMsg
import io.github.manuelwiesner.holycraft.feature.msg.Message
import org.bukkit.command.CommandSender

class DeathCounterCmd(feature: TablistFeature) : CmdBaseRoot<TablistFeature>(
    feature, "deathcounter", DeathCounterCmdSet(feature), DeathCounterCmdAdjust(feature), DeathCounterCmdReset(feature)
) {

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

class DeathCounterCmdSet(feature: TablistFeature) : CmdBaseArgs<TablistFeature>(
    feature, "set", CmdArg.OFFLINE_PLAYER, CmdArg.NUMBER(0..Int.MAX_VALUE)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = args.offlinePlayer(0)
        val newValue = args.number(1)

        val oldValue = this.feature.getDeathCount(player).getAndSet(newValue)
        return DeathCounterSetCmdMsg(player, oldValue, newValue)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

class DeathCounterCmdAdjust(feature: TablistFeature) : CmdBaseArgs<TablistFeature>(feature, "adjust", CmdArg.OFFLINE_PLAYER, CmdArg.NUMBER()) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = args.offlinePlayer(0)
        val adjustDelta = args.number(1)

        val newValue = this.feature.getDeathCount(player).addAndGet(adjustDelta)
        return DeathCounterAdjustedCmdMsg(player, adjustDelta, newValue)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

class DeathCounterCmdReset(feature: TablistFeature) : CmdBaseArgs<TablistFeature>(feature, "reset-all") {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        this.feature.resetAllDeathCounts()
        return DeathCounterResetCmdMsg()
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}