package io.github.manuelwiesner.holycraft.load

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.HolyCraftPlugin
import io.github.manuelwiesner.holycraft.logger.Logger
import org.bukkit.plugin.Plugin

/**
 * The base class for each managed loadable, provides some additional functionality.
 */
abstract class LoadableBase<T : Any, MGR : Loadable<*>>(private val manager: MGR, name: String, getter: (() -> T)? = null) :
    _Loadable<T>(manager.name(name), manager.log(name), getter) {

    fun getManager(): MGR = this.manager

    override fun getHolyCraft(): HolyCraft = this.manager.getHolyCraft()

    override fun getPlugin(): Plugin = this.manager.getPlugin()
}

private fun Loadable<*>.name(name: String): String = "${getName()}-$name"

private fun Loadable<*>.log(name: String): Logger = Logger.getLogger(name(name), getPlugin().logger)