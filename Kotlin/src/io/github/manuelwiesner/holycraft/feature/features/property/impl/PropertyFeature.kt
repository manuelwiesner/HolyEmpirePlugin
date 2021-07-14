package io.github.manuelwiesner.holycraft.feature.features.property.impl

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.feature.features.economy.EconomyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.*
import io.github.manuelwiesner.holycraft.feature.features.property.listeners.*
import io.github.manuelwiesner.holycraft.feature.getFeature
import io.github.manuelwiesner.holycraft.feature.msg.Message
import io.github.manuelwiesner.holycraft.player.View
import io.github.manuelwiesner.holycraft.store.Store
import io.github.manuelwiesner.holycraft.store.StoreConverter
import io.github.manuelwiesner.holycraft.yaml.SafeYaml
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.block.Block
import org.bukkit.entity.*
import org.bukkit.projectiles.BlockProjectileSource
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class PropertyFeature(manager: _FeatureManager) :
    FeatureBase<EconomyFeature>(manager, "PROPERTY", { manager.getFeature()!! }), IPropertyFeature {

    private val refundMultiplier: SafeYaml<Double> = getHolyCraft().getYamlManager()
        .getDoubleWrapper("property.refundMultiplier").makeSafe(1.0)

    private val storedProperties: Store<ChunkLocation, Property> = getHolyCraft().getStoreManager()
        .getStore("properties", ChunkLocationConverter, PropertyConverter)

    private val ownedPropertiesView: View<MutableList<ChunkLocation>> = getHolyCraft().getPlayerManager()
        .getView("properties", StoreConverter.LIST(ChunkLocationConverter))

    init {
        this.children += PropertyCmd(this)
        this.children += PropertyBuildingListener(this)
        this.children += PropertyBlockInteractionListener(this)
        this.children += PropertyEntityInteractionListener(this)
        this.children += PropertyChestInteractionListener(this)
        this.children += PropertyCausalityListener(this)
        this.children += PropertyExplosionListener(this)
        this.children += PropertyPvPListener(this)
    }

    override fun getProperty(chunk: ChunkLocation): IProperty? {
        return this.storedProperties[chunk]
    }

    override fun addProperty(chunk: ChunkLocation, player: OfflinePlayer, price: Int): Boolean {
        return this.storedProperties.computeIfAbsent(chunk) {
            getOwnedProperties(player.uniqueId) += it
            Property(player.uniqueId, price, chunk, AtomicBoolean(true))
        }.updateNewlyCreated()
    }

    override fun removeProperty(chunk: ChunkLocation): Boolean {
        val oldProperty = this.storedProperties[chunk]
        return if (oldProperty != null) {
            this.storedProperties.remove(chunk)
            getOwnedProperties(oldProperty.getOwner()).remove(chunk)
            true
        } else false
    }

    override fun getProperties(playerId: UUID): List<IProperty> {
        return this.storedProperties.raw().values.filter { it.getOwner() == playerId }
    }

    override fun getEconomyFeature(): EconomyFeature {
        return getItem()
    }

    override fun getNextPropertyPrice(player: OfflinePlayer, otherWorldFlag: Boolean): Int {
        if (otherWorldFlag) return 2000

        return when (val ownedProperties = this.ownedPropertiesView[player.uniqueId]?.size?.plus(1) ?: 0) {
            in 0..14 -> 0
            in 15..24 -> 200
            in 25..34 -> 400
            in 35..44 -> 600
            in 45..54 -> 800
            else -> 1000
        }
    }

    override fun getRefundAmount(paidAmount: Int): Int {
        return (paidAmount.toDouble() * this.refundMultiplier.get()).roundToInt()
    }

    override fun isBuildingForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        return player?.let { !property.canBuild(it.uniqueId) } ?: !property.isBuildingAllowed()

    }

    override fun isBlockInteractionForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        return player?.let { !property.canBlockInteract(it.uniqueId) } ?: !property.isBlockInteractAllowed()
    }

    override fun isEntityInteractionForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        return player?.let { !property.canEntityInteract(it.uniqueId) } ?: !property.isEntityInteractAllowed()
    }

    override fun isChestInteractionForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        return player?.let { !property.canChestInteract(it.uniqueId) } ?: !property.isChestInteractAllowed()
    }

    override fun isExplosionForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        return !property.isExplosionAllowed()
    }

    override fun isCausalityForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        val buildingForbidden = player?.let { !property.canBuild(it.uniqueId) } ?: !property.isBuildingAllowed()
        return buildingForbidden && !property.isCausalityAllowed()
    }

    override fun isPvPForbidden(chunk: Chunk, player: Player?): Boolean {
        if (player?.isOp == true) return false
        val property = getProperty(chunk.location()) ?: return false
        return !property.isPvPAllowed()
    }

    private fun getOwnedProperties(uuid: UUID): MutableList<ChunkLocation> {
        return this.ownedPropertiesView.computeIfAbsent(uuid) { Collections.synchronizedList(arrayListOf()) }
    }
}

fun PropertyFeature.checkAttacker(attacker: Entity?, checkNull: Boolean, message: () -> Message, isForbidden: (Player?) -> Boolean): Boolean {
    // no attacker -> lightning, tnt?
    if (attacker == null && checkNull) {
        return isForbidden(null)
    }

    // attacker is player
    if (attacker is Player) {
        if (isForbidden(attacker)) {
            message().sendMessage(this, attacker)
            return true
        }
        return false
    }

    // attacker is lightning
    if (attacker is LightningStrike) {
        return isForbidden(null)
    }

    // attacker is projectile
    if (attacker is Projectile) {
        val shooter = attacker.shooter

        // projectile shooter is player
        if (shooter is Player) {
            if (isForbidden(shooter)) {
                message().sendMessage(this, shooter)
                return true
            }
            return false
        }

        // projectile shooter is dispenser
        if (shooter is BlockProjectileSource) {
            return isForbidden(null)
        }

        // projectile shooter is wither (WitherSkull)
        if (shooter is Wither) {
            return isForbidden(null)
        }
    }

    return false
}

fun <T : Block> Iterable<T>.findForbiddenBlocks(isForbidden: (Chunk) -> Boolean): List<T> {
    return this.findForbidden({ it.chunk }, isForbidden)
}

fun <T : LivingEntity> Iterable<T>.findForbiddenEntities(isForbidden: (Chunk) -> Boolean): List<T> {
    return this.findForbidden({ it.location.chunk }, isForbidden)
}

fun <T> Iterable<T>.findForbidden(transformer: (T) -> Chunk, isForbidden: (Chunk) -> Boolean): List<T> {
    val forbiddenMap = hashMapOf<Chunk, Boolean>()
    for (obj in this) forbiddenMap[transformer(obj)] = false
    for (chunk in forbiddenMap.keys) forbiddenMap[chunk] = isForbidden(chunk)

    return this.filter { forbiddenMap[transformer(it)] == true }
}