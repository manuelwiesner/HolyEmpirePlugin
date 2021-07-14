package io.github.manuelwiesner.holycraft.yaml

/**
 * Is used as a wrapper for a 'normal' Yaml and always provides a default value if the value is null.
 */
interface SafeYaml<T : Any> {
    /**
     * Gets the converted, cached value at the provided path.
     */
    fun get(): T

    /**
     * Sets the cached value to value.
     */
    fun set(value: T)
}