package io.github.manuelwiesner.holycraft.feature.features.warp

import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerTeleportEvent

private const val HOME_NAME = "default"

class HomeCmd(feature: WarpFeature) : CmdBaseRoot<WarpFeature>(feature, "home", HomeCmdTp(feature), HomeCmdSet(feature), HomeCmdRemove(feature))

class HomeCmdTp(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val homeLocation = this.feature.getPlayerHome(player.uniqueId, HOME_NAME) ?: return HomeNotSetCmdMsg()
        val playerWorld = player.world.name
        val allowedWorlds = this.feature.getAllowedWorlds()

        if (!player.isOp && !allowedWorlds.any { it.equals(playerWorld, true) }) return HomeTpNotAllowedCmdMsg(playerWorld, allowedWorlds.fancyJoin())

        this.feature.teleportPlayer(player, homeLocation)
        return HomeTpCmdMsg(homeLocation)
    }
}

class HomeCmdSet(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "set") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val playerWorld = player.world.name
        val allowedWorlds = this.feature.getAllowedWorlds()

        if (!player.isOp && !allowedWorlds.any { it.equals(playerWorld, true) }) return HomeSetNotAllowedCmdMsg(playerWorld, allowedWorlds.fancyJoin())

        if (this.feature.getPlayerHome(player.uniqueId, HOME_NAME) != null) return HomeAlreadySetCmdMsg()

        this.feature.setPlayerHome(player.uniqueId, HOME_NAME, player.location)
        return HomeSetCmdMsg(player.location)
    }
}

class HomeCmdRemove(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "remove") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val oldHome = this.feature.removePlayerHome(player.uniqueId, HOME_NAME) ?: return HomeNotSetCmdMsg()
        return HomeRemoveCmdMsg(oldHome)
    }
}