package io.github.manuelwiesner.holycraft.feature.features.warp

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class NamedHomeCmd(feature: WarpFeature) : CmdBaseRoot<WarpFeature>(feature, "nhome", NamedHomeCmdTp(feature),
        NamedHomeCmdSet(feature), NamedHomeCmdRemove(feature), NamedHomeCmdAdmin(feature)) {
    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

fun WarpFeature.getHomes(sender: CommandSender): Iterable<String> {
    if (sender !is Player) return emptySet()
    return this.getPlayerHomes(sender.uniqueId) ?: emptySet()
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdTp(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "tp", CmdArg.CUSTOM({ feature.getHomes(it) })) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val homeName = args.text(0)
        val homeLocation = this.feature.getPlayerHome(player.uniqueId, homeName) ?: return NHomeNotSetCmdMsg(homeName)
        val playerWorld = player.world.name
        val allowedWorlds = this.feature.getAllowedWorlds()

        if (!player.isOp && !allowedWorlds.any { it.equals(playerWorld, true) }) return NHomeTpNotAllowedCmdMsg(playerWorld, allowedWorlds.fancyJoin())

        this.feature.teleportPlayer(player, homeLocation)
        return NHomeTpCmdMsg(homeName, homeLocation)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdSet(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "set", CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val homeName = args.text(0)
        val playerWorld = player.world.name
        val allowedWorlds = this.feature.getAllowedWorlds()

        if (!player.isOp && !allowedWorlds.any { it.equals(playerWorld, true) }) return NHomeSetNotAllowedCmdMsg(playerWorld, allowedWorlds.fancyJoin())

        if (this.feature.getPlayerHome(player.uniqueId, homeName) != null) return NHomeAlreadySetCmdMsg(homeName)

        this.feature.setPlayerHome(player.uniqueId, homeName, player.location)
        return NHomeSetCmdMsg(homeName, player.location)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdRemove(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "remove", CmdArg.CUSTOM({ feature.getHomes(it) })) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val homeName = args.text(0)
        val oldHome = this.feature.removePlayerHome(player.uniqueId, homeName) ?: return NHomeNotSetCmdMsg(homeName)
        return NHomeRemoveCmdMsg(homeName, oldHome)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdAdmin(feature: WarpFeature) : CmdBaseRoot<WarpFeature>(feature, "admin", NamedHomeCmdAdminList(feature),
        NamedHomeCmdAdminTp(feature), NamedHomeCmdAdminSet(feature), NamedHomeCmdAdminRemove(feature)) {
    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdAdminList(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "list", CmdArg.OFFLINE_PLAYER) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val homePlayer = args.offlinePlayer(0)
        val homePlayerName = homePlayer.name ?: homePlayer.uniqueId.toString()
        val homes = this.feature.getPlayerHomes(homePlayer.uniqueId)?.fancyJoin()
                ?: return NHomeAdminNoHomesCmdMsg(homePlayerName)

        return NHomeAdminListCmdMsg(homePlayerName, homes)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdAdminTp(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "tp", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val adminPlayer = ensurePlayer(sender)
        val homePlayer = args.offlinePlayer(0)
        val homePlayerName = homePlayer.name ?: homePlayer.uniqueId.toString()
        val homeName = args.text(1)

        val homeLocation = this.feature.getPlayerHome(homePlayer.uniqueId, homeName)
                ?: return NHomeAdminNotSetCmdMsg(homePlayerName, homeName)

        this.feature.teleportPlayer(adminPlayer, homeLocation)
        return NHomeAdminTpCmdMsg(homePlayerName, homeName, homeLocation)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdAdminSet(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "set", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val adminPlayer = ensurePlayer(sender)
        val homePlayer = args.offlinePlayer(0)
        val homePlayerName = homePlayer.name ?: homePlayer.uniqueId.toString()
        val homeName = args.text(1)

        if (this.feature.getPlayerHome(homePlayer.uniqueId, homeName) != null) return NHomeAdminAlreadySetCmdMsg(homePlayerName, homeName)

        this.feature.setPlayerHome(homePlayer.uniqueId, homeName, adminPlayer.location)
        return NHomeAdminSetCmdMsg(homePlayerName, homeName, adminPlayer.location)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class NamedHomeCmdAdminRemove(feature: WarpFeature) : CmdBaseArgs<WarpFeature>(feature, "remove", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val homePlayer = args.offlinePlayer(0)
        val homePlayerName = homePlayer.name ?: homePlayer.uniqueId.toString()
        val homeName = args.text(1)
        val oldHome = this.feature.removePlayerHome(homePlayer.uniqueId, homeName)
                ?: return NHomeAdminNotSetCmdMsg(homePlayerName, homeName)

        return NHomeAdminRemoveCmdMsg(homePlayerName, homeName, oldHome)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}