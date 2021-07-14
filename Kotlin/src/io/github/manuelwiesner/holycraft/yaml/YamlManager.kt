package io.github.manuelwiesner.holycraft.yaml

import io.github.manuelwiesner.holycraft.load.Loadable
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import java.util.*

/**
 * Allows easier access to a YAML config by providing wrappers for certain paths/keys,
 * which convert and cache the value at the provided path and only write it back when unloading.
 * Also saves the default config when loading and saves the current state to the disk when unloading.
 */
interface YamlManager : Loadable<FileConfiguration> {
    /**
     * Creates a YAML wrapper for String values.
     * @see getWrapper
     */
    fun getStringWrapper(path: String): Yaml<String> = getWrapper(path) { config, p -> config.getString(p) }

    /**
     * Creates a YAML wrapper for Boolean values.
     * @see getWrapper
     */
    fun getBooleanWrapper(path: String): Yaml<Boolean> = getWrapper(path) { config, p -> config.getBoolean(p) }

    /**
     * Creates a YAML wrapper for Int values.
     * @see getWrapper
     */
    fun getIntWrapper(path: String): Yaml<Int> = getWrapper(path) { config, p -> config.getInt(p) }

    /**
     * Creates a YAML wrapper for Double values.
     * @see getWrapper
     */
    fun getDoubleWrapper(path: String): Yaml<Double> = getWrapper(path) { config, p -> config.getDouble(p) }

    /**
     * Creates a YAML wrapper for Location values.
     * @see getWrapper
     */
    fun getLocationWrapper(path: String): Yaml<Location> = getWrapper(path) { config, p -> config.getLocation(p) }

    /**
     * Creates a YAML wrapper for UUID values.
     * @see getWrapper
     */
    fun getUUIDWrapper(path: String): Yaml<UUID> =
        getWrapper(path, { c, p -> c.getString(p)?.let { UUID.fromString(it) } }) { c, p, v -> c.set(p, v?.toString()) }

    /**
     * Creates a YAML wrapper for String-List values.
     * @see getWrapper
     */
    fun getStringListWrapper(path: String): Yaml<MutableList<String>> = getWrapper(path) { config, p -> config.getStringList(p) }

    /**
     * Creates a YAML wrapper for T values, via a custom getter and the default setter.
     * The context (YAML config) of this manager is used.
     * @see org.bukkit.configuration.ConfigurationSection.set
     */
    fun <T : Any> getWrapper(path: String, getter: (config: FileConfiguration, path: String) -> T?): Yaml<T> =
        getWrapper(path, getter) { config, p, item -> config.set(p, item) }

    /**
     * Creates a YAML wrapper for T values, via a custom getter and custom setter.
     * The context (YAML config) of this manager is used.
     */
    fun <T : Any> getWrapper(
        path: String,
        getter: (config: FileConfiguration, path: String) -> T?,
        setter: (config: FileConfiguration, path: String, item: T?) -> Unit
    ): Yaml<T>
}