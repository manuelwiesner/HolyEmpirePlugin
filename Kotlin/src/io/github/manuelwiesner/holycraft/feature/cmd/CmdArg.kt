package io.github.manuelwiesner.holycraft.feature.cmd

import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil

interface CmdArg {
    fun parseArg(sender: CommandSender, arg: String): Any

    fun completeArg(sender: CommandSender, arg: String): MutableList<String>?

    class TEXT(private val values: List<String> = emptyList()) : CmdArg {

        constructor(vararg values: String) : this(listOf(*values))

        override fun parseArg(sender: CommandSender, arg: String): Any {
            if (this.values.isEmpty()) return arg
            return this.values.find { it.equals(arg, true) }
                ?: throw InvalidArgCmdException(arg, this.values.fancyJoin())
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String>? {
            if (this.values.isEmpty()) return null
            return StringUtil.copyPartialMatches(arg, this.values, arrayListOf())
        }
    }

    class NUMBER(
        private val minMax: IntRange? = null,
        private val numberProvider: ((CommandSender) -> MutableList<String>)? = null
    ) : CmdArg {

        override fun parseArg(sender: CommandSender, arg: String): Any {
            val value = arg.toIntOrNull() ?: throw ArgNumberCmdException(arg)
            if (this.minMax == null || this.minMax.contains(value)) return value
            throw ArgNumberRangeCmdException(value, this.minMax.first, this.minMax.last)
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String>? {
            return this.numberProvider?.invoke(sender)
        }
    }

    class DECIMAL(
        private val minMax: ClosedFloatingPointRange<Double>? = null,
        private val numberProvider: ((CommandSender) -> MutableList<String>)? = null
    ) : CmdArg {

        override fun parseArg(sender: CommandSender, arg: String): Any {
            val value = arg.toDoubleOrNull() ?: throw ArgNumberCmdException(arg)
            if (this.minMax == null || this.minMax.contains(value)) return value
            throw ArgNumberRangeCmdException(value, this.minMax.start, this.minMax.endInclusive)
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String>? {
            return this.numberProvider?.invoke(sender)
        }
    }

    object FLAG : CmdArg {
        private val VALUES = listOf("true", "false")
        private val FANCY_VALUES = VALUES.fancyJoin()

        override fun parseArg(sender: CommandSender, arg: String): Any {
            return when {
                arg.equals("true", ignoreCase = true) -> true
                arg.equals("false", ignoreCase = true) -> false
                else -> throw InvalidArgCmdException(arg, FANCY_VALUES)
            }
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String> {
            return StringUtil.copyPartialMatches(arg, VALUES, arrayListOf())
        }
    }

    object ONLINE_PLAYER : CmdArg {
        override fun parseArg(sender: CommandSender, arg: String): Any {
            return Bukkit.getPlayer(arg)?.takeUnless { sender is Player && !sender.canSee(it) }
                ?: throw ArgOnlinePlayerCmdException(arg)
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String> {
            return sender.server.onlinePlayers
                .filter { (sender !is Player || sender.canSee(it)) && StringUtil.startsWithIgnoreCase(it.name, arg) }
                .mapTo(arrayListOf()) { it.name }
        }
    }

    object OFFLINE_PLAYER : CmdArg {
        override fun parseArg(sender: CommandSender, arg: String): Any {
            return sender.server.offlinePlayers.find { it.name?.equals(arg, true) ?: false }
                ?: throw ArgOfflinePlayerCmdException(arg)
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String> {
            return sender.server.offlinePlayers.mapNotNull { it.name }
                .filterTo(mutableListOf()) { StringUtil.startsWithIgnoreCase(it, arg) }
        }
    }

    class CUSTOM(
        private val completeProvider: (CommandSender) -> Iterable<String>,
        private val parseProvider: ((CommandSender) -> Iterable<String>)? = completeProvider
    ) : CmdArg {

        override fun parseArg(sender: CommandSender, arg: String): Any {
            val providedValues = this.parseProvider?.invoke(sender) ?: return arg
            return providedValues.find { it.equals(arg, true) }
                ?: throw InvalidArgCmdException(arg, providedValues.fancyJoin())
        }

        override fun completeArg(sender: CommandSender, arg: String): MutableList<String> {
            val providedValues = this.completeProvider(sender)
            return StringUtil.copyPartialMatches(arg, providedValues, arrayListOf())
        }
    }
}