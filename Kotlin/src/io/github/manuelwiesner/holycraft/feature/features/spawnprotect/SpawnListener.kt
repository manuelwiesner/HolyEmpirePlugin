package io.github.manuelwiesner.holycraft.feature.features.spawnprotect

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.PropertyBuyEvent
import io.github.manuelwiesner.holycraft.feature.features.property.PropertySellEvent
import io.github.manuelwiesner.holycraft.feature.features.property.impl.location
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerRespawnEvent

class SpawnListener(feature: SpawnProtectFeature) : ListenerBase<SpawnProtectFeature>(feature) {

    @EventHandler
    fun onCreatureSpawnEvent(e: CreatureSpawnEvent) {
        if (this.feature.isProtected(e.location.chunk.location())) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onEntityDamageEvent(e: EntityDamageEvent) {
        if (e.entityType != EntityType.PLAYER || e.cause == EntityDamageEvent.DamageCause.VOID) return

        if (this.feature.isProtected(e.entity.location.chunk.location())) {
            val location = e.entity.location
            if (!(location.blockX == 776 && location.blockZ == 327 && location.blockY == 69)) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerRespawnEvent(e: PlayerRespawnEvent) {
        val active = this.feature.getActive()
        if (active != null && active.uniqueId == e.player.uniqueId) {
            e.respawnLocation = this.feature.getLocation()
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPropertyBuyEvent(event: PropertyBuyEvent) {
        if (!event.player.isOp && this.feature.isProtected(event.chunk.location())) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPropertySellEvent(event: PropertySellEvent) {
        if (!event.player.isOp && this.feature.isProtected(event.property.getChunkLocation())) {
            event.isCancelled = true
        }
    }
}