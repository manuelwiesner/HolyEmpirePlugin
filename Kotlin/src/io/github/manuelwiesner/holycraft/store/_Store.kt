package io.github.manuelwiesner.holycraft.store

import com.google.gson.internal.Streams
import com.google.gson.internal.bind.JsonTreeWriter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import io.github.manuelwiesner.holycraft.load.LoadableBase
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of Store
 * @see Store
 */
class _Store<K : Any, V : Any>(
    manager: _StoreManager, name: String,
    private val keyConverter: StoreConverter<K>,
    private val valueConverter: StoreConverter<V>,
    private val charset: Charset
) : LoadableBase<File, _StoreManager>(manager, name, { manager.getItem().resolve("store-$name.json") }), Store<K, V> {
    /**
     * The cached keys/values.
     */
    private val storeCache: ConcurrentHashMap<K, V> = ConcurrentHashMap()

    /**
     * Loads the data from the disk and converts the JSON.
     */
    override fun onLoad() {
        this.storeCache.clear()
        loadFromDisk(getItem())
    }

    /**
     * Saves the converted data to the disk.
     */
    override fun onUnload() {
        saveToDisk()
        this.storeCache.clear()
    }

    /**
     * Saves to the disk
     */
    override fun saveToDisk() {
        runCatching { getItem() }.onSuccess { saveToDisk(it) }
    }

    /**
     * Gets the cached value of key.
     */
    override fun get(key: K): V? {
        postLoad()
        return this.storeCache[key]
    }

    /**
     * Sets the cached value of key.
     */
    override fun set(key: K, value: V) {
        postLoad()
        this.storeCache[key] = value
    }

    /**
     * Checks if a value is cached for key.
     */
    override fun contains(key: K): Boolean {
        postLoad()
        return this.storeCache.containsKey(key)
    }

    /**
     * Removes the cached value of key.
     */
    override fun remove(key: K): V? {
        postLoad()
        return this.storeCache.remove(key)
    }

    /**
     * Computes the function compute if key is missing.
     */
    override fun computeIfAbsent(key: K, compute: (K) -> V): V {
        postLoad()
        return this.storeCache.computeIfAbsent(key, compute)
    }

    /**
     * Executes the action for each element in this store.
     */
    override fun forEach(action: (K, V) -> Unit) {
        postLoad()
        this.storeCache.forEach(action)
    }

    /**
     * Clears all keys/values of this store.
     */
    override fun clear() {
        postLoad()
        this.storeCache.clear()
    }

    /**
     * The raw cache-map of this store.
     */
    override fun raw(): ConcurrentHashMap<K, V> {
        postLoad()
        return this.storeCache
    }

    /**
     * Loads file and converts it.
     */
    private fun loadFromDisk(file: File) {
        runCatching {
            if (!file.exists()) return
            JsonReader(file.bufferedReader(this.charset)).use { json ->
                json.beginObject()
                while (json.hasNext()) readEntry(json, json.nextName())
                json.endObject()
            }
        }.onSuccess {
            getLogger().info("Read from file: $file")
        }.onFailure {
            this.storeCache.clear()
            throw IOException("Failed to load from: $file", it)
        }
    }

    /**
     * Reads/converts a single entry of the JSON-File.
     */
    private fun readEntry(json: JsonReader, name: String) {
        runCatching {
            this.storeCache[this.keyConverter.fromString(name)] = this.valueConverter.fromJson(json)
        }.onFailure {
            getLogger().warn("Failed to convert entry $name to its type!", it)
            while (json.peek() != JsonToken.NAME && json.peek() != JsonToken.END_DOCUMENT) json.skipValue()
        }
    }

    /**
     * Converts the data and saves it to the file.
     */
    private fun saveToDisk(file: File) {
        runCatching {
            file.createNewFile()
            JsonWriter(file.bufferedWriter(this.charset)).use { json ->
                json.setIndent("  ")
                json.beginObject()
                this.storeCache.forEach { (k, v) -> writeEntry(json, k, v) }
                json.endObject()
                Unit
            }
        }.onSuccess {
            getLogger().info("Saved to file: $file")
        }.onFailure {
            getLogger().error("Failed to save store to file: $file", it)
        }
    }

    /**
     * Writes/converts a single entry of the JSON-File.
     */
    private fun writeEntry(json: JsonWriter, key: K, value: V) {
        runCatching {
            val convertedKey = this.keyConverter.toString(key)
            val convertedValue = JsonTreeWriter().use { this.valueConverter.toJson(it, value); it.get() }
            json.name(convertedKey)
            Streams.write(convertedValue, json)
        }.onFailure { getLogger().warn("Failed to convert entry $key - $value to string!", it) }
    }
}