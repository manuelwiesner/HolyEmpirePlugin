package io.github.manuelwiesner.holycraft.feature.features.property.impl

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.manuelwiesner.holycraft.store.*
import org.bukkit.Chunk
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

data class ChunkLocation(val chunkX: Int, val chunkZ: Int, val world: UUID)

// ---------------------------------------------------------------------------------------------------------------------

fun Chunk.location(): ChunkLocation {
    return ChunkLocation(this.x, this.z, this.world.uid)
}

// ---------------------------------------------------------------------------------------------------------------------

object ChunkLocationConverter : StoreConverter<ChunkLocation> {

    override fun fromString(value: String): ChunkLocation {
        val elements = value.split(":")
        if (elements.size != 3) throw IllegalStateException("Invalid ChunkLocation!")
        return ChunkLocation(elements[0].toInt(), elements[1].toInt(), UUID.fromString(elements[2]))
    }

    override fun toString(value: ChunkLocation): String {
        return "${value.chunkX}:${value.chunkZ}:${value.world}"
    }

    override fun fromJson(json: JsonReader): ChunkLocation {
        return fromString(json.nextString())
    }

    override fun toJson(json: JsonWriter, value: ChunkLocation) {
        json.value(toString(value))
    }

}

// ---------------------------------------------------------------------------------------------------------------------

object PropertyConverter : _StoreConverter<Property>() {

    override fun fromJson(json: JsonReader): Property {
        return json.nextObject {
            val owner = getUUID("owner")
            val pricePaid = getInt("price-paid")
            val location = nextName("chunk-location").let { ChunkLocationConverter.fromJson(it) }
            val builders = getList("builders", arrayListOf()) { nextUUID() }
            val blockInteractors = getList("block-interactors", arrayListOf()) { nextUUID() }
            val entityInteractors = getList("entity-interactors", arrayListOf()) { nextUUID() }
            val chestInteractors = getList("chest-interactors", arrayListOf()) { nextUUID() }
            val allowBuilding = getBoolean("allow-building")
            val allowBlockInteracting = getBoolean("allow-block-interacting")
            val allowEntityInteracting = getBoolean("allow-entity-interacting")
            val allowChestInteracting = getBoolean("allow-chest-interacting")
            val allowExplosions = getBoolean("allow-explosions")
            val allowCausality = getBoolean("allow-causality")
            val allowPvP = getBoolean("allow-pvp")

            Property(
                owner, pricePaid, location, AtomicBoolean(false), builders, blockInteractors, entityInteractors, chestInteractors,
                allowBuilding, allowBlockInteracting, allowEntityInteracting, allowChestInteracting, allowExplosions, allowCausality, allowPvP
            )
        }
    }

    override fun toJson(json: JsonWriter, value: Property) {
        json.objectValue(value) { property ->
            setUUID("owner", property.getOwner())
            setInt("price-paid", property.getPricePaid())
            name("chunk-location").also { ChunkLocationConverter.toJson(it, property.getChunkLocation()) }
            setList("builders", property.getBuilders()) { uuidValue(it) }
            setList("block-interactors", property.getBlockInteractors()) { uuidValue(it) }
            setList("entity-interactors", property.getEntityInteractors()) { uuidValue(it) }
            setList("chest-interactors", property.getChestInteractors()) { uuidValue(it) }
            setBoolean("allow-building", property.isBuildingAllowed())
            setBoolean("allow-block-interacting", property.isBlockInteractAllowed())
            setBoolean("allow-entity-interacting", property.isEntityInteractAllowed())
            setBoolean("allow-chest-interacting", property.isChestInteractAllowed())
            setBoolean("allow-explosions", property.isExplosionAllowed())
            setBoolean("allow-causality", property.isCausalityAllowed())
            setBoolean("allow-pvp", property.isPvPAllowed())
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
