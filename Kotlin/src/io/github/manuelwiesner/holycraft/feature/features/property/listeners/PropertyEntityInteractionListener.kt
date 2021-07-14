package io.github.manuelwiesner.holycraft.feature.features.property.listeners

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.checkAttacker
import io.github.manuelwiesner.holycraft.feature.features.property.impl.findForbiddenEntities
import io.github.manuelwiesner.holycraft.feature.msg.PropertyForbiddenEntityInteractMsg
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffectType
import org.bukkit.projectiles.BlockProjectileSource
import org.bukkit.projectiles.ProjectileSource

/**
 * Blocks all illegal entity interactions:
 * 1. Killing animals (all non-mobs?, name-tagged mobs?)
 * 2. Right clicking animals (milk, wool, ride horse)
 * 3. Dispensers doing that / wolfs
 */
class PropertyEntityInteractionListener(feature: PropertyFeature) : ListenerBase<PropertyFeature>(feature) {

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractAtEntityEvent(event: PlayerInteractAtEntityEvent) {
        if (!isEntityInteractionEntity(event.rightClicked)) return

        if (this.feature.isEntityInteractionForbidden(event.rightClicked.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenEntityInteractMsg("interacting with entity").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        if (!isEntityInteractionEntity(event.rightClicked)) return

        if (this.feature.isEntityInteractionForbidden(event.rightClicked.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenEntityInteractMsg("interacting with entity").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerEggThrowEvent(event: PlayerEggThrowEvent) {
        if (this.feature.isEntityInteractionForbidden(event.egg.location.chunk, event.player)) {
            event.isHatching = false
            PropertyForbiddenEntityInteractMsg("spawning a chicken").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerShearEntityEvent(event: PlayerShearEntityEvent) {
        if (!isEntityInteractionEntity(event.entity)) return

        if (this.feature.isEntityInteractionForbidden(event.entity.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenEntityInteractMsg("shearing an entity").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerLeashEntityEvent(event: PlayerLeashEntityEvent) {
        if (!isEntityInteractionEntity(event.entity)) return

        if (this.feature.isEntityInteractionForbidden(event.entity.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenEntityInteractMsg("leashing an entity").sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerUnleashEntityEvent(event: PlayerUnleashEntityEvent) {
        if (!isEntityInteractionEntity(event.entity)) return

        if (this.feature.isEntityInteractionForbidden(event.entity.location.chunk, event.player)) {
            event.isCancelled = true
            PropertyForbiddenEntityInteractMsg("unleashing an entity").sendMessage(this.feature, event.player)
        }
    }

    // cancel all protected villager -> witch transformation (tridents)
    @EventHandler(ignoreCancelled = true)
    fun onEntityTransformEvent(event: EntityTransformEvent) {
        if (event.transformReason != EntityTransformEvent.TransformReason.LIGHTNING || event.entity !is Villager) return

        if (this.feature.isEntityInteractionForbidden(event.entity.location.chunk, null)) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityCombustByEntityEvent(event: EntityCombustByEntityEvent) {
        if (!isEntityInteractionEntity(event.entity)) return

        val player = event.combuster as? Player ?: (event.combuster as? Projectile)?.shooter as? Player

        if (this.feature.isEntityInteractionForbidden(event.entity.location.chunk, player)) {
            event.isCancelled = true
            if (player != null) PropertyForbiddenEntityInteractMsg("combusting entity").sendMessage(this.feature, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (!isEntityInteractionEntity(event.entity)) return

        val chunk = event.entity.location.chunk
        if (this.feature.checkAttacker(event.damager, false,
                { PropertyForbiddenEntityInteractMsg("damaging entity") },
                { this.feature.isEntityInteractionForbidden(chunk, it) })
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onPotionSplashEvent(event: PotionSplashEvent) {
        if (event.entity.effects.none { isHarmfulPotionEffect(it.type) }) return
        if (!shouldCancelPotionShooter(event.entity.shooter)) return
        val player = event.entity.shooter as? Player

        val forbidden = event.affectedEntities.filter { isEntityInteractionEntity(it) }
            .findForbiddenEntities { this.feature.isEntityInteractionForbidden(it, player) }
        forbidden.forEach { event.setIntensity(it, 0.0) }

        if (forbidden.isNotEmpty() && player != null) {
            PropertyForbiddenEntityInteractMsg("damaging entities").sendMessage(this.feature, player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onAreaEffectCloudApplyEvent(event: AreaEffectCloudApplyEvent) {
        if (!isHarmfulPotionEffect(event.entity.basePotionData.type.effectType)) return
        if (!shouldCancelPotionShooter(event.entity.source)) return
        val player = event.entity.source as? Player

        val forbidden = event.affectedEntities.filter { isEntityInteractionEntity(it) }
            .findForbiddenEntities { this.feature.isEntityInteractionForbidden(it, player) }
        forbidden.forEach { event.affectedEntities.remove(it) }

        if (forbidden.isNotEmpty() && player != null) {
            PropertyForbiddenEntityInteractMsg("damaging entities").sendMessage(this.feature, player)
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

        if (event.block.world.getNearbyEntities(event.block.location, 1.0, 1.0, 1.0) { isEntityInteractionEntity(it) }.isNotEmpty()) {
            val player = event.ignitingEntity as? Player ?: (event.ignitingEntity as? Projectile)?.shooter as? Player
            if (this.feature.isEntityInteractionForbidden(event.block.chunk, player)) {
                event.isCancelled = true
                if (player != null) PropertyForbiddenEntityInteractMsg("burning entities").sendMessage(this.feature, player)
            }
        }
    }

    private fun isEntityInteractionEntity(entity: Entity): Boolean {
        if (entity.customName != null) return true
        return when (entity) {
            is Animals -> true
            is Villager -> true
            else -> false
        }
    }

    private fun shouldCancelPotionShooter(shooter: ProjectileSource?): Boolean {
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