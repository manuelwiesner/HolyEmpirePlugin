package io.github.manuelwiesner.holycraft.feature.cmd

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature.msg.InvalidArgCmdException
import io.github.manuelwiesner.holycraft.feature.msg.Message
import io.github.manuelwiesner.holycraft.feature.msg.fancyJoin
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil

/**
 * The base for all command which have further children
 * E.g. '/home' is a root command with the children 'set' and 'remove'
 */
abstract class CmdBaseRoot<T : FeatureBase<*>>(feature: T, name: String, private vararg val children: CmdBase<T>) : CmdBase<T>(feature, name) {

    /**
     * Checks if any child matches the first arg and executes it.
     * Special case is a blank child, which means no arg required.
     * Throws if no match is found
     */
    final override fun onCommand(sender: CommandSender, args: List<String>): Message? {
        val child = this.children.find {

            it.acceptsSender(sender) &&
                    if (args.isEmpty()) it.name.isBlank()
                    else it.name.equals(args.first(), true)

        } ?: throw InvalidArgCmdException(args.firstOrNull(),
                this.children.filter { it.acceptsSender(sender) }.fancyJoin { it.name })

        return child.onCommand(sender, args.drop(1))
    }

    /**
     * Checks which children match the given args and returns possible completions.
     */
    final override fun onTabComplete(sender: CommandSender, args: List<String>): MutableList<String>? {
        val possibleMatches = this.children.filter { it.acceptsSender(sender) && it.name.isNotBlank() }

        return when {
            args.isEmpty() -> possibleMatches.mapTo(arrayListOf()) { it.name }
            args.size == 1 -> possibleMatches.map { it.name }.filterTo(arrayListOf()) { StringUtil.startsWithIgnoreCase(it, args.first()) }
            else -> possibleMatches.firstOrNull { it.name.equals(args.first(), true) }?.onTabComplete(sender, args.drop(1))
        }
    }
}