package io.github.manuelwiesner.holycraft.feature.cmd

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature.msg.ArgCountCmdException
import io.github.manuelwiesner.holycraft.feature.msg.Message
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * The base for all command actual command implementation.
 * E.g.: /home set/remove: home is a CmdBaseRoot and set/remove are CmdBaseArgs.
 */
abstract class CmdBaseArgs<T : FeatureBase<*>>(feature: T, name: String, private vararg val cmdArgs: CmdArg) : CmdBase<T>(feature, name) {

    /**
     * Checks if all arguments match and if so executes this command. Throws if arguments don't match.
     */
    final override fun onCommand(sender: CommandSender, args: List<String>): Message? {
        if (this.cmdArgs.size != args.size) throw ArgCountCmdException(this.name, this.cmdArgs.size)

        return executeCommand(sender, this.cmdArgs.mapIndexed { i, arg -> arg.parseArg(sender, args[i]) })
    }

    /**
     * Auto completes the arguments of this command.
     */
    final override fun onTabComplete(sender: CommandSender, args: List<String>): MutableList<String>? {
        if (args.size > this.cmdArgs.size) return null

        return kotlin.runCatching {
            args.dropLast(1).forEachIndexed { i, arg -> this.cmdArgs[i].parseArg(sender, arg) }
            this.cmdArgs[args.lastIndex].completeArg(sender, args[args.lastIndex])
        }.getOrNull()
    }

    /**
     * Actual implementation of command execution.
     */
    abstract fun executeCommand(sender: CommandSender, args: List<Any>): Message?

    fun List<Any>.text(index: Int) = this[index] as String
    fun List<Any>.number(index: Int) = this[index] as Int
    fun List<Any>.decimal(index: Int) = this[index] as Double
    fun List<Any>.bool(index: Int) = this[index] as Boolean
    fun List<Any>.onlinePlayer(index: Int) = this[index] as Player
    fun List<Any>.offlinePlayer(index: Int) = this[index] as OfflinePlayer
}