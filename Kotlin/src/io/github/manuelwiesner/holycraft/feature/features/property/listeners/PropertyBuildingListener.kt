package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.checkAttacker
import io.github.manuelwiesner.holycraft.feature.features.property.impl.location
import io.github.manuelwiesner.holycraft.feature.msg.PropertyForbiddenBuildMsg
import org.bukkit.Material
import org.bukkit.block.data.type.Dispenser
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.player.*

/**
 * Listens for building / destroying of illegal blocks:
 * 1. Place Block
 * 2. Break Block
 * 3. Crops (trampling, harvesting etc.)
 * 4. Dispenser placing blocks
 *
 * Addable: HangingBreakByEntityEvent, EntityDamageByEntityEvent, SignChangeEvent, PlayerBucketEmptyEvent,
 * PlayerBucketFillEvent, PlayerHarvestBlockEvent
 */
class PropertyBuildingListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    @EventHandler(ignoreCancelled = true)
    fun onHangingBreakByEntityEvent(event: HangingBreakByEntityEvent) {
        val chunk = event.entity.location.chunk
        if (this.feature.checkAttacker(event.remover, true,
                { PropertyForbiddenBuildMsg("destroying hanging") },
                { this.feature.isBuildingForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.entity !is ArmorStand) return

        val chunk = event.entity.location.chunk
        if (this.feature.checkAttacker(event.damager, false,
                { PropertyForbiddenBuildMsg("destroying armor-stand") },
                { this.feature.isBuildingForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockDispenseEvent(event: BlockDispenseEvent) {
        val dispenser = event.block.blockData as? Dispenser ?: return

        val dispenserChunk = event.block.chunk
        val placedChunk = event.block.getRelative(dispenser.facing).chunk

        if (dispenserChunk == placedChunk) return

        val placedProperty = this.feature.getProperty(placedChunk.location()) ?: return
        val dispenserProperty = this.feature.getProperty(dispenserChunk.location())

        if (!placedProperty.isBuildingAllowed() && placedProperty.getOwner() != dispenserProperty?.getOwner()) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return

        // check if 'placeable block' is right clicked
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val inventory = event.player.inventory

            if (isPlaceableBuildMaterial(inventory.itemInMainHand.type) || isPlaceableBuildMaterial(inventory.itemInOffHand.type)) {
                if (this.feature.isBuildingForbidden(block.chunk, event.player)) {
                    event.isCancelled = true
                    PropertyForbiddenBuildMsg("placing armor-stand/item-frame").sendMessage(this.feature, event.player)
                }
            }

            return
        }

        // check for farmland destruction
        if (event.action != Action.PHYSICAL) return
        if (block.type != Material.FARMLAND) return

        if (this.feature.isBuildingForbidden(block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBuildMsg("destroying farmland").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        if (this.feature.isBuildingForbidden(event.block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBuildMsg("destroying block").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPlaceEvent(event: BlockPlaceEvent) {
        if (this.feature.isBuildingForbidden(event.block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBuildMsg("placing block").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerBucketEmptyEvent(event: PlayerBucketEmptyEvent) {
        if (this.feature.isBuildingForbidden(event.block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBuildMsg("emptying bucket").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerBucketFillEvent(event: PlayerBucketFillEvent) {
        if (this.feature.isBuildingForbidden(event.block.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBuildMsg("filling bucket").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerHarvestBlockEvent(event: PlayerHarvestBlockEvent) {
        if (this.feature.isBuildingForbidden(event.harvestedBlock.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenBuildMsg("harvesting crop").sendMessage(this.feature, event.player)
        }
    }

    private fun isPlaceableBuildMaterial(material: Material): Boolean {
        return when (material) {
            Material.ARMOR_STAND -> true
            Material.ITEM_FRAME -> true
            else -> false
        }
    }
}