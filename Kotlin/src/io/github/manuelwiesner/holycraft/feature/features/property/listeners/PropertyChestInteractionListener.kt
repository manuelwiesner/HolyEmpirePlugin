package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.checkAttacker
import io.github.manuelwiesner.holycraft.feature.features.property.impl.location
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.entity.minecart.HopperMinecart
import org.bukkit.entity.minecart.PoweredMinecart
import org.bukkit.entity.minecart.StorageMinecart
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerArmorStandManipulateEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTakeLecternBookEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent

/**
 * Listens for changes to / around chests and blocks them if there are not permitted
 * 1. Open chests
 * 2. Place chests in general (also double chests)
 * 3. Place hoppers around it
 * 4. Move with piston
 */
class PropertyChestInteractionListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    companion object {
        private val CHEST_FACES = arrayOf(BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST)
    }

    // check for double chest on property border
    @EventHandler(ignoreCancelled = true)
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        if (event.blockPlaced.type != Material.CHEST) return

        val ownerId = event.player.uniqueId
        val placedChunk = event.blockPlaced.chunk
        val placedProperty = this.feature.getProperty(placedChunk.location())

        for (face in CHEST_FACES) {
            val relative = event.blockPlaced.getRelative(face)
            if (relative.type != Material.CHEST) continue

            val relativeChunk = relative.chunk
            if (placedChunk == relativeChunk) continue

            val relativeProperty = this.feature.getProperty(relativeChunk.location())

            if (relativeProperty == null) {
                PropertyInfoDoubleChestMsg().sendMessage(this.feature, event.player)
                continue
            }

            if (!relativeProperty.isBuildingAllowed() && (placedProperty == null || relativeProperty.getOwner() != placedProperty.getOwner())) {
                if (relativeProperty.canBuild(ownerId)) {
                    PropertyInfoDoubleChestMsg().sendMessage(this.feature, event.player)
                } else {
                    event.isCancelled = true
                    PropertyForbiddenChestInteractMsg("creating illegal double-chest").sendMessage(this.feature, event.player)
                    return
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return

        // check if 'placed entity' is chest entity
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val inventory = event.player.inventory

            if (isPlaceableChestInteractionMaterial(inventory.itemInMainHand.type) || isPlaceableChestInteractionMaterial(inventory.itemInOffHand.type)) {
                if (this.feature.isChestInteractionForbidden(block.chunk, event.player)) {
                    event.isCancelled = true
                    PropertyForbiddenChestInteractMsg("placing minecart").sendMessage(this.feature, event.player)
                    return
                }
            }
        }

        // check if clicked entity is chest entity
        if (!isChestInteractionMaterial(block.state.type)) return

        if (this.feature.isChestInteractionForbidden(block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenChestInteractMsg("interacting with block").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerTakeLecternBookEvent(event: PlayerTakeLecternBookEvent) {
        if (this.feature.isChestInteractionForbidden(event.lectern.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenChestInteractMsg("taking lectern book").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        if (!isChestInteractionEntity(event.rightClicked)) return

        // rotation is checked in block listener
        if (event.rightClicked is ItemFrame) return

        if (this.feature.isChestInteractionForbidden(event.rightClicked.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenChestInteractMsg("interacting with entity").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerArmorStandManipulateEvent(event: PlayerArmorStandManipulateEvent) {
        if (this.feature.isChestInteractionForbidden(event.rightClicked.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenChestInteractMsg("manipulating armor-stand").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (!isChestInteractionEntity(event.entity)) return

        val chunk = event.entity.location.chunk
        if (this.feature.checkAttacker(event.damager, false,
                { PropertyForbiddenChestInteractMsg("destroying chest-entity") },
                { this.feature.isChestInteractionForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleDestroyEvent(event: VehicleDestroyEvent) {
        if (!isChestInteractionEntity(event.vehicle)) return

        val chunk = event.vehicle.location.chunk
        if (this.feature.checkAttacker(event.attacker, true,
                { PropertyForbiddenChestInteractMsg("destroying chest-vehicle") },
                { this.feature.isChestInteractionForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    private fun isChestInteractionEntity(entity: Entity): Boolean {
        return when (entity) {
            is StorageMinecart -> true
            is PoweredMinecart -> true
            is HopperMinecart -> true
            is ArmorStand -> true
            is ItemFrame -> true
            else -> false
        }
    }

    private fun isPlaceableChestInteractionMaterial(material: Material): Boolean {
        return when (material) {
            Material.CHEST_MINECART -> true
            Material.FURNACE_MINECART -> true
            Material.HOPPER_MINECART -> true
            else -> false
        }
    }

    private fun isChestInteractionMaterial(material: Material): Boolean {
        return when (material) {
            Material.DISPENSER -> true
            Material.CHEST -> true
            Material.FURNACE -> true
            Material.JUKEBOX -> true
            Material.BEACON -> true
            Material.ANVIL -> true
            Material.CHIPPED_ANVIL -> true
            Material.DAMAGED_ANVIL -> true
            Material.TRAPPED_CHEST -> true
            Material.HOPPER -> true
            Material.DROPPER -> true
            Material.BARREL -> true
            Material.SMOKER -> true
            Material.BLAST_FURNACE -> true
            Material.CONDUIT -> true
            Material.BREWING_STAND -> true

            Material.SHULKER_BOX -> true
            Material.WHITE_SHULKER_BOX -> true
            Material.ORANGE_SHULKER_BOX -> true
            Material.MAGENTA_SHULKER_BOX -> true
            Material.LIGHT_BLUE_SHULKER_BOX -> true
            Material.YELLOW_SHULKER_BOX -> true
            Material.LIME_SHULKER_BOX -> true
            Material.PINK_SHULKER_BOX -> true
            Material.GRAY_SHULKER_BOX -> true
            Material.LIGHT_GRAY_SHULKER_BOX -> true
            Material.CYAN_SHULKER_BOX -> true
            Material.PURPLE_SHULKER_BOX -> true
            Material.BLUE_SHULKER_BOX -> true
            Material.BROWN_SHULKER_BOX -> true
            Material.GREEN_SHULKER_BOX -> true
            Material.RED_SHULKER_BOX -> true
            Material.BLACK_SHULKER_BOX -> true
            else -> false
        }
    }
}