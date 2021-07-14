package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.cmd.*
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import kotlin.math.ceil

class HelpCmd(feature: CmdFeature) : CmdBase<CmdFeature>(feature, "help") {

    private val cmdArg: CmdArgCmd = CmdArgCmd(this.feature)

    override fun onCommand(sender: CommandSender, args: List<String>): Message? {
        when {
            args.isEmpty() -> printHelpPage(sender, 0)
            args.size == 1 -> {
                args.first().let { arg ->
                    arg.toIntOrNull()?.let { n ->
                        printHelpPage(sender, n - 1)
                    } ?: printHelpFor(sender, arg)
                }
            }
            else -> throw ArgCountCmdException(this.name, 1)
        }

        return null
    }

    override fun onTabComplete(sender: CommandSender, args: List<String>): MutableList<String>? {
        return if (args.size == 1) {
            this.cmdArg.completeArg(sender, args.first())
        } else null
    }

    private fun printHelpPage(sender: CommandSender, page: Int) {
        val availableCommands = this.feature.getAllCommands().filter { it.acceptsSender(sender) }
        val lastHelpPage = ceil(availableCommands.size / 7.0).toInt()

        if (page < 0 || page >= lastHelpPage) throw ArgNumberRangeCmdException(page + 1, 1, lastHelpPage)

        HelpPageHeaderCmdMsg(page + 1, lastHelpPage).sendMessage(this.feature, sender)

        availableCommands.subList(page * 7, ((page * 7) + 7).coerceAtMost(availableCommands.size)).forEach { cmd ->
            val descr = this.feature.getInfo(sender, cmd, "descr")?.joinToString()
            HelpPageEntryCmdMsg(cmd.name, descr ?: "-----").sendMessage(this.feature, sender)
        }

        HelpPageFooterCmdMsg().sendMessage(this.feature, sender)
    }

    private fun printHelpFor(sender: CommandSender, command: String) {
        val cmd = this.cmdArg.parseArg(sender, command) as CmdBase<*>
        val descr = this.feature.getInfo(sender, cmd, "descr")
        val usage = this.feature.getInfo(sender, cmd, "usage")

        HelpForHeaderCmdMsg(cmd.name).sendMessage(this.feature, sender)

        descr?.forEach { HelpForDescrCmdMsg(it).sendMessage(this.feature, sender) }
        usage?.forEach { HelpForUsageCmdMsg(it).sendMessage(this.feature, sender) }
        cmd.getPluginCommand().aliases.takeIf { it.isNotEmpty() }?.let {
            HelpForAliasesCmdMsg(it.fancyJoin()).sendMessage(this.feature, sender)
        }

        HelpForFooterCmdMsg().sendMessage(this.feature, sender)
    }
}

private fun CmdFeature.getInfo(sender: CommandSender, command: CmdBase<*>, type: String): List<String>? {
    return getHolyCraft().getLangManager().getLanguage(sender).getTranslation("help.$type.${command.name}", arrayOf(command.name))
}

private class CmdArgCmd(private val feature: CmdFeature) : CmdArg {

    override fun parseArg(sender: CommandSender, arg: String): Any {
        val allowedCommands = compileAllowedCommands(sender)
        return allowedCommands[arg.toLowerCase()] ?: throw InvalidArgCmdException(arg, allowedCommands.keys.fancyJoin())
    }

    override fun completeArg(sender: CommandSender, arg: String): MutableList<String> {
        return StringUtil.copyPartialMatches(arg, compileAllowedCommands(sender).keys, arrayListOf())
    }

    private fun compileAllowedCommands(sender: CommandSender): MutableMap<String, CmdBase<*>> {
        return hashMapOf<String, CmdBase<*>>().also { map ->
            this.feature.getAllCommands().filter { it.acceptsSender(sender) }.forEach { cmd ->
                cmd.getPluginCommand().aliases.forEach { alias -> map[alias] = cmd }
                map[cmd.name] = cmd
            }
        }
    }
}