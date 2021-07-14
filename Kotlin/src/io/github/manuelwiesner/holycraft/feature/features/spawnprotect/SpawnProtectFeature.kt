package io.github.manuelwiesner.holycraft.feature.features.spawnprotect

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class SpawnProtectFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "SPAWNPROTECT") {

    private lateinit var location: Location
    private lateinit var worldUUID: UUID
    private lateinit var netherUUID: UUID
    private var activePlayer: Player? = null

    init {
        this.children += SpawnListener(this)
        this.children += ActiveCmd(this)
    }

    override fun loadFeature() {
        this.location = Location(Bukkit.getWorld("world"), 778.5, 69.1, 323.5)
        this.worldUUID = UUID.fromString("44f60ea4-36c7-443c-bfd1-70e8dad9226a")
        this.netherUUID = UUID.fromString("1c24f3e3-828c-4b7b-8d53-9b225704f12a")
    }

    fun setActive(player: Player?) {
        this.activePlayer = player
    }

    fun getActive(): Player? {
        return this.activePlayer
    }

    fun getLocation(): Location {
        return this.location
    }

    fun getSpawnChunks(): List<ChunkLocation> {
        val list = arrayListOf<ChunkLocation>()
        for (x in 24..50) {
            for (z in 11..37) {
                list += ChunkLocation(x, z, this.worldUUID)
            }
        }
        return list
    }

    fun isProtected(chunk: ChunkLocation): Boolean {
        return when (chunk.world) {
            this.worldUUID -> chunk.chunkX in 24..50 && chunk.chunkZ in 11..37
            this.netherUUID -> chunk.chunkX in 2..6 && chunk.chunkZ in 1..5
            else -> false
        }
    }
}