package io.github.manuelwiesner.holycraft.feature.features.property

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.event.callThisEvent
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.location
import io.github.manuelwiesner.holycraft.feature.features.spawnprotect.SpawnProtectFeature
import io.github.manuelwiesner.holycraft.feature.getFeature
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs

class PropertyCmd(feature: PropertyFeature) : CmdBaseRoot<PropertyFeature>(
    feature, "property",
    PropertyCmdBuy(feature), PropertyCmdSell(feature), PropertyCmdInfo(feature), PropertyCmdList(feature),
    PropertyCmdAllowAll(feature), PropertyCmdDisallowAll(feature), PropertyCmdAllow(feature), PropertyCmdDisallow(feature),
    PropertyCmdGrantAll(feature), PropertyCmdRevokeAll(feature), PropertyCmdGrant(feature), PropertyCmdRevoke(feature),
    PropertyCmdAdmin(feature)
)

// ---------------------------------------------------------------------------------------------------------------------

private val PROPERTY_ALLOW_TYPES: List<String> = listOf("build", "block", "entity", "chest", "explode", "causality", "pvp")
private val PROPERTY_GRANT_TYPES: List<String> = listOf("build", "block", "entity", "chest")

private val PARTICLE_TYPE: Particle = Particle.DRAGON_BREATH
private const val PARTICLE_COUNT: Int = 3

// ---------------------------------------------------------------------------------------------------------------------

private fun allowOnProperty(property: IProperty, type: String, value: Boolean): String {
    return when (type.toLowerCase()) {
        "build" -> property.setBuilding(value).let { "BUILDING" }
        "block" -> property.setBlockInteract(value).let { "BLOCK-INTERACTING" }
        "entity" -> property.setEntityInteract(value).let { "ENTITY-INTERACTING" }
        "chest" -> property.setChestInteract(value).let { "CHEST-INTERACTING" }
        "explode" -> property.setExplosion(value).let { "EXPLOSIONS" }
        "causality" -> property.setCausality(value).let { "CAUSALITY" }
        "pvp" -> property.setPvP(value).let { "PVP" }
        else -> throw InvalidArgCmdException(type, PROPERTY_ALLOW_TYPES.fancyJoin())
    }
}

private fun grantOnProperty(property: IProperty, uuid: UUID, type: String, value: Boolean): Pair<String, Boolean> {
    return when (type.toLowerCase()) {
        "build" -> "BUILDING" to if (value) property.allowBuild(uuid) else property.disallowBuild(uuid)
        "block" -> "BLOCK-INTERACTING" to if (value) property.allowBlockInteract(uuid) else property.disallowBlockInteract(uuid)
        "entity" -> "ENTITY-INTERACTING" to if (value) property.allowEntityInteract(uuid) else property.disallowEntityInteract(uuid)
        "chest" -> "CHEST-INTERACTING" to if (value) property.allowChestInteract(uuid) else property.disallowChestInteract(uuid)
        else -> throw InvalidArgCmdException(type, PROPERTY_GRANT_TYPES.fancyJoin())
    }
}

private fun IPropertyFeature.getProperty(player: Player, asOwner: Boolean): IProperty {
    val chunk = player.location.chunk.location()
    outlineChunk(chunk.chunkX, chunk.chunkZ, player)

    val property = getProperty(chunk) ?: throw ReturnMessageException(
        PropertyUnownedCmdMsg(chunk, getNextPropertyPrice(player, player.world.environment != World.Environment.NORMAL))
    )

    return property.takeUnless { asOwner && property.getOwner() != player.uniqueId }
        ?: throw ReturnMessageException(PropertyNotYoursCmdMsg(chunk))
}

private fun IPropertyFeature.executeTransaction(player: UUID, amount: Int, reason: String, chunk: ChunkLocation): Boolean {
    return this.getEconomyFeature().executeTransaction(player, amount, "property-$reason-${chunk.chunkX}:${chunk.chunkZ}")
}

