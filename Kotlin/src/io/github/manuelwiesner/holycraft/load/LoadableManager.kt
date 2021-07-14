package io.github.manuelwiesner.holycraft.load

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.HolyCraftPlugin
import io.github.manuelwiesner.holycraft.logger.Logger
import org.bukkit.plugin.Plugin
import java.util.*

/**
 * The base class for each loadable manager.
 */
abstract class LoadableManager<I : Any, C : Any>(private val holyCraft: HolyCraft, name: String, getter: (() -> I)? = null) :
    _Loadable<I>(name, Logger.getLogger(name, holyCraft.getPlugin().logger), getter) {

    override fun getHolyCraft(): HolyCraft = this.holyCraft

    override fun getPlugin(): Plugin = this.holyCraft.getPlugin()

    /**
     * All created children of this manager, cached for loading/unloading.
     * -> only addable when unloaded
     */
    protected val childrenCache: MutableList<C> = Collections.synchronizedList(arrayListOf())
}