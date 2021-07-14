package io.github.manuelwiesner.holycraft.feature.features.property

import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import java.util.UUID

interface IProperty {
    fun canBuild(uuid: UUID): Boolean
    fun canBlockInteract(uuid: UUID): Boolean
    fun canEntityInteract(uuid: UUID): Boolean
    fun canChestInteract(uuid: UUID): Boolean

    fun allowBuild(uuid: UUID): Boolean
    fun allowBlockInteract(uuid: UUID): Boolean
    fun allowEntityInteract(uuid: UUID): Boolean
    fun allowChestInteract(uuid: UUID): Boolean

    fun disallowBuild(uuid: UUID): Boolean
    fun disallowBlockInteract(uuid: UUID): Boolean
    fun disallowEntityInteract(uuid: UUID): Boolean
    fun disallowChestInteract(uuid: UUID): Boolean

    fun isBuildingAllowed(): Boolean
    fun isBlockInteractAllowed(): Boolean
    fun isEntityInteractAllowed(): Boolean
    fun isChestInteractAllowed(): Boolean
    fun isExplosionAllowed(): Boolean
    fun isCausalityAllowed(): Boolean
    fun isPvPAllowed(): Boolean

    fun setBuilding(allowed: Boolean)
    fun setBlockInteract(allowed: Boolean)
    fun setEntityInteract(allowed: Boolean)
    fun setChestInteract(allowed: Boolean)
    fun setExplosion(allowed: Boolean)
    fun setCausality(allowed: Boolean)
    fun setPvP(allowed: Boolean)

    fun getOwner(): UUID
    fun getBuilders(): List<UUID>
    fun getBlockInteractors(): List<UUID>
    fun getEntityInteractors(): List<UUID>
    fun getChestInteractors(): List<UUID>

    fun getPricePaid(): Int
    fun getChunkLocation(): ChunkLocation
}