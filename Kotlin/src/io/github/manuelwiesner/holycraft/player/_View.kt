package io.github.manuelwiesner.holycraft.player

import io.github.manuelwiesner.holycraft.load.LoadableBase
import io.github.manuelwiesner.holycraft.store.StoreConverter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of View
 * @see View
 */
class _View<T : Any>(manager: _PlayerManager, val key: String, private val converter: StoreConverter<T>) :
    LoadableBase<Unit, _PlayerManager>(manager, key), View<T> {

    /**
     * Cached, converted values of key mapped to each player.
     */
    private val valueCache: ConcurrentHashMap<UUID, T> = ConcurrentHashMap()

    /**
     * Checks all player data and converts/caches it.
     */
    override fun onLoad() {
        this.valueCache.clear()
        getManager().forEachPlayer { uuid, storeMap -> storeMap[this.key]?.let { this.converter.fromString(it) }?.let { this.valueCache[uuid] = it } }
    }

    /**
     * Converts/saves all cached data back to the main store.
     */
    override fun onUnload() {
        saveToDisk()
        this.valueCache.clear()
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        this.valueCache.forEach { (uuid, value) -> getManager().getPlayer(uuid)[this.key] = this.converter.toString(value) }
    }

    /**
     * Gets the cached value of uniqueId.
     */
    override fun get(uniqueId: UUID): T? {
        postLoad()
        return this.valueCache[uniqueId]
    }

    /**
     * Sets the cached value of uniqueId.
     */
    override fun set(uniqueId: UUID, value: T) {
        postLoad()
        this.valueCache[uniqueId] = value
    }

    /**
     * Removes the cached element.
     */
    override fun remove(uniqueId: UUID): T? {
        postLoad()
        return this.valueCache.remove(uniqueId)
    }

    /**
     * Computes the function compute if uniqueId is missing
     */
    override fun computeIfAbsent(uniqueId: UUID, compute: (UUID) -> T): T {
        postLoad()
        return this.valueCache.computeIfAbsent(uniqueId, compute)
    }

    /**
     * Runs the consumer for each player object cached in this View.
     */
    override fun forEach(consumer: (UUID, T) -> Unit) {
        postLoad()
        this.valueCache.forEach(consumer)
    }

    /**
     * Clears all values.
     */
    override fun clear() {
        this.valueCache.clear()
    }
}