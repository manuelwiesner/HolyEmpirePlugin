@file:Suppress("unused")

package io.github.manuelwiesner.holycraft.store

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.bukkit.Location
import java.io.StringReader
import java.io.StringWriter
import java.time.Instant
import java.util.*

/**
 * Helper class for pure JSON converters, converts the JSON output to a String and reads the String as JSON.
 */
abstract class _StoreConverter<T> : StoreConverter<T> {
    /**
     * Reads value as JSON and performs the conversion of the JSON.
     */
    final override fun fromString(value: String): T {
        return JsonReader(StringReader(value)).use { fromJson(it) }
    }

    /**
     * Converts value to a JSON and returns the String representation of it.
     */
    final override fun toString(value: T): String {
        return StringWriter().also { JsonWriter(it).use { json -> toJson(json, value) } }.toString()
    }
}

// ---------------------------------------------------------------------------------------------------------------------

fun JsonReader.nextName(key: String): JsonReader {
    if (nextName() != key) throw IllegalArgumentException("Key $key missing in JSON!")
    return this
}

fun <T> JsonReader.nextNullOrT(getter: JsonReader.() -> T): T? {
    return if (peek() == JsonToken.NULL) nextNull().let { null } else getter(this)
}

fun JsonReader.nextUUID(): UUID {
    return UUID.fromString(nextString())
}

fun JsonReader.nextInstant(): Instant {
    return Instant.parse(nextString())
}

fun JsonReader.nextLocation(): Location {
    return Location.deserialize(nextMap<Any?>(hashMapOf()) { nextString() })
}

inline fun <reified T : Enum<T>> JsonReader.nextEnum(): T {
    val value = nextString()
    return enumValues<T>().find { it.name.equals(value, true) }
        ?: throw IllegalStateException("Expected enum of type ${T::class.simpleName} but was $value!")
}

fun <T : Any> JsonReader.nextObject(converter: JsonReader.() -> T): T {
    beginObject()
    return converter().also { endObject() }
}

fun <T> JsonReader.nextList(list: MutableList<T>, converter: JsonReader.() -> T): MutableList<T> {
    return beginArray().let {
        list.also { while (hasNext()) it.add(converter()) }
    }.also { endArray() }
}

fun <T> JsonReader.nextMap(map: MutableMap<String, T>, valueConverter: JsonReader.() -> T): MutableMap<String, T> {
    return nextMap(map, { it }, valueConverter)
}

fun <K, V> JsonReader.nextMap(map: MutableMap<K, V>, keyConverter: (String) -> K, valueConverter: JsonReader.() -> V): MutableMap<K, V> {
    return beginObject().let {
        map.also { while (hasNext()) it[keyConverter(nextName())] = valueConverter() }
    }.also { endObject() }
}

// ---------------------------------------------------------------------------------------------------------------------

fun <T> JsonWriter.nullOrTValue(value: T?, action: JsonWriter.(T) -> Unit): JsonWriter {
    return if (value != null) action(value).let { this } else nullValue()
}

fun JsonWriter.uuidValue(uuid: UUID): JsonWriter {
    return value(uuid.toString())
}

fun JsonWriter.instantValue(instant: Instant): JsonWriter {
    return value(instant.toString())
}

fun JsonWriter.locationValue(location: Location): JsonWriter {
    return mapValue(location.serialize()) { value(it.toString()) }
}

fun JsonWriter.enumValue(value: Enum<*>): JsonWriter {
    return value(value.name)
}

fun <T : Any> JsonWriter.objectValue(value: T, action: JsonWriter.(T) -> Unit): JsonWriter {
    return beginObject().also { action(value) }.endObject()
}

fun <T> JsonWriter.listValue(listData: List<T>, action: JsonWriter.(T) -> Unit): JsonWriter {
    return beginArray().apply { listData.forEach { action(it) } }.endArray()
}

fun <T> JsonWriter.mapValue(mapData: Map<String, T>, valueAction: JsonWriter.(T) -> Unit): JsonWriter {
    return mapValue(mapData, { it }, valueAction)
}

