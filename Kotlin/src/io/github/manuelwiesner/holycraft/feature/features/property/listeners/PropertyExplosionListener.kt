package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.findForbiddenBlocks
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.hanging.HangingBreakEvent

/**
 * Blocks all illegal explosions:
 * 1. TnT
 * 2. Fire Charge
 * 3. Creeper
 * 4. TnT Minecart
 * 5. Wither
 * 6. End Crystal
 * 7. TnT machine / canon / pistons etc.
 * 8. Dispensers doing those things
 *
 * Addable: ExplosionPrimeEvent
 */
class PropertyExplosionListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    // -> ExplosionPrimeEvent
    @EventHandler(ignoreCancelled = true)
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
        if (this.feature.isExplosionForbidden(event.location.chunk, null)) {
            event.isCancelled = true
            return
        }

        event.blockList().removeAll(event.blockList().findForbiddenBlocks { this.feature.isExplosionForbidden(it, null) })
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageEvent(event: EntityDamageEvent) {
        if (event.cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            if (this.feature.isExplosionForbidden(event.entity.location.chunk, null)) {
                event.isCancelled = true
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    fun onHangingBreakEvent(event: HangingBreakEvent) {
        if (event.cause != HangingBreakEvent.RemoveCause.EXPLOSION) return

        if (this.feature.isExplosionForbidden(event.entity.location.chunk, null)) {
            event.isCancelled = true
        }
    }
}