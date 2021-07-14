package io.github.manuelwiesner.holycraft.feature.msg

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature.features.chestshop.BlockLocation
import io.github.manuelwiesner.holycraft.feature.features.property.impl.ChunkLocation
import org.bukkit.*

import org.bukkit.command.CommandSender
import java.util.*

/**
 * Represents a message, being sent to a player. key is a translation key for Lang and args are the formatting arguments.
 */
open class Message protected constructor(val key: String, vararg val args: Any) {
    /**
     * Sends a message to the specified receiver. The translation key is translated into the receivers language,
     * formatted and sent.
     */
    fun sendMessage(feature: FeatureBase<*>, receiver: CommandSender) {
        feature.getLogger().trace("Sending message ${this.key} to ${receiver.name}")
        feature.getHolyCraft().getLangManager().getLanguage(receiver).sendTranslation(this.key, this.args, receiver)
    }
}

/**
 * Joins the content of an Iterable to a string, excluding null and blank elements as well as formatting the list with
 * color codes.
 */
fun <T> Iterable<T>.fancyJoin(transformer: (T) -> String? = { it.toString() }): String {
    return this.map(transformer).filter { it?.isNotBlank() ?: false }.joinToString("§7,§a ", "§a")
}

/**
 * Returns the name of an offline player, but it can sometimes be null, then the UUID as a String is returned.
 */
fun OfflinePlayer.nameOrId(): String {
    return this.name ?: this.uniqueId.toString()
}

/**
 * Prefixes a true value green and a false value red.
 */
fun Boolean.prefix(): String {
    return if (this) "§atrue" else "§cfalse"
}

/**
 * Lists all values of a block location for use with the spread operator.
 */
fun BlockLocation.values(): Array<Any> {
    return arrayOf(this.x, this.y, this.z, Bukkit.getWorld(this.world)?.name ?: "unknown")
}

/**
 * Lists all values of a chunk location for use with the spread operator.
 */
fun ChunkLocation.values(): Array<Any> {
    return arrayOf(this.chunkX, this.chunkZ, Bukkit.getWorld(this.world)?.name ?: "unknown")
}

// ---------------------------------------------------------------------------------------------------------------------
// ---------------------------------------------------------------------------------------------------------------------

/*
 * All messages are collected here, if there is a new feature with new messages they will be added here.
 * This makes it easier to overview all the possible messages for translating languages as well as ensuring the same
 * arguments are passed each time.
 *
 * Regex for getting all keys: 'CmdMessage\("([a-z.-]+)"\)'
 * Regex for getting all keys with arguments: 'CmdMessage\("([a-z.-]+)", ([[a-zA-Z.]+, ]+)'
 */

// ---------------------------------------------------------------------------------------------------------------------
// WarpFeature commands
// ---------------------------------------------------------------------------------------------------------------------

class NHomeNotSetCmdMsg(name: String) : Message("cmd.nhome.not-set", name)

class NHomeTpNotAllowedCmdMsg(playerWorld: String, allowedWorlds: String) : Message("cmd.nhome.tp.not-allowed", playerWorld, allowedWorlds)

class NHomeTpCmdMsg(name: String, location: Location) : Message("cmd.nhome.tp", name, location.blockX, location.blockY, location.blockZ)

class NHomeSetNotAllowedCmdMsg(playerWorld: String, allowedWorld: String) : Message("cmd.nhome.set.not-allowed", playerWorld, allowedWorld)

class NHomeAlreadySetCmdMsg(name: String) : Message("cmd.nhome.already-set", name)

class NHomeSetCmdMsg(name: String, location: Location) : Message("cmd.nhome.set", name, location.blockX, location.blockY, location.blockZ)

class NHomeRemoveCmdMsg(name: String, location: Location) : Message("cmd.nhome.remove", name, location.blockX, location.blockY, location.blockZ)

class NHomeAdminNotSetCmdMsg(playerName: String, homeName: String) : Message("cmd.nhome.admin.not-set", playerName, homeName)

class NHomeAdminNoHomesCmdMsg(playerName: String) : Message("cmd.nhome.admin.no-homes", playerName)

class NHomeAdminListCmdMsg(playerName: String, homeNames: String) : Message("cmd.nhome.admin.list", playerName, homeNames)

class NHomeAdminTpCmdMsg(playerName: String, homeName: String, location: Location) :
    Message("cmd.nhome.admin.tp", playerName, homeName, location.blockX, location.blockY, location.blockZ)

class NHomeAdminAlreadySetCmdMsg(playerName: String, homeName: String) : Message("cmd.nhome.admin.already-set", playerName, homeName)

