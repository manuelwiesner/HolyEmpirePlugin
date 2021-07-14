package io.github.manuelwiesner.holycraft

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class HolyCraftPlugin : JavaPlugin() {

    private val holyCraft: HolyCraft = HolyCraft(this)

    override fun onEnable() {
        kotlin.runCatching {
            this.holyCraft.load()
        }.onFailure {
            this.logger.severe("Failed to load plugin!")
            onDisable()
            throw it
        }
    }

    override fun onDisable() {
        this.holyCraft.unload()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        this.logger.severe("Command ${command.name} is registered via plugin.yml but not programmatically!".also {
            sender.sendMessage(arrayOf("Please contact a dev: Error in command ${command.name}!", it))
        })
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        this.logger.severe("Command ${command.name} is registered via plugin.yml but not programmatically!")
        return emptyList()
    }
}