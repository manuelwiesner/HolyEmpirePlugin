package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.checkAttacker
import io.github.manuelwiesner.holycraft.feature.msg.PropertyForbiddenBlockInteractMsg
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.entity.minecart.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.vehicle.VehicleDestroyEvent

/**
 * Allows clicking of buttons, doors, tripwires, ovens, tables etc
 *
 * Addable: PlayerBedEnterEvent
 */
class PropertyBlockInteractionListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return

        // check if 'placed entity' is a block entity
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val inventory = event.player.inventory

            if (isPlaceableBlockInteractionMaterial(inventory.itemInMainHand.type) || isPlaceableBlockInteractionMaterial(inventory.itemInOffHand.type)) {
                if (this.feature.isBlockInteractionForbidden(block.chunk, event.player)) {
                    event.isCancelled = true
                    PropertyForbiddenBlockInteractMsg("placing minecart/boat").sendMessage(this.feature, event.player)
                    return
                }
            }
        }

        // check if interacted entity is block entity
        if (!isBlockInteractionMaterial(block.state.type)) return

        if (this.feature.isBlockInteractionForbidden(block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBlockInteractMsg("interacting with block").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        if (!isBlockInteractionEntity(event.rightClicked)) {
            // item frames have to be checked here
            if (event.rightClicked !is ItemFrame) {
                return
            }
        }

        if (this.feature.isBlockInteractionForbidden(event.rightClicked.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBlockInteractMsg("interacting with entity").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onVehicleDestroyEvent(event: VehicleDestroyEvent) {
        if (!isBlockInteractionEntity(event.vehicle)) return

        val chunk = event.vehicle.location.chunk

        if (this.feature.checkAttacker(event.attacker, true,
                { PropertyForbiddenBlockInteractMsg("destroying vehicle") },
                { this.feature.isBlockInteractionForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    private fun isBlockInteractionEntity(entity: Entity): Boolean {
        return when (entity) {
            is RideableMinecart -> true
            is ExplosiveMinecart -> true
            is Boat -> true
            else -> false
        }
    }

    private fun isPlaceableBlockInteractionMaterial(material: Material): Boolean {
        return when (material) {
            Material.OAK_BOAT -> true
            Material.SPRUCE_BOAT -> true
            Material.BIRCH_BOAT -> true
            Material.JUNGLE_BOAT -> true
            Material.ACACIA_BOAT -> true
            Material.DARK_OAK_BOAT -> true
            Material.MINECART -> true
            Material.TNT_MINECART -> true
            else -> false
        }
    }

    private fun isBlockInteractionMaterial(material: Material): Boolean {
        return when (material) {
            Material.NOTE_BLOCK -> true
            Material.LEVER -> true
            Material.STONE_PRESSURE_PLATE -> true
            Material.OAK_PRESSURE_PLATE -> true
            Material.SPRUCE_PRESSURE_PLATE -> true
            Material.BIRCH_PRESSURE_PLATE -> true
            Material.JUNGLE_PRESSURE_PLATE -> true
            Material.ACACIA_PRESSURE_PLATE -> true
            Material.DARK_OAK_PRESSURE_PLATE -> true
            Material.CRIMSON_PRESSURE_PLATE -> true
            Material.WARPED_PRESSURE_PLATE -> true
            Material.POLISHED_BLACKSTONE_PRESSURE_PLATE -> true
            Material.REDSTONE_ORE -> true
            Material.OAK_TRAPDOOR -> true
            Material.SPRUCE_TRAPDOOR -> true
            Material.BIRCH_TRAPDOOR -> true
            Material.JUNGLE_TRAPDOOR -> true
            Material.ACACIA_TRAPDOOR -> true
            Material.DARK_OAK_TRAPDOOR -> true
            Material.CRIMSON_TRAPDOOR -> true
            Material.WARPED_TRAPDOOR -> true
            Material.OAK_FENCE_GATE -> true
            Material.SPRUCE_FENCE_GATE -> true
            Material.BIRCH_FENCE_GATE -> true
            Material.JUNGLE_FENCE_GATE -> true
            Material.ACACIA_FENCE_GATE -> true
            Material.DARK_OAK_FENCE_GATE -> true
            Material.CRIMSON_FENCE_GATE -> true
            Material.WARPED_FENCE_GATE -> true
            Material.ENCHANTING_TABLE -> true
            Material.TRIPWIRE_HOOK -> true
            Material.STONE_BUTTON -> true
            Material.OAK_BUTTON -> true
            Material.SPRUCE_BUTTON -> true
            Material.BIRCH_BUTTON -> true
            Material.JUNGLE_BUTTON -> true
            Material.ACACIA_BUTTON -> true
            Material.DARK_OAK_BUTTON -> true
            Material.CRIMSON_BUTTON -> true
            Material.WARPED_BUTTON -> true
            Material.POLISHED_BLACKSTONE_BUTTON -> true
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE -> true
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE -> true
            Material.OAK_DOOR -> true
            Material.SPRUCE_DOOR -> true
            Material.BIRCH_DOOR -> true
            Material.JUNGLE_DOOR -> true
            Material.ACACIA_DOOR -> true
            Material.DARK_OAK_DOOR -> true
            Material.CRIMSON_DOOR -> true
            Material.WARPED_DOOR -> true
            Material.JIGSAW -> true
            Material.CAKE -> true
            Material.WHITE_BED -> true
            Material.ORANGE_BED -> true
            Material.MAGENTA_BED -> true
            Material.LIGHT_BLUE_BED -> true
            Material.YELLOW_BED -> true
            Material.LIME_BED -> true
            Material.PINK_BED -> true
            Material.GRAY_BED -> true
            Material.LIGHT_GRAY_BED -> true
            Material.CYAN_BED -> true
            Material.PURPLE_BED -> true
            Material.BLUE_BED -> true
            Material.BROWN_BED -> true
            Material.GREEN_BED -> true
            Material.RED_BED -> true
            Material.BLACK_BED -> true
            Material.CAULDRON -> true
            Material.LECTERN -> true
            Material.BELL -> true
            Material.TRIPWIRE -> true
            Material.OAK_BOAT -> true
            Material.SPRUCE_BOAT -> true
            Material.BIRCH_BOAT -> true
            Material.JUNGLE_BOAT -> true
            Material.ACACIA_BOAT -> true
            Material.DARK_OAK_BOAT -> true
            Material.MINECART -> true
            Material.TNT_MINECART -> true

//            Material.CRAFTING_TABLE -> true
//            Material.ENDER_CHEST -> true
//            Material.CARTOGRAPHY_TABLE -> true
//            Material.FLETCHING_TABLE -> true
//            Material.GRINDSTONE -> true
//            Material.SMITHING_TABLE -> true
//            Material.STONECUTTER -> true
            else -> false
        }
    }
}