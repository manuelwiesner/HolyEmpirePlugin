package io.github.manuelwiesner.holycraft.feature.features.chestshop

import io.github.manuelwiesner.holycraft.feature.event.ListenerBase
import io.github.manuelwiesner.holycraft.feature.features.property.PropertySellEvent
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import io.github.manuelwiesner.holycraft.feature.features.property.impl.location
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*
import org.bukkit.event.block.BlockPhysicsEvent


class ChestShopListener(feature: ChestShopFeature) : ListenerBase<ChestShopFeature>(feature) {

    companion object {
        private val SIGN_BLOCK_FACES = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPropertySellEvent(event: PropertySellEvent) {
        if (this.feature.hasChestShop(event.property.getChunkLocation())) {
            event.isCancelled = true
            ChestShopRemoveBeforeSellingPropertyMsg().sendMessage(this.feature, event.player)
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onSignClick(event: PlayerInteractEvent) {
        if (event.action != Action.LEFT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_BLOCK) return
        val blockState = event.clickedBlock?.state
        if (blockState is Sign) this.feature.onSignClick(blockState, event.player, event.action == Action.LEFT_CLICK_BLOCK)
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityExplodeEvent(event: EntityExplodeEvent) {
        event.blockList().removeAll { it.state is Chest || it.state is Sign }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onChestShopBreakEvent(event: BlockBreakEvent) {
        val blockState = event.block.state

        if (blockState is Chest) {
            if (this.feature.checkChestShop(blockState)) {
                event.isCancelled = true
                ChestShopPartOfShopMsg().sendMessage(this.feature, event.player)
            }
            return
        }

        if (blockState is Sign) {
            val chestShop = this.feature.getChestShop(blockState) ?: return

            when {
                chestShop.owner == event.player.uniqueId -> {

                    this.feature.destroyShop(chestShop)
                    ChestShopDestroyedMsg(blockState.location()).sendMessage(this.feature, event.player)

                }
                event.player.isOp && event.player.isSneaking -> {

                    this.feature.destroyShop(chestShop)
                    ChestShopAdminDestroyedMsg(chestShop.owner?.let { Bukkit.getOfflinePlayer(it) }, blockState.location())
                        .sendMessage(this.feature, event.player)

                }
                else -> {

                    event.isCancelled = true
                    ChestShopNotYourShopMsg().sendMessage(this.feature, event.player)

                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onSignPlace(event: SignChangeEvent) {
        // the creator of the shop or null if admin shop
        val shopCreator: UUID? = checkShopPrefixLine(event.getLine(0), event.player).also { if (!it.first) return }.second

        // the buy and sell price respectively the first and second element
        val buySellPrice: Pair<Int?, Int?> = checkPriceLine(event.getLine(1))
            ?: ChestShopExplanationMsg(2).sendMessage(this.feature, event.player).let { event.isCancelled = true; return }

        // the amount of items to use with the specified price
        val amountOfItems: Int = event.getLine(2)?.trim()?.toIntOrNull()?.takeIf { it in 1..64 }
            ?: ChestShopExplanationMsg(3).sendMessage(this.feature, event.player).let { event.isCancelled = true; return }

        // the material of items to use
        val materialOfItems = event.getLine(3)?.trim()?.let { Material.getMaterial(it.toUpperCase().replace(' ', '_')) }
            ?: ChestShopExplanationMsg(4).sendMessage(this.feature, event.player).let { event.isCancelled = true; return }

        // check if sign creator is property owner or op
        if (!checkPropertyOwner(event.player, event.block.chunk.location())) {
            event.isCancelled = true
            return
        }

        // handle admin shop creation
        if (shopCreator == null) {

            // create admin shop
            val sign = event.block.state.location()
            val buyPrice = buySellPrice.first
            val sellPrice = buySellPrice.second
            val chestShop = ChestShop(null, sign, null, null, buyPrice, sellPrice, amountOfItems, materialOfItems)

            if (this.feature.createShop(chestShop)) {
                val name = "#ADMINSHOP"
                event.setLine(0, name)
                event.setLine(3, materialOfItems.name)
                ChestShopCreatedMsg(sign, name, buyPrice, sellPrice, amountOfItems, materialOfItems).sendMessage(this.feature, event.player)
            } else {
                event.isCancelled = true
                ChestShopAlreadyPartOfShopMsg().sendMessage(this.feature, event.player)
            }

        }

        // handle user shop creation
        else {

            // acquire only one chest around sign and check if it's a double chest
            val foundChests: Pair<Chest, Chest>? = findSingleDoubleChest(event.player, event.block)
            if (foundChests == null) {
                event.isCancelled = true
                return
            }

            // create user shop
            val name = Bukkit.getOfflinePlayer(shopCreator).name
            if (name == null) {
                event.player.sendMessage("§cCouldn't get name of player, please contact a dev!")
                event.isCancelled = true
                return
            }
            val sign = event.block.state.location()
            val leftChest = foundChests.first.location()
            val rightChest = foundChests.second.location()
            val buyPrice = buySellPrice.first
            val sellPrice = buySellPrice.second

            val chestShop = ChestShop(shopCreator, sign, leftChest, rightChest, buyPrice, sellPrice, amountOfItems, materialOfItems)

            if (this.feature.createShop(chestShop)) {
                event.setLine(0, name)
                event.setLine(3, materialOfItems.name)
                ChestShopCreatedMsg(sign, name, buyPrice, sellPrice, amountOfItems, materialOfItems).sendMessage(this.feature, event.player)
            } else {
                event.isCancelled = true
                ChestShopAlreadyPartOfShopMsg().sendMessage(this.feature, event.player)
            }

        }
    }

    /**
     * Checks if this is supposed to ba a chest-shop via the prefix line
     * - Users can create a shop with the '#SHOP' prefix
     * - Admin-Shops can be created with the '#ADMINSHOP' prefix
     * - Admins can create shops for other users with '#PLAYER-NAME' prefix e.g. '#MMrMM'
     *
     * If this line is not valid no output will be given -> normal sign with normnal text?
     */
    private fun checkShopPrefixLine(firstLine: String?, player: Player): Pair<Boolean, UUID?> {
        val shopPrefixLine = firstLine?.trim() ?: return false to null
        if (!shopPrefixLine.startsWith('#')) return false to null

        if (shopPrefixLine.equals("#SHOP", true)) {
            return true to player.uniqueId
        }

        if (player.isOp && shopPrefixLine.equals("#ADMINSHOP", true)) {
            return true to null
        }

        return if (player.isOp) {
            val userName = shopPrefixLine.substring(1)
            val user = player.server.offlinePlayers.find { it.name?.equals(userName, true) ?: false }
            return if (user != null) true to user.uniqueId else false to null
        } else false to null
    }

    /**
     * Checks the second line for a buying and or selling price.
     * - B10 -> Buy for 10
     * - S9 -> Sell for 9
     * - B154-S140 -> Buy for 154, Sell for 140
     * - also B100  -  S 100
     *
     * If both buy and sell are given, buy must be left/first
     */
    private fun checkPriceLine(secondLine: String?): Pair<Int?, Int?>? {
        val priceLine = secondLine?.trim()?.split('-') ?: return null

        when (priceLine.size) {
            2 -> {
                val buyPart = priceLine[0].trim()
                val sellPart = priceLine[1].trim()
                if (buyPart.firstOrNull()?.toUpperCase() != 'B' || sellPart.firstOrNull()?.toUpperCase() != 'S') return null
                val buyPrice = buyPart.substring(1).toIntOrNull()?.takeIf { it > 0 } ?: return null
                val sellPrice = sellPart.substring(1).toIntOrNull()?.takeIf { it > 0 } ?: return null
                return buyPrice to sellPrice
            }
            1 -> {
                val type = priceLine[0].firstOrNull() ?: return null
                val price = priceLine[0].substring(1).toIntOrNull()?.takeIf { it > 0 } ?: return null

                return when (type.toUpperCase()) {
                    'B' -> price to null
                    'S' -> null to price
                    else -> null
                }
            }
            else -> return null
        }
    }

    private fun checkPropertyOwner(player: Player, chunk: ChunkLocation): Boolean {
        if (player.isOp) return true

        val property = this.feature.getPropertyFeature().getProperty(chunk)

        return if (property == null || property.getOwner() != player.uniqueId) {
            ChestShopNotYourPropertyMsg(chunk).sendMessage(this.feature, player)
            false
        } else true
    }

    private fun findSingleDoubleChest(player: Player, signBlock: Block): Pair<Chest, Chest>? {
        var foundChest: Chest? = null

        // find single chest
        for (face in SIGN_BLOCK_FACES) {
            val chest = signBlock.getRelative(face).state as? Chest ?: continue

            if (foundChest != null) {
                ChestShopMultipleChestsMsg().sendMessage(this.feature, player)
                return null
            }

            foundChest = chest
        }

        // no chest found
        if (foundChest == null) ChestShopNoChestFoundMsg().sendMessage(this.feature, player).let { return null }

        // check if it's double chest
        val doubleChest = foundChest.inventory.holder as? DoubleChest
        val leftSide = doubleChest?.leftSide as? Chest
        val rightSide = doubleChest?.rightSide as? Chest

        // only single chest
        if (doubleChest == null || leftSide == null || rightSide == null) {
            ChestShopOnlyDoubleChestMsg().sendMessage(this.feature, player)
            return null
        }

        // check if chest is also on owned property
        val signChunk = signBlock.chunk.location()
        val leftSideChunk = leftSide.chunk.location()
        val rightSideChunk = leftSide.chunk.location()
        if (leftSideChunk != signChunk && !checkPropertyOwner(player, leftSideChunk)) return null
        if (rightSideChunk != leftSideChunk && rightSideChunk != signChunk && !checkPropertyOwner(player, rightSideChunk)) return null

        return leftSide to rightSide
    }
}