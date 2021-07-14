package io.github.manuelwiesner.holycraft.yaml

/**
 * A wrapper for a certain path in a YAML config. The value at the provided path is cached when loading and
 * only written to the config when unloading.
 */
interface Yaml<T : Any> {
    /**
     * Gets the converted, cached value at the provided path.
     */
    fun get(): T?

    /**
     * Gets the converted, cached value at the provided path or if it is null returns def.
     */
    fun get(def: T): T = get() ?: def

    /**
     * Sets the cached value to value.
     */
    fun set(value: T?)

    /**
     * Returns a SafeYaml backed by this Yaml, basically defines a default value which will always be used if the
     * value is null.
     */
    fun makeSafe(default: T): SafeYaml<T>
}