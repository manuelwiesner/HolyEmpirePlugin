package io.github.manuelwiesner.holycraft.feature.features.warp

import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender
import org.bukkit.event.player.PlayerTeleportEvent

private const val SPAWN_NAME = "spawn"

class SpawnCmd(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "spawn") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val warpLocation = this.feature.getWarp(SPAWN_NAME) ?: return WarpNotSetCmdMsg(SPAWN_NAME)
        val playerWorld = player.world.name
        val allowedWorlds = this.feature.getAllowedWorlds()

        if (!player.isOp && !allowedWorlds.any { it.equals(playerWorld, true) }) return WarpTpNotAllowedCmdMsg(playerWorld, allowedWorlds.fancyJoin())

        this.feature.teleportPlayer(player, warpLocation)
        return WarpTpCmdMsg(SPAWN_NAME, warpLocation)
    }
}
