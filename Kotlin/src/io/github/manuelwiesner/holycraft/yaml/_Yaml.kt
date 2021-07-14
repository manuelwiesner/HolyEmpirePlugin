package io.github.manuelwiesner.holycraft.yaml

import io.github.manuelwiesner.holycraft.load.LoadableBase
import org.bukkit.configuration.file.FileConfiguration

/**
 * Implementation of YAML, stores the path and custom getter/setter.
 * @see Yaml
 */
class _Yaml<T : Any>(
    manager: _YamlManager, private val path: String,
    private val getter: (config: FileConfiguration, path: String) -> T?,
    private val setter: (config: FileConfiguration, path: String, item: T?) -> Unit
) : LoadableBase<Unit, _YamlManager>(manager, path), Yaml<T> {
    /**
     * The cached item at path.
     */
    @Volatile
    private var cachedItem: T? = null

    /**
     * Caches the item at path from the YAML config.
     */
    override fun onLoad() {
        this.cachedItem = this.getter(getManager().getItem(), this.path)
    }

    /**
     * Writes the cached item back to the config.
     */
    override fun onUnload() {
        saveToDisk()
        this.cachedItem = null
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        runCatching { getManager().getItem() }.mapCatching { this.setter(it, this.path, this.cachedItem) }
            .onFailure { getLogger().warn("Failed to set ${this.path} to ${this.cachedItem} in config.yml", it) }
    }

    /**
     * Returns the cached value.
     */
    override fun get(): T? {
        postLoad()
        return this.cachedItem
    }

    /**
     * Sets the cached value.
     */
    override fun set(value: T?) {
        postLoad()
        this.cachedItem = value
    }

    override fun makeSafe(default: T): SafeYaml<T> {
        preLoad()
        return _SafeYaml(this, default)
    }
}