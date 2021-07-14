package io.github.manuelwiesner.holycraft.store

import com.google.common.util.concurrent.AtomicDouble
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.bukkit.Location
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Converts objects to/from String and JSON.
 */
interface StoreConverter<T> {
    /**
     * String -> T
     */
    fun fromString(value: String): T

    /**
     * T -> String
     */
    fun toString(value: T): String

    /**
     * JSON -> T
     */
    fun fromJson(json: JsonReader): T

    /**
     * T -> JSON
     */
    fun toJson(json: JsonWriter, value: T)

    companion object {

        /**
         * StoreConverter for Strings.
         */
        val TEXT: StoreConverter<String> = object : StoreConverter<String> {
            override fun fromString(value: String) = value

            override fun toString(value: String) = value

            override fun fromJson(json: JsonReader) = json.nextString()

            override fun toJson(json: JsonWriter, value: String) {
                json.value(value)
            }
        }

        /**
         * StoreConverter for Ints.
         */
        val NUMBER: StoreConverter<Int> = object : StoreConverter<Int> {
            override fun fromString(value: String) = value.toInt()

            override fun toString(value: Int) = value.toString()

            override fun fromJson(json: JsonReader) = json.nextInt()

            override fun toJson(json: JsonWriter, value: Int) {
                json.value(value)
            }
        }

        /**
         * StoreConverter for AtomicIntegers.
         */
        val ATOMIC_NUMBER: StoreConverter<AtomicInteger> = object : StoreConverter<AtomicInteger> {
            override fun fromString(value: String) = AtomicInteger(value.toInt())

            override fun toString(value: AtomicInteger) = value.toString()

            override fun fromJson(json: JsonReader) = AtomicInteger(json.nextInt())

            override fun toJson(json: JsonWriter, value: AtomicInteger) {
                json.value(value.get())
            }
        }

        /**
         * StoreConverter for Doubles.
         */
        val DECIMAL: StoreConverter<Double> = object : StoreConverter<Double> {
            override fun fromString(value: String) = value.toDouble()

            override fun toString(value: Double) = value.toString()

            override fun fromJson(json: JsonReader) = json.nextDouble()

            override fun toJson(json: JsonWriter, value: Double) {
                json.value(value)
            }
        }

        /**
         * StoreConverter for AtomicDoubles.
         */
        val ATOMIC_DECIMAL: StoreConverter<AtomicDouble> = object : StoreConverter<AtomicDouble> {
            override fun fromString(value: String) = AtomicDouble(value.toDouble())

            override fun toString(value: AtomicDouble) = value.toString()

            override fun fromJson(json: JsonReader) = AtomicDouble(json.nextDouble())

            override fun toJson(json: JsonWriter, value: AtomicDouble) {
                json.value(value.get())
            }
        }

        /**
         * StoreConverter for Booleans.
         */
        val FLAG: StoreConverter<Boolean> = object : StoreConverter<Boolean> {
            override fun fromString(value: String): Boolean {
                return when {
                    value.equals("false", true) -> false
                    value.equals("true", true) -> true
                    else -> throw IllegalArgumentException("Value must be true or false")
                }
            }

            override fun toString(value: Boolean) = value.toString()

            override fun fromJson(json: JsonReader) = json.nextBoolean()

            override fun toJson(json: JsonWriter, value: Boolean) {
                json.value(value)
            }
        }

        /**
         * StoreConverter for AtomicBooleans.
         */
        val ATOMIC_FLAG: StoreConverter<AtomicBoolean> = object : StoreConverter<AtomicBoolean> {
            override fun fromString(value: String): AtomicBoolean {
                return when {
                    value.equals("false", true) -> AtomicBoolean(false)
                    value.equals("true", true) -> AtomicBoolean(true)
                    else -> throw IllegalArgumentException("Value must be true or false")
                }
            }

            override fun toString(value: AtomicBoolean) = value.toString()

            override fun fromJson(json: JsonReader) = AtomicBoolean(json.nextBoolean())

            override fun toJson(json: JsonWriter, value: AtomicBoolean) {
                json.value(value.get())
            }
        }

        /**
         * StoreConverter for UUIDs.
         */
        val UUID: StoreConverter<UUID> = object : StoreConverter<UUID> {
            override fun fromString(value: String) = java.util.UUID.fromString(value)

            override fun toString(value: UUID) = value.toString()

            override fun fromJson(json: JsonReader) = json.nextUUID()

            override fun toJson(json: JsonWriter, value: UUID) {
                json.uuidValue(value)
            }
        }

        /**
         * StoreConverter for UUIDs.
         */
        val INSTANT: StoreConverter<Instant> = object : StoreConverter<Instant> {
            override fun fromString(value: String) = Instant.parse(value)

            override fun toString(value: Instant) = value.toString()

            override fun fromJson(json: JsonReader) = json.nextInstant()

            override fun toJson(json: JsonWriter, value: Instant) {
                json.instantValue(value)
            }
        }

        /**
         * StoreConverter for Locations.
         */
        val LOCATION: StoreConverter<Location> = object : _StoreConverter<Location>() {
            override fun fromJson(json: JsonReader): Location {
                return json.nextLocation()
            }

            override fun toJson(json: JsonWriter, value: Location) {
                json.locationValue(value)
            }
        }

        /**
         * Makes the provided converter nullable.
         */
        fun <T> NULL(converter: StoreConverter<T>): StoreConverter<T?> {
            return object : StoreConverter<T?> {
                override fun fromString(value: String) = if (value == "NULL") null else converter.fromString(value)

                override fun toString(value: T?) = value?.let { converter.toString(it) } ?: "NULL"

                override fun fromJson(json: JsonReader): T? = json.nextNullOrT { converter.fromJson(this) }

                override fun toJson(json: JsonWriter, value: T?) {
                    json.nullOrTValue(value) { converter.toJson(this, it) }
                }
            }
        }

        /**
         * Makes the provided converter to a list of T.
         */
        fun <T> LIST(converter: StoreConverter<T>): StoreConverter<MutableList<T>> {
            return object : _StoreConverter<MutableList<T>>() {
                override fun fromJson(json: JsonReader): MutableList<T> {
                    return json.nextList(arrayListOf()) { converter.fromJson(this) }
                }

                override fun toJson(json: JsonWriter, value: MutableList<T>) {
                    json.listValue(value) { converter.toJson(this, it) }
                }
            }
        }

        /**
         * Makes the provided converter to a synchronized list of T.
         */
        fun <T> SYNC_LIST(converter: StoreConverter<T>): StoreConverter<MutableList<T>> {
            return object : _StoreConverter<MutableList<T>>() {
                override fun fromJson(json: JsonReader): MutableList<T> {
                    return json.nextList(Collections.synchronizedList(arrayListOf())) { converter.fromJson(this) }
                }

                override fun toJson(json: JsonWriter, value: MutableList<T>) {
                    json.listValue(value) { converter.toJson(this, it) }
                }
            }
        }

        /**
         * Makes the provided converter to a map of K and V.
         */
        fun <K, V> MAP(keyConverter: StoreConverter<K>, valueConverter: StoreConverter<V>): StoreConverter<MutableMap<K, V>> {
            return object : _StoreConverter<MutableMap<K, V>>() {
                override fun fromJson(json: JsonReader): MutableMap<K, V> {
                    return json.nextMap(hashMapOf(), { keyConverter.fromString(it) }) { valueConverter.fromJson(this) }
                }

                override fun toJson(json: JsonWriter, value: MutableMap<K, V>) {
                    json.mapValue(value, { keyConverter.toString(it) }) { valueConverter.toJson(this, it) }
                }
            }
        }

        /**
         * Makes the provided converter to a synchronized map of K and V.
         */
        fun <K, V> SYNC_MAP(keyConverter: StoreConverter<K>, valueConverter: StoreConverter<V>): StoreConverter<MutableMap<K, V>> {
            return object : _StoreConverter<MutableMap<K, V>>() {
                override fun fromJson(json: JsonReader): MutableMap<K, V> {
                    return json.nextMap(ConcurrentHashMap(), { keyConverter.fromString(it) }) { valueConverter.fromJson(this) }
                }

                override fun toJson(json: JsonWriter, value: MutableMap<K, V>) {
                    json.mapValue(value, { keyConverter.toString(it) }) { valueConverter.toJson(this, it) }
                }
            }
        }
    }
}

