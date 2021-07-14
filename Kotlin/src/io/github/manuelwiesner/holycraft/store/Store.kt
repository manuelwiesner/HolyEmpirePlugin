package io.github.manuelwiesner.holycraft.store

import java.util.concurrent.ConcurrentHashMap

/**
 * A key/value store which utilizes a cache and loads/saves it via converters back to JSON.
 */
interface Store<K : Any, V : Any> {
    /**
     * Gets the cached value for a key.
     */
    operator fun get(key: K): V?

    /**
     * Sets the cached value for a key.
     */
    operator fun set(key: K, value: V)

    /**
     * Checks if a value is cached for a key.
     */
    operator fun contains(key: K): Boolean

    /**
     * Removes the cached value for a key.
     */
    fun remove(key: K): V?

    /**
     * Computes the function compute if key is missing.
     */
    fun computeIfAbsent(key: K, compute: (K) -> V): V

    /**
     * Executes the action for each element in this store.
     */
    fun forEach(action: (K, V) -> Unit)

    /**
     * Clears all keys/values of this store.
     */
    fun clear()

    /**
     * Returns the raw cache-map of this store.
     */
    fun raw(): ConcurrentHashMap<K, V>
}