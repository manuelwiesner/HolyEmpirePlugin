package io.github.manuelwiesner.holycraft.yaml

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.load.LoadableManager
import org.bukkit.configuration.file.FileConfiguration

/**
 * Implementation of YamlManager
 * @see YamlManager
 */
class _YamlManager(holyCraft: HolyCraft) : LoadableManager<FileConfiguration, _Yaml<*>>(
    holyCraft,
    "YAML",
    { holyCraft.getPlugin().reloadConfig(); holyCraft.getPlugin().config }
), YamlManager {

    /**
     * Saves default config and loads wrappers.
     */
    override fun onLoad() {
        this.getPlugin().saveDefaultConfig()
        this.childrenCache.forEach { it.load() }
    }

    /**
     * Unloads wrappers and saves the config to the disk.
     */
    override fun onUnload() {
        this.childrenCache.forEach { it.unload() }
        this.getPlugin().saveConfig()
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        this.childrenCache.forEach { it.saveToDisk() }
        this.getPlugin().saveConfig()
    }

    /**
     * Creates a new wrapper at path with custom getter/setter methods. Also caches the created _YAML for loading/unloading
     * -> Method can only be called before loading.
     */
    override fun <T : Any> getWrapper(
        path: String,
        getter: (config: FileConfiguration, path: String) -> T?,
        setter: (config: FileConfiguration, path: String, item: T?) -> Unit
    ): Yaml<T> {
        preLoad()
        return _Yaml(this, path, getter, setter).also { this.childrenCache += it }
    }
}