package io.github.manuelwiesner.holycraft.feature

import io.github.manuelwiesner.holycraft.load.Loadable

/**
 * Manages all features of this plugin. A feature actually implements a function of this plugin which result in actual
 * behavioral differences on the server, e.g. home locations.
 */
interface FeatureManager : Loadable<Unit> {

    /**
     * Returns all loaded features.
     */
    fun getFeatures(): List<Feature>

    /**
     * Returns the feature with name if found, otherwise null.
     */
    fun getFeature(name: String): Feature?
}

/**
 * Workaround for the missing inline ability of interfaces, gets a Feature via its type
 */
inline fun <reified T : Feature> FeatureManager.getFeature(): T? {
    for (feature in getFeatures()) if (feature is T) return feature
    return null
}