package io.github.manuelwiesner.holycraft.feature.event

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.load.Load
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

/**
 * The base for every event listener. Handles registering/unregistering of the listener.
 */
abstract class ListenerBase<T : FeatureBase<*>>(protected val feature: T) : Load, Listener {

    /**
     * Registers all event methods of this listener to the PluginManager.
     */
    final override fun load() {
        this.feature.getPlugin().let { it.server.pluginManager.registerEvents(this, it) }
    }

    /**
     * Unregisters all event methods of this listener from the HandlerList.
     */
    final override fun unload() {
        HandlerList.unregisterAll(this)
    }
}