class NHomeAdminSetCmdMsg(playerName: String, homeName: String, location: Location) :
    Message("cmd.nhome.admin.set", playerName, homeName, location.blockX, location.blockY, location.blockZ)

class NHomeAdminRemoveCmdMsg(playerName: String, homeName: String, location: Location) :
    Message("cmd.nhome.admin.remove", playerName, homeName, location.blockX, location.blockY, location.blockZ)

// ---------------------------------------------------------------------------------------------------------------------

class HomeNotSetCmdMsg
    : Message("cmd.home.not-set")

class HomeTpNotAllowedCmdMsg(playerWorld: String, allowedWorlds: String) : Message("cmd.home.tp.not-allowed", playerWorld, allowedWorlds)

class HomeTpCmdMsg(location: Location) : Message("cmd.home.tp", location.blockX, location.blockY, location.blockZ)

class HomeSetNotAllowedCmdMsg(playerWorld: String, allowedWorlds: String) : Message("cmd.home.set.not-allowed", playerWorld, allowedWorlds)

class HomeAlreadySetCmdMsg
    : Message("cmd.home.already-set")

class HomeSetCmdMsg(location: Location) : Message("cmd.home.set", location.blockX, location.blockY, location.blockZ)

class HomeRemoveCmdMsg(location: Location) : Message("cmd.home.remove", location.blockX, location.blockY, location.blockZ)

// ---------------------------------------------------------------------------------------------------------------------

class WarpListCmdMsg(warpList: String) : Message("cmd.warp.list", warpList)

class WarpNotSetCmdMsg(name: String) : Message("cmd.warp.not-set", name)

class WarpTpNotAllowedCmdMsg(playerWorld: String, allowedWorlds: String) : Message("cmd.warp.tp.not-allowed", playerWorld, allowedWorlds)

class WarpInfoCmdMsg(name: String, location: Location) : Message("cmd.warp.info", name, location.blockX, location.blockY, location.blockZ)

class WarpTpCmdMsg(name: String, location: Location) : Message("cmd.warp.tp", name, location.blockX, location.blockY, location.blockZ)

class WarpAlreadySetCmdMsg(name: String) : Message("cmd.warp.already-set", name)

class WarpSetCmdMsg(name: String, newLocation: Location) : Message("cmd.warp.set", name, newLocation.blockX, newLocation.blockY, newLocation.blockZ)

class WarpRemoveCmdMsg(name: String, oldLocation: Location) :
    Message("cmd.warp.remove", name, oldLocation.blockX, oldLocation.blockY, oldLocation.blockZ)

// ---------------------------------------------------------------------------------------------------------------------
// CmdFeature commands
// ---------------------------------------------------------------------------------------------------------------------

class MessageArgsCmdMsg
    : Message("cmd.message.args")

class MessageYourselfCmdMsg
    : Message("cmd.message.yourself")

class MessageReceived(sender: String, message: String) : Message("cmd.message.received", sender, message)

class MessageSent(receiver: String, message: String) : Message("cmd.message.sent", receiver, message)

// ---------------------------------------------------------------------------------------------------------------------

class RNoMsgCmdMsg
    : Message("cmd.r.no-msg")

class RNoInteractorCmdMsg
    : Message("cmd.r.no-interactor")

// ---------------------------------------------------------------------------------------------------------------------

class LanguageGetCmdMsg(language: String) : Message("cmd.language.get", language)

class LanguageSetCmdMsg(language: String) : Message("cmd.language.set", language)

class LanguageListCmdMsg(languages: String) : Message("cmd.language.list", languages)

// ---------------------------------------------------------------------------------------------------------------------

class ResolveWorldCmdMsg(worldName: String, worldUuid: UUID) : Message("cmd.resolve.world", worldName, worldUuid)

class ResolveNoSuchWorldCmdMsg(worldName: String) : Message("cmd.resolve.no-such-world", worldName)

class ResolvePlayerCmdMsg(player: OfflinePlayer, playerUuid: UUID) : Message("cmd.resolve.player", player.nameOrId(), playerUuid)

// ---------------------------------------------------------------------------------------------------------------------

class HelpPageHeaderCmdMsg(currentPage: Int, numOfPages: Int) : Message("cmd.help.page-header", currentPage, numOfPages)

class HelpPageEntryCmdMsg(cmdName: String, cmdDescr: String) : Message("cmd.help.page-entry", cmdName, cmdDescr)

class HelpPageFooterCmdMsg
    : Message("cmd.help.page-footer")

class HelpForHeaderCmdMsg(name: String) : Message("cmd.help.for-header", name)