private fun outlineChunk(x: Int, z: Int, player: Player) {
    val playerY = player.location.blockY
    val minX = x * 16
    val maxX = minX + 16
    val minZ = z * 16
    val maxZ = minZ + 16

    for (i in 0..16) {
        for (y in playerY..(playerY + 10)) {
            player.spawnParticle(PARTICLE_TYPE, (minX + i).toDouble(), y.toDouble(), minZ.toDouble(), PARTICLE_COUNT, 0.1, 0.1, 0.1, 0.0)
            player.spawnParticle(PARTICLE_TYPE, (minX + i).toDouble(), y.toDouble(), maxZ.toDouble(), PARTICLE_COUNT, 0.1, 0.1, 0.1, 0.0)
            player.spawnParticle(PARTICLE_TYPE, minX.toDouble(), y.toDouble(), (minZ + i).toDouble(), PARTICLE_COUNT, 0.1, 0.1, 0.1, 0.0)
            player.spawnParticle(PARTICLE_TYPE, maxX.toDouble(), y.toDouble(), (minZ + i).toDouble(), PARTICLE_COUNT, 0.1, 0.1, 0.1, 0.0)
        }
    }
}

private fun ensureCorrectWorld(player: Player) {
    val environment = player.world.environment
    if (environment != World.Environment.NORMAL && environment != World.Environment.NETHER) {
        throw ReturnMessageException(PropertyWrongEnvironmentCmdMsg(environment))
    }
}

private fun isCloseEnough(chunk: ChunkLocation, player: Player): Boolean {
    val playerChunk = player.location.chunk
    val differenceX = abs(chunk.chunkX - playerChunk.x)
    val differenceZ = abs(chunk.chunkZ - playerChunk.z)

    return differenceX < 2 && differenceZ < 2
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdBuy(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "buy") {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()
        outlineChunk(chunk.chunkX, chunk.chunkZ, player)

        val price = this.feature.getNextPropertyPrice(player, player.world.environment != World.Environment.NORMAL)

        // check if it's already owned
        if (this.feature.getProperty(chunk) != null) return PropertyAlreadyOwnedCmdMsg(chunk)


        return PropertyBuyEvent(player, player.location.chunk).callThisEvent({
            // check if sufficient funds and do transaction
            if (!this.feature.executeTransaction(player.uniqueId, -price, "buy", chunk)) return@callThisEvent PropertyMissingFundsCmdMsg(chunk, price)

            // add property and refund if someone else was faster
            if (!this.feature.addProperty(chunk, player, price)) {
                this.feature.executeTransaction(player.uniqueId, price, "buy_refund", chunk) // refund money, someone else was faster
                return@callThisEvent PropertyAlreadyOwnedCmdMsg(chunk)
            }

            return@callThisEvent PropertyBoughtCmdMsg(chunk, price)
        }, {
            PropertyCanNotBuyCmdMsg(chunk)
        })

    }
}

// ---------------------------------------------------------------------------------------------------------------------


class PropertyCmdSell(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "sell") {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()

        // get owned property and refund amount
        val property = this.feature.getProperty(player, true)
        val refund = this.feature.getRefundAmount(property.getPricePaid())

