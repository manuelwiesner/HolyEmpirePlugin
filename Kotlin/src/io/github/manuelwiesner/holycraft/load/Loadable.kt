package io.github.manuelwiesner.holycraft.load

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.HolyCraftPlugin
import io.github.manuelwiesner.holycraft.logger.Logger
import org.bukkit.plugin.Plugin

/**
 * Extends the capabilities of Load by providing additional functionality like logging, state checking etc.
 */
interface Loadable<T : Any> : Load {
    /**
     * The name of this object
     */
    fun getName(): String

    /**
     * The logger of this object
     */
    fun getLogger(): Logger

    /**
     * The HolyCraft instance owning this Loadable
     */
    fun getHolyCraft(): HolyCraft

    /**
     * The plugin owning this Loadable
     */
    fun getPlugin(): Plugin

    /**
     * Some dependency used by this object
     * Only safe to call if isLoaded() returns true
     */
    fun getItem(): T

    /**
     * Whether this object is loaded or not
     */
    fun isLoaded(): Boolean

    /**
     * Used for saving the current status of the server while its running.
     */
    fun saveToDisk()
}

/**
 * Implementation of Loadable, provides all functionality described above
 */
abstract class _Loadable<T : Any>(private val name: String, private val logger: Logger, private val getter: (() -> T)? = null) : Loadable<T> {

    private var loaded: Boolean = false
    private var item: T? = null

    final override fun getName(): String = this.name

    final override fun getLogger(): Logger = this.logger

    final override fun getItem(): T = this.item ?: throw IllegalStateException("${getName()}: Item is null!")

    final override fun isLoaded(): Boolean = this.loaded

    final override fun load() {
        preLoad()

        kotlin.runCatching {
            this.getLogger().trace("Loading...")
            this.item = this.getter?.invoke()
            this.onLoad()
            this.loaded = true
            this.getLogger().debug("Loaded successfully!")
        }.onFailure {
            this.getLogger().error("Failed to load!")
            throw it
        }
    }

    final override fun unload() {
        if (!this.loaded) return

        this.getLogger().trace("Unloading...")
        this.onUnload()
        this.item = null
        this.loaded = false
        this.getLogger().debug("Unloaded successfully!")
    }

    protected fun preLoad() {
        if (this.loaded) throw IllegalStateException("${getName()} is already loaded!")
    }

    protected fun postLoad() {
        if (!this.loaded) throw IllegalStateException("${getName()} is not loaded!")
    }

    protected abstract fun onLoad()
    protected abstract fun onUnload()
}