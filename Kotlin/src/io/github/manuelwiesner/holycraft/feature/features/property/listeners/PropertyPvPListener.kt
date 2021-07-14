package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.*
import io.github.manuelwiesner.holycraft.feature.msg.PropertyForbiddenPvPMsg
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.*
import org.bukkit.potion.PotionEffectType
import org.bukkit.projectiles.BlockProjectileSource
import org.bukkit.projectiles.ProjectileSource

/**
 * NOTE: Only blocking player-player pvp for now, without building rights the rest is not really possible.
 *
 * Blocks all illegal PvP activities:
 * 1. Hitting player to player
 * 2. Explosion damage via player (TNT)
 * 3. Block damage via player (Anvil, wither rose, fns, lava)
 * 4. Move player (piston, water, pushing) -> cactus, campfire
 * 5. Falling Blocks / suffocation (water, sand, concrete powder)
 * 6. Fall damage (destroy block under)
 * 7. Potions (poison, damage)
 * 8. Dispenser (arrow, lava)
 * 9. Luring mobs (zombies, withers) to player and letting the mob kill the player
 *
 * Addable: EntityDamageEvent
 */
class PropertyPvPListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return

        val chunk = event.entity.location.chunk
        if (this.feature.checkAttacker(event.damager, false,
                { PropertyForbiddenPvPMsg("damaging player") },
                { this.feature.isPvPForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityCombustByEntityEvent(event: EntityCombustByEntityEvent) {
        if (event.entity !is Player) return

        val player = event.combuster as? Player ?: (event.combuster as? Projectile)?.shooter as? Player

        if (this.feature.isPvPForbidden(event.entity.location.chunk, player)) {
            event.isCancelled = true
            if (player != null) PropertyForbiddenPvPMsg("combusting player").sendMessage(this.feature, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onBlockIgniteEvent(event: BlockIgniteEvent) {
        if (event.cause != BlockIgniteEvent.IgniteCause.FIREBALL && event.cause != BlockIgniteEvent.IgniteCause.LIGHTNING) return

        if (event.ignitingBlock?.type != Material.DISPENSER
            && event.ignitingEntity !is LightningStrike
            && event.ignitingEntity !is Fireball
            && event.ignitingEntity !is Player
        ) return

        if (event.block.world.getNearbyEntities(event.block.location, 1.0, 1.0, 1.0) { it is Player }.isNotEmpty()) {
            val player = event.ignitingEntity as? Player ?: (event.ignitingEntity as? Projectile)?.shooter as? Player
            if (this.feature.isPvPForbidden(event.block.chunk, player)) {
                event.isCancelled = true
                if (player != null) PropertyForbiddenPvPMsg("burning players").sendMessage(this.feature, player)
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPotionSplashEvent(event: PotionSplashEvent) {
        if (event.entity.effects.none { isHarmfulPotionEffect(it.type) }) return
        if (!isCancelledPotionShooter(event.entity.shooter)) return
        val player = event.entity.shooter as? Player

        if (this.feature.isPvPForbidden(event.entity.location.chunk, player)) {
            event.isCancelled = true
            if (player != null) PropertyForbiddenPvPMsg("throwing potion").sendMessage(this.feature, player)
            return
        }

        val forbidden = event.affectedEntities.filterIsInstance<Player>().findForbiddenEntities { this.feature.isPvPForbidden(it, player) }
        forbidden.forEach { event.setIntensity(it, 0.0) }

        if (forbidden.isNotEmpty() && player != null) {
            PropertyForbiddenPvPMsg("damaging players").sendMessage(this.feature, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onLingeringPotionSplashEvent(event: LingeringPotionSplashEvent) {
        if (event.entity.effects.none { isHarmfulPotionEffect(it.type) }) return
        if (!isCancelledPotionShooter(event.entity.shooter)) return
        val player = event.entity.shooter as? Player

        if (this.feature.isPvPForbidden(event.entity.location.chunk, player)) {
            event.isCancelled = true
            if (player != null) PropertyForbiddenPvPMsg("throwing potion").sendMessage(this.feature, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onAreaEffectCloudApplyEvent(event: AreaEffectCloudApplyEvent) {
        if (!isHarmfulPotionEffect(event.entity.basePotionData.type.effectType)) return
        if (!isCancelledPotionShooter(event.entity.source)) return
        val player = event.entity.source as? Player


        val forbidden = event.affectedEntities.filterIsInstance<Player>().findForbiddenEntities { this.feature.isPvPForbidden(it, player) }
        forbidden.forEach { event.affectedEntities.remove(it) }

        if (forbidden.isNotEmpty() && player != null) {
            PropertyForbiddenPvPMsg("damaging players").sendMessage(this.feature, player)
        }
    }

    private fun isCancelledPotionShooter(shooter: ProjectileSource?): Boolean {
        return when (shooter) {
            is Player -> true
            is BlockProjectileSource -> true
            else -> false
        }
    }

    private fun isHarmfulPotionEffect(effect: PotionEffectType?): Boolean {
        return when (effect) {
            PotionEffectType.WEAKNESS -> true
            PotionEffectType.UNLUCK -> true
            PotionEffectType.SLOW_DIGGING -> true
            PotionEffectType.SLOW -> true
            PotionEffectType.POISON -> true
            PotionEffectType.LEVITATION -> true
            PotionEffectType.HUNGER -> true
            PotionEffectType.HARM -> true
            PotionEffectType.BLINDNESS -> true
            else -> false
        }
    }
}