fun <K, V> JsonWriter.mapValue(mapData: Map<K, V>, keyConverter: (K) -> String, valueAction: JsonWriter.(V) -> Unit): JsonWriter {
    beginObject().apply { mapData.forEach { (k, v) -> name(keyConverter(k)); valueAction(v) } }.endObject()
    return this
}

// ---------------------------------------------------------------------------------------------------------------------

fun JsonReader.getString(key: String): String = nextName(key).nextString()

fun JsonReader.getBoolean(key: String): Boolean = nextName(key).nextBoolean()

fun JsonReader.getInt(key: String): Int = nextName(key).nextInt()

fun JsonReader.getLong(key: String): Long = nextName(key).nextLong()

fun JsonReader.getDouble(key: String): Double = nextName(key).nextDouble()

fun JsonReader.getUUID(key: String): UUID = nextName(key).nextUUID()

fun JsonReader.getInstant(key: String): Instant = nextName(key).nextInstant()

fun JsonReader.getLocation(key: String): Location = nextName(key).nextLocation()

inline fun <reified T : Enum<T>> JsonReader.getEnum(key: String): T {
    return nextName(key).nextEnum()
}

fun <T : Any> JsonReader.getObject(key: String, converter: JsonReader.() -> T): T {
    return nextName(key).nextObject(converter)
}

fun <T> JsonReader.getList(key: String, list: MutableList<T>, converter: JsonReader.() -> T): MutableList<T> {
    return nextName(key).nextList(list, converter)
}

fun <T> JsonReader.getMap(key: String, map: MutableMap<String, T>, valueConverter: JsonReader.() -> T): MutableMap<String, T> {
    return nextName(key).nextMap(map, valueConverter)
}

fun <K, V> JsonReader.getMap(key: String, map: MutableMap<K, V>, keyConverter: (String) -> K, valueConverter: JsonReader.() -> V): MutableMap<K, V> {
    return nextName(key).nextMap(map, keyConverter, valueConverter)
}

// ---------------------------------------------------------------------------------------------------------------------

fun JsonReader.getNullOrString(key: String): String? = nextName(key).nextNullOrT { nextString() }

fun JsonReader.getNullOrBoolean(key: String): Boolean? = nextName(key).nextNullOrT { nextBoolean() }

fun JsonReader.getNullOrInt(key: String): Int? = nextName(key).nextNullOrT { nextInt() }

fun JsonReader.getNullOrLong(key: String): Long? = nextName(key).nextNullOrT { nextLong() }

fun JsonReader.getNullOrDouble(key: String): Double? = nextName(key).nextNullOrT { nextDouble() }

fun JsonReader.getNullOrUUID(key: String): UUID? = nextName(key).nextNullOrT { nextUUID() }

fun JsonReader.getNullOrInstant(key: String): Instant? = nextName(key).nextNullOrT { nextInstant() }

fun JsonReader.getNullOrLocation(key: String): Location? = nextName(key).nextNullOrT { nextLocation() }

inline fun <reified T : Enum<T>> JsonReader.getNullOrEnum(key: String): T? {
    return nextName(key).nextNullOrT { nextEnum<T>() }
}

fun <T : Any> JsonReader.getNullOrObject(key: String, converter: JsonReader.() -> T): T? {
    return nextName(key).nextNullOrT { nextObject(converter) }
}

fun <T> JsonReader.getNullOrList(key: String, list: MutableList<T>, converter: JsonReader.() -> T): MutableList<T>? {
    return nextName(key).nextNullOrT { nextList(list, converter) }
}

fun <T> JsonReader.getNullOrMap(key: String, map: MutableMap<String, T>, valueConverter: JsonReader.() -> T): MutableMap<String, T>? {
    return nextName(key).nextNullOrT { nextMap(map, valueConverter) }
}

fun <K, V> JsonReader.getNullOrMap(key: String, map: MutableMap<K, V>, keyConv: (String) -> K, valueConv: JsonReader.() -> V): MutableMap<K, V>? {
    return nextName(key).nextNullOrT { nextMap(map, keyConv, valueConv) }
}

