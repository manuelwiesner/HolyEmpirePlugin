package io.github.manuelwiesner.holycraft.feature.features.tablist

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender

class ClanTagCmd(feature: TablistFeature) : CmdBaseRoot<TablistFeature>(
    feature, "clantag", ClanTagCmdSet(feature), ClanTagCmdRemove(feature),
    ClanTagCmdAdminSet(feature)
)

class ClanTagCmdSet(feature: TablistFeature) : CmdBaseArgs<TablistFeature>(feature, "set", CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        val value = args.text(0)
        val maxLength = this.feature.getMaxPrefixLength()
        if (!sender.isOp && value.length > maxLength) return ClanTagTooLongCmdMsg(value, maxLength)

        this.feature.setClanTag(player, value)
        return ClanTagSetCmdMsg(value)
    }
}

class ClanTagCmdRemove(feature: TablistFeature) : CmdBaseArgs<TablistFeature>(feature, "remove") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val oldTag = this.feature.removeClanTag(ensurePlayer(sender))
        return if (oldTag == null) ClanTagNotSetCmdMsg() else ClanTagRemoveCmdMsg(oldTag)
    }
}

class ClanTagCmdAdminSet(feature: TablistFeature) : CmdBaseArgs<TablistFeature>(feature, "admin-set", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT()) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = args.offlinePlayer(0)
        val value = args.text(1)

        this.feature.setClanTag(player, value)
        return ClanTagAdminSetCmdMsg(player, value)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}