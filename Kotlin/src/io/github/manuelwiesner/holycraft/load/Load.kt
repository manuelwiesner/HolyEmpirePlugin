package io.github.manuelwiesner.holycraft.load

/**
 * An object that can be loaded/unloaded.
 */
interface Load {
    /**
     * Called once each load/unload cycle, can throw
     */
    fun load()

    /**
     * Can be called at any time/any state and must not throw
     */
    fun unload()
}