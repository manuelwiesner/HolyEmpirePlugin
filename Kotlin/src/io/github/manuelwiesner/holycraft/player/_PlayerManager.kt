package io.github.manuelwiesner.holycraft.player

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.load.LoadableManager
import io.github.manuelwiesner.holycraft.store.Store
import io.github.manuelwiesner.holycraft.store.StoreConverter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of PlayerManager
 * @see PlayerManager
 */
class _PlayerManager(holyCraft: HolyCraft) : LoadableManager<Unit, _View<*>>(holyCraft, "PLAYER"), PlayerManager {
    /**
     * The underlying store with a layout of UUID-Map<String,String?>.
     */
    private val playerStore: Store<UUID, MutableMap<String, String>> = getHolyCraft().getStoreManager()
        .getUUIDStore("players", StoreConverter.MAP(StoreConverter.TEXT, StoreConverter.TEXT))

    /**
     * Load all cached Views.
     */
    override fun onLoad() {
        this.childrenCache.forEach { it.load() }
    }

    /**
     * Unload all cached Views.
     */
    override fun onUnload() {
        // clear all values since the caches will only update present values and
        // the store might contain values which have been removed from the cache
        this.playerStore.forEach { _, map -> map.clear() }

        this.childrenCache.forEach { it.unload() }
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        // clear all values since the caches will only update present values and
        // the store might contain values which have been removed from the cache
        this.playerStore.forEach { _, map -> map.clear() }

        this.childrenCache.forEach { it.saveToDisk() }
    }

    /**
     * Returns a View object accessing a single key on any player-object.
     * Also transforms the String values via a converter and caches them.
     */
    override fun <T : Any> getView(key: String, converter: StoreConverter<T>): View<T> {
        preLoad()
        if (this.childrenCache.find { it.key == key } != null) throw IllegalStateException("View for $key already registered!")
        return _View(this, key, converter).also { this.childrenCache += it }
    }

    /**
     * Returns a single player-object with key uniqueId.
     */
    fun getPlayer(uniqueId: UUID): MutableMap<String, String> {
        return this.playerStore.computeIfAbsent(uniqueId) { ConcurrentHashMap() }
    }

    /**
     * Performs action for each player-object.
     */
    fun forEachPlayer(action: (UUID, MutableMap<String, String>) -> Unit) {
        this.playerStore.forEach(action)
    }
}