class HelpForFooterCmdMsg
    : Message("cmd.help.for-footer")

class HelpForDescrCmdMsg(descr: String) : Message("cmd.help.for-descr", descr)

class HelpForUsageCmdMsg(usage: String) : Message("cmd.help.for-usage", usage)

class HelpForAliasesCmdMsg(aliases: String) : Message("cmd.help.for-aliases", aliases)

// ---------------------------------------------------------------------------------------------------------------------

class SaveSuccessCmdMsg
    : Message("cmd.save.success")

class SaveFailureCmdMsg(simpleName: String, message: String) : Message("cmd.save.failure", simpleName, message)

// ---------------------------------------------------------------------------------------------------------------------
// EconomyFeature commands
// ---------------------------------------------------------------------------------------------------------------------

class HolycoinSelfTransferCmdMsg
    : Message("cmd.holycoin.self-transfer")

class HolycoinTransferredCmdMsg(receiver: OfflinePlayer, newBalance: Int, transferredAmount: Int) :
    Message("cmd.holycoin.transferred", receiver.nameOrId(), newBalance, transferredAmount)

class HolycoinMissingFundsCmdMsg(currentBalance: Int, requestedAmount: Int) : Message("cmd.holycoin.missing-funds", currentBalance, requestedAmount)

class HolycoinReceivedCmdMsg(sender: OfflinePlayer, newBalance: Int, transferredAmount: Int) :
    Message("cmd.holycoin.received", sender.nameOrId(), newBalance, transferredAmount)

class HolycoinBalanceCmdMsg(currentBalance: Int) : Message("cmd.holycoin.balance", currentBalance)

class HolycoinAdjustedCmdMsg(newBalance: Int, amount: Int) :
    Message("cmd.holycoin.adjusted", newBalance, if (amount < 0) "§c$amount" else "§a$amount")

class HolycoinAdminSamePlayerCmdMsg
    : Message("cmd.holycoin.admin.same-player")

class HolycoinAdminTransferredCmdMsg(sender: OfflinePlayer, receiver: OfflinePlayer, senderBalance: Int, receiverBalance: Int, amount: Int) :
    Message("cmd.holycoin.admin.transferred", sender.nameOrId(), receiver.nameOrId(), senderBalance, receiverBalance, amount)

class HolycoinAdminMissingFundsCmdMsg(sender: OfflinePlayer, currentBalance: Int, requestedAmount: Int) :
    Message("cmd.holycoin.admin.missing-funds", sender.nameOrId(), currentBalance, requestedAmount)

class HolycoinAdminBalanceCmdMsg(player: OfflinePlayer, balance: Int) : Message("cmd.holycoin.admin.balance", player.nameOrId(), balance)

class HolycoinAdminAdjustCmdMsg(player: OfflinePlayer, balance: Int, amount: Int) :
    Message("cmd.holycoin.admin.adjust", player.nameOrId(), balance, if (amount < 0) "§c$amount" else "§a$amount")

// ---------------------------------------------------------------------------------------------------------------------
// TablistFeature commands
// ---------------------------------------------------------------------------------------------------------------------

class DeathCounterSetCmdMsg(player: OfflinePlayer, oldValue: Int, newValue: Int) :
    Message("cmd.deathcounter.set", player.nameOrId(), oldValue, newValue)

class DeathCounterAdjustedCmdMsg(player: OfflinePlayer, adjustDelta: Int, newValue: Int) :
    Message("cmd.deathcounter.adjusted", player.nameOrId(), adjustDelta, newValue)

class DeathCounterResetCmdMsg
    : Message("cmd.deathcounter.reset-all")

// ---------------------------------------------------------------------------------------------------------------------

class ClanTagTooLongCmdMsg(value: String, maxLength: Int) : Message("cmd.clantag.long", value, maxLength)

class ClanTagSetCmdMsg(value: String) : Message("cmd.clantag.set", value)

class ClanTagAdminSetCmdMsg(player: OfflinePlayer, value: String) : Message("cmd.clantag.admin-set", player.nameOrId(), value)

class ClanTagNotSetCmdMsg
    : Message("cmd.clantag.not-set")

class ClanTagRemoveCmdMsg(oldValue: String) : Message("cmd.clantag.remove", oldValue)

// ---------------------------------------------------------------------------------------------------------------------
// PropertyFeature commands / messages
// ---------------------------------------------------------------------------------------------------------------------

class PropertyMissingFundsCmdMsg(chunk: ChunkLocation, price: Int) : Message("cmd.property.missing-funds", *chunk.values(), price)

