package io.github.manuelwiesner.holycraft.feature.features.warp

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.player.View
import io.github.manuelwiesner.holycraft.store.StoreConverter
import io.github.manuelwiesner.holycraft.yaml.SafeYaml
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*

// TODO: Change this class to use a Warp datatype which stores more information in a dedicated Store object.
class WarpFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "WARP") {

    /**
     * IF the players are only allowed to use this in the overworld for example.
     */
    private val allowedWorlds: SafeYaml<MutableList<String>> = getHolyCraft().getYamlManager()
        .getStringListWrapper("feature.warp.allowedWorlds").makeSafe(arrayListOf())

    /**
     * The spawn location of this server
     */
    private val warpLocations: SafeYaml<MutableList<String>> = getHolyCraft().getYamlManager()
        .getStringListWrapper("feature.warp.warps").makeSafe(arrayListOf())
    private val warpLocationMap: MutableMap<String, Location> = hashMapOf()

    /**
     * A view to all player homes. Player UUID -> Home Name -> Home Location
     */
    private val homeLocations: View<MutableMap<String, Location>> = getHolyCraft().getPlayerManager()
        .getView("warp.home", StoreConverter.MAP(StoreConverter.TEXT, StoreConverter.LOCATION))

    init {
        this.children += SpawnCmd(this)
        this.children += HomeCmd(this)
        this.children += WarpCmd(this)
        this.children += NamedHomeCmd(this)
    }

    override fun loadFeature() {
        this.warpLocations.get().stream().map { it.split("#") }
            .filter { it.size == 2 }.map { it[0] to StoreConverter.LOCATION.fromString(it[1]) }
            .forEach { this.warpLocationMap[it.first] = it.second }
    }

    override fun unloadFeature() {
        val list = arrayListOf<String>()
        this.warpLocationMap.forEach { (k, v) -> list.add("$k#${StoreConverter.LOCATION.toString(v)}") }
        this.warpLocations.set(list)
    }

    override fun saveToDisk() {
        unloadFeature()
    }

    fun getAllowedWorlds(): List<String> = this.allowedWorlds.get()

    fun getWarps(): Iterable<String> = this.warpLocationMap.keys

    fun getWarp(name: String): Location? = this.warpLocationMap[name]

    fun setWarp(name: String, location: Location): Unit = this.warpLocationMap.set(name, location)

    fun removeWarp(name: String): Location? = this.warpLocationMap.remove(name)

    fun getPlayerHomes(uuid: UUID): Iterable<String>? {
        return this.homeLocations[uuid]?.keys
    }

    fun getPlayerHome(uuid: UUID, name: String): Location? {
        return this.homeLocations[uuid]?.get(name)
    }

    fun setPlayerHome(uuid: UUID, name: String, location: Location) {
        this.homeLocations.computeIfAbsent(uuid) { hashMapOf() }[name] = location
    }

    fun removePlayerHome(uuid: UUID, name: String): Location? {
        return this.homeLocations[uuid]?.remove(name)
    }

    fun teleportPlayer(player: Player, loc: Location) {
        val curr = Location(loc.world, loc.x, loc.y, loc.z, loc.yaw, loc.pitch)
        val next = Location(loc.world, loc.x, loc.y + 1, loc.z, loc.yaw, loc.pitch)
        while (curr.y < 255 && !curr.block.isEmpty && !next.block.isEmpty) {
            curr.y = curr.y + 1
            next.y = next.y + 1
        }

        player.teleport(curr, PlayerTeleportEvent.TeleportCause.COMMAND)
    }
}