        return PropertySellEvent(player, property).callThisEvent({
            // remove from owned properties and refund money
            this.feature.removeProperty(chunk)
            this.feature.executeTransaction(player.uniqueId, refund, "sell", chunk)
            PropertySoldCmdMsg(chunk, refund)
        }, {
            PropertyCanNotSellCmdMsg(chunk)
        })
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdInfo(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "info") {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message? {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()
        val property = this.feature.getProperty(player, false)

        val owner = Bukkit.getOfflinePlayer(property.getOwner())
        PropertyInfoHeaderCmdMsg(chunk, owner).sendMessage(this.feature, player)

        val building = property.isBuildingAllowed()
        val blockInteracting = property.isBlockInteractAllowed()
        val entityInteracting = property.isEntityInteractAllowed()
        val chestInteracting = property.isChestInteractAllowed()
        val exploding = property.isExplosionAllowed()
        val causality = property.isExplosionAllowed()
        val pvp = property.isPvPAllowed()
        PropertyInfoValuesCmdMsg(building, blockInteracting, entityInteracting, chestInteracting, exploding, causality, pvp)
            .sendMessage(this.feature, player)

        property.getBuilders().takeIf { it.isNotEmpty() }?.fancyJoin { Bukkit.getOfflinePlayer(it).nameOrId() }
            ?.let { PropertyInfoGrantedPlayersCmdMsg("Building", it).sendMessage(this.feature, player) }

        property.getBlockInteractors().takeIf { it.isNotEmpty() }?.fancyJoin { Bukkit.getOfflinePlayer(it).nameOrId() }
            ?.let { PropertyInfoGrantedPlayersCmdMsg("Block-Interacting", it).sendMessage(this.feature, player) }

        property.getEntityInteractors().takeIf { it.isNotEmpty() }?.fancyJoin { Bukkit.getOfflinePlayer(it).nameOrId() }
            ?.let { PropertyInfoGrantedPlayersCmdMsg("Entity-Interacting", it).sendMessage(this.feature, player) }

        property.getChestInteractors().takeIf { it.isNotEmpty() }?.fancyJoin { Bukkit.getOfflinePlayer(it).nameOrId() }
            ?.let { PropertyInfoGrantedPlayersCmdMsg("Chest-Interacting", it).sendMessage(this.feature, player) }

        if (property.getOwner() == player.uniqueId) {
            PropertyInfoRefundCmdMsg(property.getPricePaid(), this.feature.getRefundAmount(property.getPricePaid())).sendMessage(this.feature, player)
        }

        PropertyInfoFooterCmdMsg().sendMessage(this.feature, player)

        return null
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdList(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "list") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message? {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val properties = this.feature.getProperties(player.uniqueId)

        PropertyListHeaderCmdMsg(properties.size).sendMessage(this.feature, player)

        properties.forEach {
            val chunk = it.getChunkLocation()
            if (isCloseEnough(chunk, player)) {
                outlineChunk(chunk.chunkX, chunk.chunkZ, player)
            }

            PropertyListEntryCmdMsg(it.getChunkLocation(), it.getPricePaid()).sendMessage(this.feature, player)
        }

        PropertyListFooterCmdMsg().sendMessage(this.feature, player)

        return null
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdAllowAll(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "allow-all", CmdArg.TEXT(PROPERTY_ALLOW_TYPES)) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val allowType = args.text(0)

        var allowName: String? = null

        for (property in this.feature.getProperties(player.uniqueId)) {
            val chunk = property.getChunkLocation()
            if (isCloseEnough(chunk, player)) {
                outlineChunk(chunk.chunkX, chunk.chunkZ, player)
            }

            val name = allowOnProperty(property, allowType, true)
            if (allowName == null) allowName = name
        }

        return allowName?.let { PropertyAllowAllCmdMsg(it) } ?: PropertyNoPropertiesOwned()
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdDisallowAll(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "disallow-all", CmdArg.TEXT(PROPERTY_ALLOW_TYPES)) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val allowType = args.text(0)

        var allowName: String? = null

        for (property in this.feature.getProperties(player.uniqueId)) {
            val chunk = property.getChunkLocation()
            if (isCloseEnough(chunk, player)) {
                outlineChunk(chunk.chunkX, chunk.chunkZ, player)
            }

            val name = allowOnProperty(property, allowType, false)
            if (allowName == null) allowName = name
        }

        return allowName?.let { PropertyDisallowAllCmdMsg(it) } ?: PropertyNoPropertiesOwned()
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdAllow(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(
    feature, "allow",
    CmdArg.TEXT(PROPERTY_ALLOW_TYPES)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()
        val property = this.feature.getProperty(player, true)

        return PropertyAllowCmdMsg(chunk, allowOnProperty(property, args.text(0), true))
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdDisallow(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(
    feature, "disallow",
    CmdArg.TEXT(PROPERTY_ALLOW_TYPES)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()
        val property = this.feature.getProperty(player, true)

        return PropertyDisallowCmdMsg(chunk, allowOnProperty(property, args.text(0), false))
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdGrantAll(feature: PropertyFeature) :
    CmdBaseArgs<PropertyFeature>(feature, "grant-all", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT(PROPERTY_GRANT_TYPES)) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val grantee = args.offlinePlayer(0)
        if (grantee.uniqueId == player.uniqueId) return PropertySelfGrantCmdMsg()
        val grantType = args.text(1)

        var grantName: String? = null
        var numGranted = 0
        var numAlreadyGranted = 0

        for (property in this.feature.getProperties(player.uniqueId)) {
            val chunk = property.getChunkLocation()
            if (isCloseEnough(chunk, player)) {
                outlineChunk(chunk.chunkX, chunk.chunkZ, player)
            }

            val pair = grantOnProperty(property, grantee.uniqueId, grantType, true)

            if (grantName == null) grantName = pair.first

            if (pair.second) {
                numGranted++
            } else numAlreadyGranted++
        }

        return grantName?.let { PropertyGrantAllCmdMsg(grantee, numGranted, numAlreadyGranted, it) } ?: PropertyNoPropertiesOwned()
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdRevokeAll(feature: PropertyFeature) :
    CmdBaseArgs<PropertyFeature>(feature, "revoke-all", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT(PROPERTY_GRANT_TYPES)) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val revokee = args.offlinePlayer(0)
        if (revokee.uniqueId == player.uniqueId) return PropertySelfGrantCmdMsg()
        val grantType = args.text(1)

        var grantName: String? = null
        var numRevoked = 0
        var numAlreadyRevoked = 0

        for (property in this.feature.getProperties(player.uniqueId)) {
            val chunk = property.getChunkLocation()
            if (isCloseEnough(chunk, player)) {
                outlineChunk(chunk.chunkX, chunk.chunkZ, player)
            }

            val pair = grantOnProperty(property, revokee.uniqueId, grantType, false)

            if (grantName == null) grantName = pair.first

            if (pair.second) {
                numRevoked++
            } else numAlreadyRevoked++
        }

        return grantName?.let { PropertyRevokeAllCmdMsg(revokee, numRevoked, numAlreadyRevoked, it) } ?: PropertyNoPropertiesOwned()
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdGrant(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(
    feature, "grant", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT(PROPERTY_GRANT_TYPES)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()
        val grantee = args.offlinePlayer(0)
        if (grantee.uniqueId == player.uniqueId) return PropertySelfGrantCmdMsg()
        val grantType = args.text(1)

        val property = this.feature.getProperty(player, true)

        val grantPair = grantOnProperty(property, grantee.uniqueId, grantType, true)

        return if (grantPair.second) {
            PropertyGrantCmdMsg(chunk, grantee, grantPair.first)
        } else {
            PropertyAlreadyGrantedCmdMsg(chunk, grantee, grantPair.first)
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdRevoke(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(
    feature, "revoke", CmdArg.OFFLINE_PLAYER, CmdArg.TEXT(PROPERTY_GRANT_TYPES)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = ensurePlayer(sender)
        ensureCorrectWorld(player)

        val chunk = player.location.chunk.location()
        val revokee = args.offlinePlayer(0)
        if (revokee.uniqueId == player.uniqueId) return PropertySelfGrantCmdMsg()
        val grantType = args.text(1)

        val property = this.feature.getProperty(player, true)

        val grantPair = grantOnProperty(property, revokee.uniqueId, grantType, false)

        return if (grantPair.second) {
            PropertyRevokeCmdMsg(chunk, revokee, grantPair.first)
        } else {
            PropertyAlreadyRevokedCmdMsg(chunk, revokee, grantPair.first)
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdAdmin(feature: PropertyFeature) : CmdBaseRoot<PropertyFeature>(
    feature, "admin",
    PropertyCmdAdminBuy(feature), PropertyCmdAdminSell(feature), PropertyCmdAdminList(feature), PropertyCmdAdminClaimSpawn(feature)
) {
    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdAdminBuy(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "buy", CmdArg.OFFLINE_PLAYER, CmdArg.FLAG) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val adminPlayer = ensurePlayer(sender)
        val buyPlayer = args.offlinePlayer(0)
        val payFlag = args.bool(1)

        val chunk = adminPlayer.location.chunk.location()
        outlineChunk(chunk.chunkX, chunk.chunkZ, adminPlayer)

        val buyPrice = this.feature.getNextPropertyPrice(buyPlayer, adminPlayer.world.environment != World.Environment.NORMAL)

        // check if it's already owned
        if (this.feature.getProperty(chunk) != null) return PropertyAlreadyOwnedCmdMsg(chunk)

        if (payFlag) {
            // check if sufficient funds and do transaction
            if (!this.feature.executeTransaction(buyPlayer.uniqueId, -buyPrice, "admin-buy", chunk)) {
                return PropertyAdminMissingFundsCmdMsg(chunk, buyPlayer, buyPrice)
            }
        }

        if (!this.feature.addProperty(chunk, buyPlayer, if (payFlag) buyPrice else 0)) {
            if (payFlag) {
                this.feature.executeTransaction(buyPlayer.uniqueId, buyPrice, "admin-buy_refund", chunk)
            } // refund money, someone else was faster
            return PropertyAlreadyOwnedCmdMsg(chunk)
        }

        return PropertyAdminBoughtCmdMsg(chunk, buyPlayer, if (payFlag) buyPrice else 0)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------


class PropertyCmdAdminSell(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "sell", CmdArg.FLAG) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val adminPlayer = ensurePlayer(sender)
        val payFlag = args.bool(0)

        val chunk = adminPlayer.location.chunk.location()
        outlineChunk(chunk.chunkX, chunk.chunkZ, adminPlayer)

        // get owned property and refund amount
        val property = this.feature.getProperty(adminPlayer, false)
        val refund = this.feature.getRefundAmount(property.getPricePaid())

        // remove from owned properties and refund money
        this.feature.removeProperty(chunk)

        if (payFlag) {
            this.feature.executeTransaction(property.getOwner(), refund, "admin-sell", chunk)
        }

        return PropertyAdminSoldCmdMsg(chunk, Bukkit.getOfflinePlayer(property.getOwner()), refund)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdAdminList(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "list", CmdArg.OFFLINE_PLAYER) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message? {
        val adminPlayer = ensurePlayer(sender)
        val player = args.offlinePlayer(0)

        val properties = this.feature.getProperties(player.uniqueId)

        PropertyAdminListHeaderCmdMsg(player, properties.size).sendMessage(this.feature, adminPlayer)

        properties.forEach {
            val chunk = it.getChunkLocation()
            if (isCloseEnough(chunk, adminPlayer)) {
                outlineChunk(chunk.chunkX, chunk.chunkZ, adminPlayer)
            }

            PropertyListEntryCmdMsg(it.getChunkLocation(), it.getPricePaid()).sendMessage(this.feature, adminPlayer)
        }

        PropertyListFooterCmdMsg().sendMessage(this.feature, adminPlayer)

        return null
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class PropertyCmdAdminClaimSpawn(feature: PropertyFeature) : CmdBaseArgs<PropertyFeature>(feature, "claim-spawn", CmdArg.FLAG) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message? {
        val player = ensurePlayer(sender)
        val forceClaim = args.bool(0)

        val spawnProtect = this.feature.getManager().getFeature<SpawnProtectFeature>()
            ?: throw IllegalStateException("SpawnProtectFeature not found!")

        var newlyClaimed = 0
        var alreadyClaimed = 0
        var removed = 0

        for (chunk in spawnProtect.getSpawnChunks()) {
            val property = this.feature.getProperty(chunk)

            if (property != null) {
                if (property.getOwner() == player.uniqueId) {
                    alreadyClaimed++
                    continue
                }

                if (!forceClaim) {
                    continue
                }

                removed++

                val refund = this.feature.getRefundAmount(property.getPricePaid())
                this.feature.executeTransaction(property.getOwner(), refund, "admin-spawn-claim", chunk)
                this.feature.removeProperty(chunk)
            }

            newlyClaimed++
            this.feature.addProperty(chunk, player, 0)
        }

        return PropertyAdminSpawnClaimCmdMsg(newlyClaimed, alreadyClaimed, removed)
    }

    override fun acceptsSender(sender: CommandSender) = sender.isOp
}