// ---------------------------------------------------------------------------------------------------------------------

fun JsonWriter.setString(key: String, value: String): JsonWriter = name(key).value(value)

fun JsonWriter.setBoolean(key: String, value: Boolean): JsonWriter = name(key).value(value)

fun JsonWriter.setInt(key: String, value: Int): JsonWriter = name(key).value(value)

fun JsonWriter.setLong(key: String, value: Long): JsonWriter = name(key).value(value)

fun JsonWriter.setDouble(key: String, value: Double): JsonWriter = name(key).value(value)

fun JsonWriter.setUUID(key: String, value: UUID): JsonWriter = name(key).uuidValue(value)

fun JsonWriter.setInstant(key: String, value: Instant): JsonWriter = name(key).instantValue(value)

fun JsonWriter.setLocation(key: String, value: Location): JsonWriter = name(key).locationValue(value)

fun JsonWriter.setEnum(key: String, value: Enum<*>): JsonWriter = name(key).enumValue(value)

fun <T : Any> JsonWriter.setObject(key: String, value: T, action: JsonWriter.(T) -> Unit): JsonWriter {
    return name(key).objectValue(value, action)
}

fun <T> JsonWriter.setList(key: String, value: List<T>, action: JsonWriter.(T) -> Unit): JsonWriter {
    return name(key).listValue(value, action)
}

fun <T> JsonWriter.setMap(key: String, value: Map<String, T>, valueAction: JsonWriter.(T) -> Unit): JsonWriter {
    return name(key).mapValue(value, valueAction)
}

fun <K, V> JsonWriter.setMap(key: String, value: Map<K, V>, keyConverter: (K) -> String, valueAction: JsonWriter.(V) -> Unit): JsonWriter {
    return name(key).mapValue(value, keyConverter, valueAction)
}

// ---------------------------------------------------------------------------------------------------------------------

fun JsonWriter.setNullOrString(key: String, value: String?): JsonWriter = name(key).value(value)

fun JsonWriter.setNullOrBoolean(key: String, value: Boolean?): JsonWriter = name(key).value(value)

fun JsonWriter.setNullOrInt(key: String, value: Int?): JsonWriter = name(key).value(value)

fun JsonWriter.setNullOrLong(key: String, value: Long?): JsonWriter = name(key).value(value)

fun JsonWriter.setNullOrDouble(key: String, value: Double?): JsonWriter = name(key).value(value)

fun JsonWriter.setNullOrUUID(key: String, value: UUID?): JsonWriter = name(key).nullOrTValue(value) { uuidValue(it) }

fun JsonWriter.setNullOrInstant(key: String, value: Instant?): JsonWriter = name(key).nullOrTValue(value) { instantValue(it) }

fun JsonWriter.setNullOrLocation(key: String, value: Location?): JsonWriter = name(key).nullOrTValue(value) { locationValue(it) }

fun JsonWriter.setNullOrEnum(key: String, value: Enum<*>?): JsonWriter = name(key).nullOrTValue(value) { enumValue(it) }

fun <T : Any> JsonWriter.setNullOrObject(key: String, value: T?, action: JsonWriter.(T) -> Unit): JsonWriter {
    return name(key).nullOrTValue(value) { objectValue(it, action) }
}

fun <T> JsonWriter.setNullOrList(key: String, value: List<T>?, action: JsonWriter.(T) -> Unit): JsonWriter {
    return name(key).nullOrTValue(value) { listValue(it, action) }
}

fun <T> JsonWriter.setNullOrMap(key: String, value: Map<String, T>?, valueAction: JsonWriter.(T) -> Unit): JsonWriter {
    return name(key).nullOrTValue(value) { mapValue(it, valueAction) }
}

fun <K, V> JsonWriter.setNullOrMap(key: String, value: Map<K, V>?, keyConverter: (K) -> String, valueAction: JsonWriter.(V) -> Unit): JsonWriter {
    return name(key).nullOrTValue(value) { mapValue(it, keyConverter, valueAction) }
}
