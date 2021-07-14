package io.github.manuelwiesner.holycraft.feature

import io.github.manuelwiesner.holycraft.load.Load

/**
 * Represents a feature of this plugin. Features implement the actual functionality of the plugin, everything else is
 * just supporting the features.
 */
interface Feature {

    /**
     * Returns all children of this feature.
     */
    fun getFeatureChildren(): List<Load>

    /**
     * Returns the name of this feature.
     */
    fun getFeatureName(): String
}