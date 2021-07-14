package io.github.manuelwiesner.holycraft.feature

import io.github.manuelwiesner.holycraft.load.Load
import io.github.manuelwiesner.holycraft.load.LoadableBase

/**
 * Implementation of Feature. Is the base class for every feature, provides some basic functionality like child loading
 * and sending of a message.
 * @see Feature
 */
abstract class FeatureBase<T : Any>(manager: _FeatureManager, private val featureName: String, getter: (() -> T)? = null) :
    LoadableBase<T, _FeatureManager>(manager, featureName, getter), Feature {

    /**
     * All children of this feature e.g. commands, listeners etc.
     * Is loaded after loadFeature() is called
     */
    protected val children: MutableList<Load> = arrayListOf()

    /**
     * First loads the feature then the children.
     */
    final override fun onLoad() {
        loadFeature()
        this.children.forEach { it.load() }
    }

    /**
     * First unloads the children in reverse then the feature.
     */
    final override fun onUnload() {
        this.children.asReversed().forEach { it.unload() }
        unloadFeature()
    }

    override fun saveToDisk() {}

    final override fun getFeatureChildren() = this.children
    final override fun getFeatureName() = this.featureName

    protected open fun loadFeature() {}
    protected open fun unloadFeature() {}
}