class PropertyAlreadyOwnedCmdMsg(chunk: ChunkLocation) : Message("cmd.property.already-owned", *chunk.values())

class PropertyBoughtCmdMsg(chunk: ChunkLocation, price: Int) : Message("cmd.property.bought", *chunk.values(), price)

class PropertyWrongEnvironmentCmdMsg(environment: World.Environment) : Message("cmd.property.wrong-environment", environment)

class PropertyUnownedCmdMsg(chunk: ChunkLocation, price: Int) : Message("cmd.property.unowned", *chunk.values(), price)

class PropertyNotYoursCmdMsg(chunk: ChunkLocation) : Message("cmd.property.not-yours", *chunk.values())

class PropertySoldCmdMsg(chunk: ChunkLocation, price: Int) : Message("cmd.property.sold", *chunk.values(), price)

class PropertyCanNotBuyCmdMsg(chunk: ChunkLocation) : Message("cmd.property.can-not-buy", *chunk.values())

class PropertyCanNotSellCmdMsg(chunk: ChunkLocation) : Message("cmd.property.can-not-sell", *chunk.values())

class PropertyInfoHeaderCmdMsg(chunk: ChunkLocation, owner: OfflinePlayer) : Message("cmd.property.info.header", *chunk.values(), owner.nameOrId())

class PropertyInfoValuesCmdMsg(
    building: Boolean, blockInteracting: Boolean, entityInteracting: Boolean,
    chestInteracting: Boolean, exploding: Boolean, causality: Boolean, pvp: Boolean
) : Message(
    "cmd.property.info.values", building.prefix(), blockInteracting.prefix(),
    entityInteracting.prefix(), chestInteracting.prefix(), exploding.prefix(), causality.prefix(), pvp.prefix()
)

class PropertyInfoGrantedPlayersCmdMsg(grantType: String, players: String) : Message("cmd.property.info.granted-players", grantType, players)

class PropertyInfoRefundCmdMsg(paidAmount: Int, refundAmount: Int) : Message("cmd.property.info.refund", paidAmount, refundAmount)

class PropertyInfoFooterCmdMsg : Message("cmd.property.info.footer")

class PropertyListHeaderCmdMsg(numOwned: Int) : Message("cmd.property.list.header", numOwned)

class PropertyListEntryCmdMsg(chunk: ChunkLocation, pricePaid: Int) : Message("cmd.property.list.entry", *chunk.values(), pricePaid)

class PropertyListFooterCmdMsg : Message("cmd.property.list.footer")

class PropertyNoPropertiesOwned : Message("cmd.property.no-properties")

class PropertyAllowAllCmdMsg(allowType: String) : Message("cmd.property.allow-all", allowType)

class PropertyDisallowAllCmdMsg(allowType: String) : Message("cmd.property.disallow-all", allowType)

class PropertyAllowCmdMsg(chunk: ChunkLocation, allowType: String) : Message("cmd.property.allow", *chunk.values(), allowType)

class PropertyDisallowCmdMsg(chunk: ChunkLocation, allowType: String) : Message("cmd.property.disallow", *chunk.values(), allowType)

class PropertySelfGrantCmdMsg : Message("cmd.property.self-grant")

class PropertyGrantAllCmdMsg(grantee: OfflinePlayer, numGranted: Int, numAlreadyGranted: Int, grantType: String) :
    Message("cmd.property.grant-all", grantee.nameOrId(), numGranted, numAlreadyGranted, grantType)

class PropertyRevokeAllCmdMsg(revokee: OfflinePlayer, numRevoked: Int, numAlreadyRevoked: Int, grantType: String) :
    Message("cmd.property.revoke-all", revokee.nameOrId(), numRevoked, numAlreadyRevoked, grantType)

class PropertyAlreadyGrantedCmdMsg(chunk: ChunkLocation, grantee: OfflinePlayer, grantType: String) :
    Message("cmd.property.already-granted", *chunk.values(), grantee.nameOrId(), grantType)

class PropertyGrantCmdMsg(chunk: ChunkLocation, grantee: OfflinePlayer, grantType: String) :
    Message("cmd.property.grant", *chunk.values(), grantee.nameOrId(), grantType)

class PropertyAlreadyRevokedCmdMsg(chunk: ChunkLocation, revokee: OfflinePlayer, grantType: String) :
    Message("cmd.property.already-revoked", *chunk.values(), revokee.nameOrId(), grantType)

class PropertyRevokeCmdMsg(chunk: ChunkLocation, revokee: OfflinePlayer, grantType: String) :
    Message("cmd.property.revoke", *chunk.values(), revokee.nameOrId(), grantType)

