package io.github.manuelwiesner.holycraft.store

import io.github.manuelwiesner.holycraft.load.Loadable
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * Provides persistent storage via key/value Stores which use converters to save/load json files.
 */
interface StoreManager : Loadable<File> {
    /**
     * Creates a Store object with String keys and T values.
     */
    fun <T : Any> getStringStore(name: String, valueConverter: StoreConverter<T>, charset: Charset = Charsets.UTF_8): Store<String, T> {
        return getStore(name, StoreConverter.TEXT, valueConverter, charset)
    }

    /**
     * Creates a Store object with UUID keys and T values.
     */
    fun <T : Any> getUUIDStore(name: String, valueConverter: StoreConverter<T>, charset: Charset = Charsets.UTF_8): Store<UUID, T> {
        return getStore(name, StoreConverter.UUID, valueConverter, charset)
    }

    /**
     * Creates a Store object with custom keys and V values.
     */
    fun <K : Any, V : Any> getStore(
        name: String,
        keyConverter: StoreConverter<K>,
        valueConverter: StoreConverter<V>,
        charset: Charset = Charsets.UTF_8
    ): Store<K, V>
}