package io.github.manuelwiesner.holycraft.feature.features.chestshop

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.manuelwiesner.holycraft.feature.features.economy.EconomyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import io.github.manuelwiesner.holycraft.feature.msg.*
import io.github.manuelwiesner.holycraft.store.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.DoubleChestInventory
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

data class BlockLocation(val x: Int, val y: Int, val z: Int, val world: UUID) {
    fun toChunk() = ChunkLocation(this.x shr 4, this.z shr 4, this.world)
}

// ---------------------------------------------------------------------------------------------------------------------

fun BlockState.location(): BlockLocation {
    return BlockLocation(this.x, this.y, this.z, this.world.uid)
}

// ---------------------------------------------------------------------------------------------------------------------

data class ChestShop(
    val owner: UUID?, val sign: BlockLocation, val leftChest: BlockLocation?, val rightChest: BlockLocation?,
    val buyPrice: Int?, val sellPrice: Int?, val amount: Int, val material: Material
) {

    fun sellItemsToShop(feature: ChestShopFeature, player: Player) {
        if (this.sellPrice == null) {
            // this shop doesn't buy from players
            ChestShopDoesNotBuyMsg().sendMessage(feature, player)
            return
        }

        if (!hasRequestedItems(player.inventory)) {
            // player doesn't have enough items to sell
            ChestShopPlayerEmptyMsg(this.amount, this.material).sendMessage(feature, player)
            return
        }

        val hasChest = this.leftChest != null && this.rightChest != null
        val inventory: Inventory? = if (hasChest) getChestInventory(this.leftChest!!, this.rightChest!!) else null

        if (hasChest && inventory == null) {
            // chest is no longer in world? world not loaded?
            feature.destroyShop(this)
            ChestShopNoChestFoundMsg().sendMessage(feature, player)
            return
        }

        if (hasChest && !hasEnoughFreeSpace(inventory!!)) {
            // this shop doesn't have enough free space
            ChestShopShopFullMsg(this.amount, this.material).sendMessage(feature, player)
            return
        }

        if (executeTransaction(feature.getEconomyFeature(), player.uniqueId, this.sellPrice, sell = true, refund = false)) {

            // Gave money to player -> take items
            if (!removeItems(player.inventory)) {

                feature.getLogger().error("$this failed to remove items from: ${player.uniqueId}!")

                if (!executeTransaction(feature.getEconomyFeature(), player.uniqueId, this.sellPrice, sell = true, refund = true)) {
                    feature.getLogger().error("$this failed to refund money from: ${player.uniqueId}!")
                }

                ChestShopPlayerEmptyMsg(this.amount, this.material).sendMessage(feature, player)
                return

            }

            if (hasChest && !addItems(inventory!!)) {

                feature.getLogger().error("$this failed to add items to shop from: ${player.uniqueId}!")

                if (!addItems(player.inventory)) {
                    feature.getLogger().error("$this failed to refund items to player: ${player.uniqueId}!")
                }

                if (!executeTransaction(feature.getEconomyFeature(), player.uniqueId, this.sellPrice, sell = true, refund = true)) {
                    feature.getLogger().error("$this failed to refund money from: ${player.uniqueId}!")
                }

                ChestShopShopFullMsg(this.amount, this.material).sendMessage(feature, player)
                return
            }

            feature.getLogger().debug("$this bought items from ${player.uniqueId}!")
            ChestShopSoldMsg(this.amount, this.material, this.sellPrice).sendMessage(feature, player)

        } else {

            // this shop has not enough money
            ChestShopInsolvent(this.sellPrice).sendMessage(feature, player)

        }
    }

    fun buyItemsFromShop(feature: ChestShopFeature, player: Player) {
        if (this.buyPrice == null) {
            // this shop doesn't sell to players
            ChestShopDoesNotSellMsg().sendMessage(feature, player)
            return
        }

        val hasChest = this.leftChest != null && this.rightChest != null
        val inventory: Inventory? = if (hasChest) getChestInventory(this.leftChest!!, this.rightChest!!) else null

        if (hasChest && inventory == null) {
            // chest is no longer in world? world not loaded?
            feature.destroyShop(this)
            ChestShopNoChestFoundMsg().sendMessage(feature, player)
            return
        }

        if (hasChest && !hasRequestedItems(inventory!!)) {
            // this shop doesn't have enough items to sell
            ChestShopShopEmptyShopMsg(this.amount, this.material).sendMessage(feature, player)
            return
        }

        if (!hasEnoughFreeSpace(player.inventory)) {
            // player doesn't have enough free space
            ChestShopPlayerFullMsg(this.amount, this.material).sendMessage(feature, player)
            return
        }

        if (executeTransaction(feature.getEconomyFeature(), player.uniqueId, this.buyPrice, sell = false, refund = false)) {

            // Took money from player -> give items
            if (hasChest && !removeItems(inventory!!)) {

                feature.getLogger().error("$this failed to remove items from shop to: ${player.uniqueId}!")

                if (!executeTransaction(feature.getEconomyFeature(), player.uniqueId, this.buyPrice, sell = false, refund = true)) {
                    feature.getLogger().error("$this failed to refund money to: ${player.uniqueId}!")
                }

                ChestShopShopEmptyShopMsg(this.amount, this.material).sendMessage(feature, player)
                return
            }

            if (!addItems(player.inventory)) {
                feature.getLogger().error("$this failed to add items from shop to: ${player.uniqueId}!")

                if (hasChest && !addItems(inventory!!)) {
                    feature.getLogger().error("$this failed to refund items to shop: ${player.uniqueId}!")
                }

                if (!executeTransaction(feature.getEconomyFeature(), player.uniqueId, this.buyPrice, sell = false, refund = true)) {
                    feature.getLogger().error("$this failed to refund money to: ${player.uniqueId}!")
                }

                ChestShopPlayerFullMsg(this.amount, this.material).sendMessage(feature, player)
                return
            }

            feature.getLogger().debug("$this sold items to ${player.uniqueId}!")
            ChestShopBoughtMsg(this.amount, this.material, this.buyPrice).sendMessage(feature, player)

        } else {

            // player has not enough money
            ChestShopMissingHolyCoins(this.buyPrice).sendMessage(feature, player)

        }
    }

    private fun getChestInventory(leftChest: BlockLocation, rightChest: BlockLocation): DoubleChestInventory? {
        if (leftChest.world != rightChest.world) return null
        val world = Bukkit.getWorld(leftChest.world) ?: return null
        val leftBlock = world.getBlockAt(leftChest.x, leftChest.y, leftChest.z).state as? Chest ?: return null
        val inventory = leftBlock.inventory as? DoubleChestInventory ?: return null
        val rightHolder = inventory.holder?.rightSide as? Chest ?: return null
        if (rightHolder.location() != rightChest) return null
        return inventory
    }

    private fun hasRequestedItems(inventory: Inventory): Boolean {
        return inventory.contains(this.material, this.amount)
    }

    private fun hasEnoughFreeSpace(inventory: Inventory): Boolean {
        var requiredSpace = this.amount
        for (stack in inventory.storageContents) {
            if (stack == null) return true
            if (stack.type == this.material) {
                requiredSpace -= inventory.maxStackSize - stack.amount
                if (requiredSpace <= 0) return true
            }
        }
        return false
    }

    private fun removeItems(inventory: Inventory): Boolean {
        val leftover = inventory.removeItem(ItemStack(this.material, this.amount))
        if (leftover.isEmpty()) return true
        val itemStack = leftover[0] ?: return true
        if (itemStack.amount <= 0) return true
        // revert changes
        inventory.addItem(ItemStack(this.material, this.amount - itemStack.amount))
        return false
    }

    private fun addItems(inventory: Inventory): Boolean {
        val leftover = inventory.addItem(ItemStack(this.material, this.amount))
        if (leftover.isEmpty()) return true
        val itemStack = leftover[0] ?: return true
        if (itemStack.amount <= 0) return true
        // revert changes
        inventory.removeItem(ItemStack(this.material, this.amount - itemStack.amount))
        return false
    }

    private fun executeTransaction(economy: EconomyFeature, player: UUID, amount: Int, sell: Boolean, refund: Boolean): Boolean {
        val pos = BlockLocationConverter.toString(this.sign)
        val sellText = if (refund) "sell_refund" else "sell"
        val buyText = if (refund) "buy_refund" else "buy"

        return if (this.owner == null) {

            // admin shop
            if (sell) {
                // player sold items -> give player money
                economy.executeTransaction(player, if (refund) -amount else amount, "chestshop_admin-$sellText-$pos")
            } else {
                // player bought items -> take money from player
                economy.executeTransaction(player, if (refund) amount else -amount, "chestshop_admin-$buyText-$pos")
            }

        } else {

            // user shop
            if (sell) {
                // player sold items -> money from owner to player,
                economy.executeTransaction(if (refund) player else this.owner, if (refund) this.owner else player, amount, "chestshop-$sellText-$pos")
            } else {
                // player bought items -> money from player to owner,
                economy.executeTransaction(if (refund) this.owner else player, if (refund) player else this.owner, amount, "chestshop-$buyText-$pos")
            }

        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------

object BlockLocationConverter : StoreConverter<BlockLocation> {

    override fun fromString(value: String): BlockLocation {
        val elements = value.split(":")
        if (elements.size != 4) throw IllegalStateException("Invalid BlockLocation!")
        return BlockLocation(elements[0].toInt(), elements[1].toInt(), elements[2].toInt(), UUID.fromString(elements[3]))
    }

    override fun toString(value: BlockLocation): String {
        return "${value.x}:${value.y}:${value.z}:${value.world}"
    }

    override fun fromJson(json: JsonReader): BlockLocation {
        return fromString(json.nextString())
    }

    override fun toJson(json: JsonWriter, value: BlockLocation) {
        json.value(toString(value))
    }

}

// ---------------------------------------------------------------------------------------------------------------------

object ChestShopConverter : _StoreConverter<ChestShop>() {
    override fun fromJson(json: JsonReader): ChestShop {
        return json.nextObject {
            val owner = getNullOrUUID("owner")
            val sign = BlockLocationConverter.fromJson(nextName("sign"))
            val leftChest = nextName("left-chest").nextNullOrT { BlockLocationConverter.fromJson(this) }
            val rightChest = nextName("right-chest").nextNullOrT { BlockLocationConverter.fromJson(this) }
            if ((leftChest == null) != (rightChest == null)) throw IllegalStateException("Either both or no chest must be null!")
            val buyPrice = getNullOrInt("buy-price")
            val sellPrice = getNullOrInt("sell-price")
            if (buyPrice == null && sellPrice == null) throw IllegalStateException("Both buy and sell price are null!")
            val amount = getInt("amount")
            if (amount !in 1..64) throw IllegalStateException("Amount must be between 1 and 64!")
            val material = getEnum<Material>("material")
            ChestShop(owner, sign, leftChest, rightChest, buyPrice, sellPrice, amount, material)
        }
    }

    override fun toJson(json: JsonWriter, value: ChestShop) {
        json.objectValue(value) { shop ->
            setNullOrUUID("owner", shop.owner)
            BlockLocationConverter.toJson(name("sign"), shop.sign)
            name("left-chest").nullOrTValue(shop.leftChest) { BlockLocationConverter.toJson(this, it) }
            name("right-chest").nullOrTValue(shop.rightChest) { BlockLocationConverter.toJson(this, it) }
            setNullOrInt("buy-price", shop.buyPrice)
            setNullOrInt("sell-price", shop.sellPrice)
            setInt("amount", shop.amount)
            setEnum("material", shop.material)
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------