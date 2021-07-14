package io.github.manuelwiesner.holycraft.feature.features.economy


import io.github.manuelwiesner.holycraft.feature.cmd.*
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender

class HolyCoinCmd(feature: EconomyFeature) : CmdBaseRoot<EconomyFeature>(
    feature, "holycoin",
    HolyCoinCmdTransfer(feature), HolyCoinCmdBalance(feature), HolyCoinCmdAdmin(feature)
)

// ---------------------------------------------------------------------------------------------------------------------

class HolyCoinCmdTransfer(feature: EconomyFeature) : CmdBaseArgs<EconomyFeature>(
    feature, "transfer",
    CmdArg.OFFLINE_PLAYER, CmdArg.NUMBER(1..Int.MAX_VALUE)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val senderPlayer = ensurePlayer(sender)
        val receiverPlayer = args.offlinePlayer(0)
        val amount = args.number(1)

        if (receiverPlayer.uniqueId == senderPlayer.uniqueId) return HolycoinSelfTransferCmdMsg()

        return if (this.feature.transferCoins(senderPlayer, receiverPlayer, amount, "transfer")) {
            HolycoinTransferredCmdMsg(receiverPlayer, this.feature.getPlayerBalance(senderPlayer.uniqueId), amount)
        } else {
            HolycoinMissingFundsCmdMsg(this.feature.getPlayerBalance(senderPlayer.uniqueId), amount)
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class HolyCoinCmdBalance(feature: EconomyFeature) : CmdBaseArgs<EconomyFeature>(feature, "balance") {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val amount = this.feature.getPlayerBalance(ensurePlayer(sender).uniqueId)
        return HolycoinBalanceCmdMsg(amount)
    }
}

// ---------------------------------------------------------------------------------------------------------------------

class HolyCoinCmdAdmin(feature: EconomyFeature) : CmdBaseRoot<EconomyFeature>(
    feature, "admin",
    HolyCoinCmdAdminTransfer(feature), HolyCoinCmdAdminInfo(feature), HolyCoinCmdAdminAdjust(feature)
) {
    override fun acceptsSender(sender: CommandSender): Boolean = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class HolyCoinCmdAdminTransfer(feature: EconomyFeature) : CmdBaseArgs<EconomyFeature>(
    feature, "transfer",
    CmdArg.OFFLINE_PLAYER, CmdArg.OFFLINE_PLAYER, CmdArg.NUMBER(1..Int.MAX_VALUE)
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val senderPlayer = args.offlinePlayer(0)
        val receiverPlayer = args.offlinePlayer(1)
        val amount = args.number(2)

        if (receiverPlayer.uniqueId == senderPlayer.uniqueId) return HolycoinAdminSamePlayerCmdMsg()

        return if (this.feature.transferCoins(senderPlayer, receiverPlayer, amount, "admin_transfer")) {
            val senderBalance = this.feature.getPlayerBalance(senderPlayer.uniqueId)
            senderPlayer.player?.let { HolycoinTransferredCmdMsg(receiverPlayer, senderBalance, amount).sendMessage(this.feature, it) }
            HolycoinAdminTransferredCmdMsg(
                senderPlayer,
                receiverPlayer,
                senderBalance,
                this.feature.getPlayerBalance(receiverPlayer.uniqueId),
                amount
            )
        } else {
            HolycoinAdminMissingFundsCmdMsg(senderPlayer, this.feature.getPlayerBalance(senderPlayer.uniqueId), amount)
        }
    }

    override fun acceptsSender(sender: CommandSender): Boolean = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class HolyCoinCmdAdminInfo(feature: EconomyFeature) : CmdBaseArgs<EconomyFeature>(feature, "balance", CmdArg.OFFLINE_PLAYER) {
    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val player = args.offlinePlayer(0)
        return HolycoinAdminBalanceCmdMsg(player, this.feature.getPlayerBalance(player.uniqueId))
    }

    override fun acceptsSender(sender: CommandSender): Boolean = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

class HolyCoinCmdAdminAdjust(feature: EconomyFeature) : CmdBaseArgs<EconomyFeature>(
    feature, "adjust",
    CmdArg.OFFLINE_PLAYER, CmdArg.NUMBER()
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        val executor = args.offlinePlayer(0)
        val amount = args.number(1)

        return if (this.feature.executeTransaction(executor.uniqueId, amount, "admin_adjust")) {
            val executorBalance = this.feature.getPlayerBalance(executor.uniqueId)
            executor.player?.let { HolycoinAdjustedCmdMsg(executorBalance, amount).sendMessage(this.feature, it) }
            HolycoinAdminAdjustCmdMsg(executor, executorBalance, amount)
        } else {
            HolycoinAdminMissingFundsCmdMsg(executor, this.feature.getPlayerBalance(executor.uniqueId), amount)
        }
    }

    override fun acceptsSender(sender: CommandSender): Boolean = sender.isOp
}

// ---------------------------------------------------------------------------------------------------------------------

fun EconomyFeature.transferCoins(sender: OfflinePlayer, receiver: OfflinePlayer, amount: Int, bookingInfo: String): Boolean {
    if (executeTransaction(sender.uniqueId, receiver.uniqueId, amount, bookingInfo)) {
        receiver.player?.let { HolycoinReceivedCmdMsg(sender, getPlayerBalance(receiver.uniqueId), amount).sendMessage(this, it) }
        return true
    }
    return false
}