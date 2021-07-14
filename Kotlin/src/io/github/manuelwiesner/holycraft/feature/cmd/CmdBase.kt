package io.github.manuelwiesner.holycraft.feature.cmd

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature.msg.*
import io.github.manuelwiesner.holycraft.load.Load
import io.github.manuelwiesner.holycraft.logger.Level
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.ArrayList

/**
 * The base for every command. Handles registering this as the executor of the command 'name'.
 */
abstract class CmdBase<T : FeatureBase<*>>(protected val feature: T, val name: String) : Load, TabExecutor {

    /**
     * The PluginCommand associated to this command.
     */
    private var pluginCommand: PluginCommand? = null

    /**
     * Returns the pluginCommand of this as non-null since when loaded it can't be null.
     */
    fun getPluginCommand(): PluginCommand {
        return this.pluginCommand!!
    }

    /**
     * Registers this as the executor of the associated PluginCommand.
     */
    final override fun load() {
        // took code from the JavaPlugin class since we don't have access to that method through the Plugin interface.
        val plugin = this.feature.getPlugin()

        val alias = this.name.toLowerCase(Locale.ENGLISH)
        var command = plugin.server.getPluginCommand(alias)

        if (command == null || command.plugin != plugin) {
            val name = plugin.description.name.toLowerCase(Locale.ENGLISH)
            command = plugin.server.getPluginCommand("$name:$alias")
        }

        if (command != null && command.plugin == plugin) {
            this.pluginCommand = command
            this.pluginCommand?.setExecutor(this)
        } else {
            throw IllegalStateException("Command $name is not registered in the plugin.yml!")
        }
    }

    /**
     * Unregisters this as the executor of the associated PluginCommand.
     */
    final override fun unload() {
        this.pluginCommand?.setExecutor(null)
        this.pluginCommand = null
    }

    final override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        log(Level.DEBUG, "Executing command for ${sender.name}: [${args.joinToString()}]...")

        try {

            if (!acceptsSender(sender)) throw UnknownCmdException(label)
            onCommand(sender, args.toList())?.sendMessage(this.feature, sender)

        } catch (e: ReturnMessageException) {

            e.sendMessage(this.feature, sender)

        } catch (e: MessageException) {

            log(Level.TRACE, "Execution failed: ${e.key}!")
            e.sendMessage(this.feature, sender)

        } catch (e: Throwable) {

            val name = e::class.simpleName ?: "Error"
            log(Level.ERROR, "Execution failed unhandled: $name", e)
            UnhandledCmdException(this.name, name, e.localizedMessage).sendMessage(this.feature, sender)

        }

        return true
    }

    final override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        log(Level.MONITOR, "Tab completing for ${sender.name}: [${args.joinToString()}]...")

        return onTabComplete(sender, args.toList()) ?: ArrayList(0)
    }

    protected fun log(lvl: Level, msg: String, t: Throwable? = null) = this.feature.getLogger().log(lvl, "[CMD-$name] $msg", t)
    protected fun ensurePlayer(sender: CommandSender): Player = sender as? Player ?: throw OnlyPlayerException(name)

    open fun acceptsSender(sender: CommandSender): Boolean = true
    abstract fun onCommand(sender: CommandSender, args: List<String>): Message?
    abstract fun onTabComplete(sender: CommandSender, args: List<String>): MutableList<String>?
}