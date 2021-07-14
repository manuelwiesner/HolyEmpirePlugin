package io.github.manuelwiesner.holycraft.feature.features.property

import io.github.manuelwiesner.holycraft.feature.features.economy.EconomyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import org.bukkit.Chunk
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

interface IPropertyFeature {
    fun getProperty(chunk: ChunkLocation): IProperty?
    fun addProperty(chunk: ChunkLocation, player: OfflinePlayer, price: Int): Boolean
    fun removeProperty(chunk: ChunkLocation): Boolean

    fun getProperties(playerId: UUID): List<IProperty>

    fun getEconomyFeature(): EconomyFeature
    fun getNextPropertyPrice(player: OfflinePlayer, otherWorldFlag: Boolean): Int
    fun getRefundAmount(paidAmount: Int): Int

    fun isBuildingForbidden(chunk: Chunk, player: Player?): Boolean
    fun isBlockInteractionForbidden(chunk: Chunk, player: Player?): Boolean
    fun isEntityInteractionForbidden(chunk: Chunk, player: Player?): Boolean
    fun isChestInteractionForbidden(chunk: Chunk, player: Player?): Boolean
    fun isExplosionForbidden(chunk: Chunk, player: Player?): Boolean
    fun isCausalityForbidden(chunk: Chunk, player: Player?): Boolean
    fun isPvPForbidden(chunk: Chunk, player: Player?): Boolean
}