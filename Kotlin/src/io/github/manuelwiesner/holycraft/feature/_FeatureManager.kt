package io.github.manuelwiesner.holycraft.feature

import io.github.manuelwiesner.holycraft.HolyCraft
import io.github.manuelwiesner.holycraft.feature.features.chestshop.ChestShopFeature
import io.github.manuelwiesner.holycraft.feature.features.cmds.CmdFeature
import io.github.manuelwiesner.holycraft.feature.features.cosmetics.CosmeticFeature
import io.github.manuelwiesner.holycraft.feature.features.economy.EconomyFeature
import io.github.manuelwiesner.holycraft.feature.features.log.LogFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.spawnprotect.SpawnProtectFeature
import io.github.manuelwiesner.holycraft.feature.features.tablist.TablistFeature
import io.github.manuelwiesner.holycraft.feature.features.update.UpdateFeature
import io.github.manuelwiesner.holycraft.feature.features.warp.WarpFeature
import io.github.manuelwiesner.holycraft.load.LoadableManager

/**
 * Implementation of FeatureManager.
 * @see FeatureManager
 */
class _FeatureManager(holyCraft: HolyCraft) : LoadableManager<Unit, FeatureBase<*>>(holyCraft, "FEATURE"), FeatureManager {

    /**
     * Registers all features statically.
     */
    init {
        this.childrenCache += CmdFeature(this)
        this.childrenCache += EconomyFeature(this)
        this.childrenCache += WarpFeature(this)
        this.childrenCache += TablistFeature(this)
        this.childrenCache += SpawnProtectFeature(this)
        this.childrenCache += LogFeature(this)
        this.childrenCache += PropertyFeature(this)
        this.childrenCache += ChestShopFeature(this)
        this.childrenCache += CosmeticFeature(this)
        this.childrenCache += UpdateFeature(this)
    }

    /**
     * Loads all features.
     */
    override fun onLoad() {
        this.childrenCache.forEach { it.load() }
    }

    /**
     * Unloads all features.
     */
    override fun onUnload() {
        this.childrenCache.asReversed().forEach { it.unload() }
    }

    /**
     * Saves all features
     */
    override fun saveToDisk() {
        this.childrenCache.asReversed().forEach { it.saveToDisk() }
    }

    /**
     * Gets all features
     */
    override fun getFeatures(): List<Feature> {
        return this.childrenCache
    }

    /**
     * Gets feature via name
     */
    override fun getFeature(name: String): Feature? {
        for (feature in getFeatures()) if (feature.getFeatureName().equals(name, true)) return feature
        return null
    }
}