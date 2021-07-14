package io.github.manuelwiesner.holycraft.yaml

/**
 * Implementation of SafeYaml
 * @see SafeYaml
 */
class _SafeYaml<T : Any>(private val wrapped: Yaml<T>, private val default: T) : SafeYaml<T> {
    /**
     * Gets the value or the default if null. Also sets the value if null.
     */
    override fun get(): T {
        return this.wrapped.get() ?: this.default.also { this.wrapped.set(it) }
    }

    /**
     * Sets the non-null value.
     */
    override fun set(value: T) {
        this.wrapped.set(value)
    }
}