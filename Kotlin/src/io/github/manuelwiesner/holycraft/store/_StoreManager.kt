package io.github.manuelwiesner.holycraft.store

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.load.LoadableManager
import java.io.File
import java.nio.charset.Charset

/**
 * Implementation of StoreManager.
 * @see StoreManager
 */
class _StoreManager(holyCraft: HolyCraft) : LoadableManager<File, _Store<*, *>>(holyCraft, "STORE", { holyCraft.getPlugin().dataFolder }), StoreManager {

    /**
     * Creates the parent directory and loads all registered stores.
     */
    override fun onLoad() {
        getItem().mkdirs()

        this.childrenCache.forEach { it.load() }
    }

    /**
     * Creates the parent directory and saves all registered stores.
     */
    override fun onUnload() {
        runCatching { getItem() }.mapCatching { it.mkdirs() }
            .onFailure { getLogger().warn("Failed to create plugin directory!", it); return }

        this.childrenCache.forEach { it.unload() }
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        runCatching { getItem() }.mapCatching { it.mkdirs() }
            .onFailure { getLogger().warn("Failed to create plugin directory!", it); return }

        this.childrenCache.forEach { it.saveToDisk() }
    }

    /**
     * Returns a store with the provided name and converters.
     */
    override fun <K : Any, V : Any> getStore(
        name: String,
        keyConverter: StoreConverter<K>,
        valueConverter: StoreConverter<V>,
        charset: Charset
    ): Store<K, V> {
        preLoad()
        return _Store(this, name, keyConverter, valueConverter, charset).also { this.childrenCache += it }
    }
}