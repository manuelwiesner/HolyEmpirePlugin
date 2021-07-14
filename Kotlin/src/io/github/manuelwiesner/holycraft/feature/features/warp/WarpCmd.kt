package io.github.manuelwiesner.holycraft.feature.features.warp

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender

class WarpCmd(feature: WarpFeature) : CmdBaseRoot<WarpFeature>(
    feature, "warp", WarpCmdList(feature),
    WarpCmdTp(feature), WarpCmdInfo(feature), WarpCmdSet(feature), WarpCmdRemove(feature)
) {
    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class WarpCmdList(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "list") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        return WarpListCmdMsg(this.feature.getWarps().fancyJoin())
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class WarpCmdTp(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "tp", CmdArg.CUSTOM({ feature.getWarps() })) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val warp = args.text(0)
        val warpLocation = this.feature.getWarp(warp) ?: return WarpNotSetCmdMsg(warp)
        val playerWorld = player.world.name
        val allowedWorlds = this.feature.getAllowedWorlds()

        if (!player.isOp && !allowedWorlds.any { it.equals(playerWorld, true) }) return WarpTpNotAllowedCmdMsg(playerWorld, allowedWorlds.fancyJoin())

        this.feature.teleportPlayer(player, warpLocation)
        return WarpTpCmdMsg(warp, warpLocation)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class WarpCmdInfo(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "info", CmdArg.CUSTOM({ feature.getWarps() })) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val warp = args.text(0)
        val warpLocation = this.feature.getWarp(warp) ?: return WarpNotSetCmdMsg(warp)
        return WarpInfoCmdMsg(warp, warpLocation)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class WarpCmdSet(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "set", CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val warp = args.text(0)

        if (this.feature.getWarp(warp) != null) return WarpAlreadySetCmdMsg(warp)

        this.feature.setWarp(warp, player.location)
        return WarpSetCmdMsg(warp, player.location)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class WarpCmdRemove(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "remove", CmdArg.CUSTOM({ feature.getWarps() })) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val warp = args.text(0)
        val oldWarp = this.feature.removeWarp(warp) ?: return WarpNotSetCmdMsg(warp)
        return WarpRemoveCmdMsg(warp, oldWarp)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}
