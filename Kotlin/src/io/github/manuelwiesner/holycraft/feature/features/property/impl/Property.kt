package io.github.manuelwiesner.holycraft.feature.features.property.impl

import io.github.manuelwiesner.holycraft.feature.features.property.IProperty
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class Property(
    private val owner: UUID,
    private val pricePaid: Int,
    private val chunkLocation: ChunkLocation,
    private var newlyCreated: AtomicBoolean,
    private val builders: MutableList<UUID> = Collections.synchronizedList(arrayListOf()),
    private val blockInteractors: MutableList<UUID> = Collections.synchronizedList(arrayListOf()),
    private val entityInteractors: MutableList<UUID> = Collections.synchronizedList(arrayListOf()),
    private val chestInteractors: MutableList<UUID> = Collections.synchronizedList(arrayListOf()),
    private var allowBuilding: Boolean = false,
    private var allowBlockInteracting: Boolean = false,
    private var allowEntityInteracting: Boolean = false,
    private var allowChestInteracting: Boolean = false,
    private var allowExplosions: Boolean = false,
    private var allowCausality: Boolean = false,
    private var allowPvP: Boolean = true
) : IProperty {

    fun updateNewlyCreated(): Boolean {
        return this.newlyCreated.compareAndSet(true, false)
    }

    override fun canBuild(uuid: UUID): Boolean {
        return isBuildingAllowed() || uuid in this.builders || uuid == this.owner
    }

    override fun canBlockInteract(uuid: UUID): Boolean {
        return canBuild(uuid) || isBlockInteractAllowed() || uuid in this.blockInteractors
    }

    override fun canEntityInteract(uuid: UUID): Boolean {
        return canBuild(uuid) || isEntityInteractAllowed() || uuid in this.entityInteractors
    }

    override fun canChestInteract(uuid: UUID): Boolean {
        return canBuild(uuid) || isChestInteractAllowed() || uuid in this.chestInteractors
    }

    override fun allowBuild(uuid: UUID): Boolean {
        return this.builders.takeUnless { uuid in this.builders }?.add(uuid) ?: false
    }

    override fun allowBlockInteract(uuid: UUID): Boolean {
        return this.blockInteractors.takeUnless { uuid in this.blockInteractors }?.add(uuid) ?: false
    }

    override fun allowEntityInteract(uuid: UUID): Boolean {
        return this.entityInteractors.takeUnless { uuid in this.entityInteractors }?.add(uuid) ?: false
    }

    override fun allowChestInteract(uuid: UUID): Boolean {
        return this.chestInteractors.takeUnless { uuid in this.chestInteractors }?.add(uuid) ?: false
    }

    override fun disallowBuild(uuid: UUID): Boolean {
        return this.builders.remove(uuid)
    }

    override fun disallowBlockInteract(uuid: UUID): Boolean {
        return this.blockInteractors.remove(uuid)
    }

    override fun disallowEntityInteract(uuid: UUID): Boolean {
        return this.entityInteractors.remove(uuid)
    }

    override fun disallowChestInteract(uuid: UUID): Boolean {
        return this.chestInteractors.remove(uuid)
    }

    override fun isBuildingAllowed(): Boolean {
        return this.allowBuilding
    }

    override fun isBlockInteractAllowed(): Boolean {
        return this.allowBlockInteracting
    }

    override fun isEntityInteractAllowed(): Boolean {
        return this.allowEntityInteracting
    }

    override fun isChestInteractAllowed(): Boolean {
        return this.allowChestInteracting
    }

    override fun isExplosionAllowed(): Boolean {
        return this.allowExplosions
    }

    override fun isCausalityAllowed(): Boolean {
        return this.allowCausality
    }

    override fun isPvPAllowed(): Boolean {
        return this.allowPvP
    }

    override fun setBuilding(allowed: Boolean) {
        this.allowBuilding = allowed
    }

    override fun setBlockInteract(allowed: Boolean) {
        this.allowBlockInteracting = allowed
    }

    override fun setEntityInteract(allowed: Boolean) {
        this.allowEntityInteracting = allowed
    }

    override fun setChestInteract(allowed: Boolean) {
        this.allowChestInteracting = allowed
    }

    override fun setExplosion(allowed: Boolean) {
        this.allowExplosions = allowed
    }

    override fun setCausality(allowed: Boolean) {
        this.allowCausality = allowed
    }

    override fun setPvP(allowed: Boolean) {
        this.allowPvP = allowed
    }

    override fun getOwner(): UUID {
        return this.owner
    }

    override fun getBuilders(): List<UUID> {
        return this.builders
    }

    override fun getBlockInteractors(): List<UUID> {
        return this.blockInteractors
    }

    override fun getEntityInteractors(): List<UUID> {
        return this.entityInteractors
    }

    override fun getChestInteractors(): List<UUID> {
        return this.chestInteractors
    }

    override fun getPricePaid(): Int {
        return this.pricePaid
    }

    override fun getChunkLocation(): ChunkLocation {
        return this.chunkLocation
    }
}