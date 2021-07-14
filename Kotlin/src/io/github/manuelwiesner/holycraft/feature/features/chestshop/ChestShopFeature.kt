package io.github.manuelwiesner.holycraft.feature.features.chestshop

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.feature.features.economy.EconomyFeature
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import io.github.manuelwiesner.holycraft.feature.features.property.impl.PropertyFeature
import io.github.manuelwiesner.holycraft.feature.getFeature
import io.github.manuelwiesner.holycraft.feature.msg.ChestShopCanNotUseYourselfMsg
import io.github.manuelwiesner.holycraft.feature.msg.ChestShopDestroyedMsg
import io.github.manuelwiesner.holycraft.store.Store
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class ChestShopFeature(manager: _FeatureManager) : FeatureBase<Pair<EconomyFeature, PropertyFeature>>(manager, "CHESTSHOP",
    { manager.getFeature<EconomyFeature>()!! to manager.getFeature()!! }) {

    /**
     * We are using thread safe maps anyway, but we don't want any data loss while removing from multiple maps
     * -> simplest solution, encapsulate everything in a only-write-block lock
     */
    private val readWriteLock: ReentrantReadWriteLock = ReentrantReadWriteLock(true)

    private val chestShops: Store<BlockLocation, ChestShop> = getHolyCraft().getStoreManager()
        .getStore("chestshop", BlockLocationConverter, ChestShopConverter)

    init {
        this.children += ChestShopListener(this)
    }

    fun getEconomyFeature(): EconomyFeature {
        return getItem().first
    }

    fun getPropertyFeature(): PropertyFeature {
        return getItem().second
    }

    fun hasChestShop(chunk: ChunkLocation): Boolean {
        var hasShop = false
        this.readWriteLock.read {
            this.chestShops.forEach { _, shop ->
                val sign = shop.sign.toChunk()
                val left = shop.leftChest?.toChunk()
                val right = shop.rightChest?.toChunk()
                if (sign == chunk || left == chunk || right == chunk) {
                    hasShop = true
                    return@forEach
                }
            }
        }
        return hasShop
    }

    fun checkChestShop(chest: Chest): Boolean {
        var hasShop = false
        var invalid: ChestShop? = null
        val chestLocation = chest.location()
        this.readWriteLock.read {
            this.chestShops.forEach { _, shop ->
                if (shop.leftChest == chestLocation || shop.rightChest == chestLocation) {
                    val sign = chest.world.getBlockAt(shop.sign.x, shop.sign.y, shop.sign.z)

                    if (sign.state !is Sign) {
                        invalid = shop
                    }

                    hasShop = true
                    return@forEach
                }
            }
        }

        val shop = invalid

        if (shop != null) {
            destroyShop(shop)
            val player = shop.owner?.let { Bukkit.getPlayer(it) }
            if (player != null) ChestShopDestroyedMsg(shop.sign).sendMessage(this, player)
        }

        return hasShop
    }

    fun getChestShop(sign: Sign): ChestShop? {
        return this.readWriteLock.read {
            this.chestShops[sign.location()]
        }
    }

    fun onSignClick(sign: Sign, player: Player, leftClick: Boolean) {
        this.readWriteLock.read {
            val chestShop = this.chestShops[sign.location()] ?: return

            if (chestShop.owner == player.uniqueId) {
                ChestShopCanNotUseYourselfMsg().sendMessage(this, player)
                return
            }

            if (leftClick) {
                chestShop.sellItemsToShop(this, player)
            } else {
                chestShop.buyItemsFromShop(this, player)
            }
        }
    }

    fun createShop(chestShop: ChestShop): Boolean {
        this.readWriteLock.write {
            if (chestShop.sign in this.chestShops) return false
            this.chestShops[chestShop.sign] = chestShop
            return true
        }
    }

    fun destroyShop(chestShop: ChestShop) {
        this.readWriteLock.write {
            if (!this.chestShops.raw().remove(chestShop.sign, chestShop)) {
                getLogger().error("ChesShop was not correctly removed: $chestShop")
            }
        }
    }
}