class PropertyAdminMissingFundsCmdMsg(chunk: ChunkLocation, player: OfflinePlayer, price: Int) :
    Message("cmd.property.admin.missing-funds", *chunk.values(), player.nameOrId(), price)

class PropertyAdminBoughtCmdMsg(chunk: ChunkLocation, player: OfflinePlayer, price: Int) :
    Message("cmd.property.admin.bought", *chunk.values(), player.nameOrId(), price)

class PropertyAdminSoldCmdMsg(chunk: ChunkLocation, player: OfflinePlayer, price: Int) :
    Message("cmd.property.admin.sold", *chunk.values(), player.nameOrId(), price)

class PropertyAdminListHeaderCmdMsg(player: OfflinePlayer, amount: Int) : Message("cmd.property.admin.list", player.nameOrId(), amount)

class PropertyAdminSpawnClaimCmdMsg(newlyClaimed: Int, alreadyClaimed: Int, removed: Int) :
    Message("cmd.property.admin.spawn-claim", newlyClaimed, alreadyClaimed, removed)

// ---------------------------------------------------------------------------------------------------------------------

class PropertyForbiddenBlockInteractMsg(type: String) : Message("property.forbidden.block-interact", type)

class PropertyForbiddenBuildMsg(type: String) : Message("property.forbidden.build", type)

class PropertyForbiddenChestInteractMsg(type: String) : Message("property.forbidden.chest-interact", type)

class PropertyForbiddenEntityInteractMsg(type: String) : Message("property.forbidden.entity-interact", type)

class PropertyForbiddenPvPMsg(type: String) : Message("property.forbidden.pvp", type)

class PropertyInfoDoubleChestMsg : Message("property.info.double-chest")

// ---------------------------------------------------------------------------------------------------------------------
// ChestShopFeature messages
// ---------------------------------------------------------------------------------------------------------------------

class ChestShopExplanationMsg(errorInLine: Int) : Message("chestshop.explanation", errorInLine)

class ChestShopRemoveBeforeSellingPropertyMsg : Message("chestshop.remove-all-shops-from-property")

class ChestShopPartOfShopMsg : Message("chestshop.part-of-shop")

class ChestShopDestroyedMsg(block: BlockLocation) : Message("chestshop.destroyed", *block.values())

class ChestShopAdminDestroyedMsg(player: OfflinePlayer?, block: BlockLocation) :
    Message("chestshop.admin-destroyed", player?.nameOrId() ?: "#ADMINSHOP", *block.values())

class ChestShopNotYourShopMsg : Message("chestshop.not-your-shop")

class ChestShopNotYourPropertyMsg(chunk: ChunkLocation) : Message("chestshop.not-your-property", *chunk.values())

class ChestShopMultipleChestsMsg : Message("chestshop.multiple-chests")

class ChestShopNoChestFoundMsg : Message("chestshop.no-chest-found")

class ChestShopOnlyDoubleChestMsg : Message("chestshop.only-double-chest")

class ChestShopAlreadyPartOfShopMsg : Message("chestshop.already-part-of-shop")

class ChestShopCanNotUseYourselfMsg : Message("chestshop.can-not-use-yourself")

class ChestShopCreatedMsg(block: BlockLocation, shopOwner: String, buyPrice: Int?, sellPrice: Int?, amount: Int, material: Material) :
    Message("chestshop.created", *block.values(), shopOwner, buyPrice ?: "NONE", sellPrice ?: "NONE", amount, material)

class ChestShopDoesNotBuyMsg : Message("chestshop.no-buying-from-player")

class ChestShopDoesNotSellMsg : Message("chestshop.no-selling-to-player")

class ChestShopShopEmptyShopMsg(amount: Int, material: Material) : Message("chestshop.shop-empty", amount, material)

class ChestShopShopFullMsg(amount: Int, material: Material) : Message("chestshop.shop-full", amount, material)

class ChestShopPlayerEmptyMsg(amount: Int, material: Material) : Message("chestshop.player-empty", amount, material)

class ChestShopPlayerFullMsg(amount: Int, material: Material) : Message("chestshop.player-full", amount, material)

class ChestShopInsolvent(coins: Int) : Message("chestshop.insolvent", coins)

class ChestShopMissingHolyCoins(coins: Int) : Message("chestshop.missing-holycoins", coins)

class ChestShopSoldMsg(amount: Int, material: Material, coins: Int) : Message("chestshop.sold", amount, material, coins)

class ChestShopBoughtMsg(amount: Int, material: Material, coins: Int) : Message("chestshop.bought", amount, material, coins)