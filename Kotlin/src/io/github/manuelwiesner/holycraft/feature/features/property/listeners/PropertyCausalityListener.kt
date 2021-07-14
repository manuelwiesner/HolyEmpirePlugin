package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.findForbidden
import io.github.manuelwiesner.holycraft.feature.features.property.impl.location
import io.github.manuelwiesner.holycraft.feature.msg.PropertyForbiddenBuildMsg
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.block.*
import org.bukkit.event.world.StructureGrowEvent

/**
 * Listens for changes that are indirectly caused from another property or simply not allowed:
 * 1. Grow trees (also mushroom trees etc.)
 * 2. Tnt canons, or simply radius
 * 3. Pistons (pull blocks, flying machines)
 * 4. Dispenser placing lava
 *
 * Addable: BlockFormEvent, BlockRedstoneEvent
 */
class PropertyCausalityListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    @EventHandler(ignoreCancelled = true)
    fun onStructureGrowEvent(event: StructureGrowEvent) {
        val player = event.player ?: return // trees without bone meal always grow
        val forbiddenBlocks = event.blocks.findForbidden({ it.chunk }) { this.feature.isCausalityForbidden(it, player) }

        if (forbiddenBlocks.find { it.location == event.location } == null) {
            forbiddenBlocks.forEach { event.blocks.remove(it) }
        } else {
            event.location.block.setType(Material.AIR, true)
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockIgniteEvent(event: BlockIgniteEvent) {
        if (event.cause != BlockIgniteEvent.IgniteCause.SPREAD) return

        val player = event.ignitingEntity as? Player ?: (event.ignitingEntity as? Projectile)?.shooter as? Player
        if (this.feature.isCausalityForbidden(event.block.chunk, player)) {
            event.isCancelled = true
            if (player != null) PropertyForbiddenBuildMsg("spreading fire").sendMessage(this.feature, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockBurnEvent(event: BlockBurnEvent) {
        if (this.feature.isCausalityForbidden(event.block.chunk, null)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockFromToEvent(event: BlockFromToEvent) {
        val fromChunk = event.block.chunk
        val toChunk = event.toBlock.chunk

        if (fromChunk == toChunk) return

        val propertyTo = this.feature.getProperty(toChunk.location()) ?: return
        val propertyFrom = this.feature.getProperty(fromChunk.location())

        if (propertyFrom != null && propertyFrom.getOwner() == propertyTo.getOwner()) return

        if (!propertyTo.isBuildingAllowed() && !propertyTo.isCausalityAllowed()) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPistonExtendEvent(event: BlockPistonExtendEvent) {
        checkCausalityEvent(event, event.block.chunk, event.blocks.toMutableList().also { list ->
            list.addAll(event.blocks.map { it.getRelative(event.direction) })
        }.map { it.chunk }.distinct())
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockPistonRetractEvent(event: BlockPistonRetractEvent) {
        checkCausalityEvent(event, event.block.chunk, event.blocks.map { it.chunk }.distinct())
    }

    private fun checkCausalityEvent(event: Cancellable, startingChunk: Chunk, affectedChunks: List<Chunk>) {
        if (affectedChunks.size == 1 && affectedChunks.first() == startingChunk) return

        val ownerStartingChunk = this.feature.getProperty(startingChunk.location())?.getOwner()

        for (chunk in affectedChunks) {
            if (chunk == startingChunk) continue
            val property = this.feature.getProperty(chunk.location()) ?: continue

            if (!property.isBuildingAllowed() && !property.isCausalityAllowed() && (ownerStartingChunk == null || property.getOwner() != ownerStartingChunk)) {
                event.isCancelled = true
                return
            }
        }
    }
}