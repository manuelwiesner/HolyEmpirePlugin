package io.github.manuelwiesner.holycraft.player

import java.util.*

/**
 * Allows access to a specific key across all players (instead of getting a player object, checking if it exists,
 * getting the String data for some key and then converting it to T, just use a View which stores a converter and the key)
 * The values are cached and only converted if loading/saving.
 */
interface View<T : Any> {
    /**
     * Gets the cached value of uniqueId.
     */
    operator fun get(uniqueId: UUID): T?

    /**
     * Sets the cached value of uniqueId.
     */
    operator fun set(uniqueId: UUID, value: T)

    /**
     * Removes the cached value of uniqueId.
     */
    fun remove(uniqueId: UUID): T?

    /**
     * Computes the function compute if key is missing.
     */
    fun computeIfAbsent(uniqueId: UUID, compute: (UUID) -> T): T

    /**
     * Runs the consumer for each player object cached in this View.
     */
    fun forEach(consumer: (UUID, T) -> Unit)

    /**
     * Clears all values of this key.
     */
    fun clear()
}