package io.github.manuelwiesner.holycraft.feature.msg

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import org.bukkit.command.CommandSender

/**
 * Represents an error, being sent to a player. key is a translation key for Lang and args are the formatting arguments.
 */
open class MessageException protected constructor(val key: String, vararg val args: Any) : Exception() {
    /**
     * Sends an error to the specified receiver. The translation key is translated into the receivers language,
     * formatted and sent.
     */
    fun sendMessage(feature: FeatureBase<*>, receiver: CommandSender) {
        feature.getLogger().trace("Sending error ${this.key} to ${receiver.name}")
        feature.getHolyCraft().getLangManager().getLanguage(receiver).sendTranslation(this.key, this.args, receiver)
    }
}

/**
 * Simply 'returns' a message without returning it. E.g. in some helper methods certain objects habe to be returned, but
 * also a message with some other condition is true. There for we do not want to throw an error, since it may be
 * 'normal'. This exception treads the passed message as if it would have been returned normally.
 */
class ReturnMessageException(val msg: Message) : Exception() {

    fun sendMessage(feature: FeatureBase<*>, receiver: CommandSender) {
        this.msg.sendMessage(feature, receiver)
    }
}

// ---------------------------------------------------------------------------------------------------------------------

/*
 * All exceptions are collected here, if there is a new feature with new exceptions they will be added here.
 * This makes it easier to overview all the possible messages for translating languages as well as ensuring the same
 * arguments are passed each time.
 *
 * NOTE: err.translation is already used in Message.kt
 *
 * Regex for getting all keys: 'CmdException\("([a-z.-]+)"\)'
 * Regex for getting all keys with arguments: 'CmdException\("([a-z.-]+)", ([[a-zA-Z.]+, ]+)'
 */

// ---------------------------------------------------------------------------------------------------------------------

class UnknownCmdException(label: String) : MessageException("err.cmd.unknown", label)

class UnhandledCmdException(cmdName: String, errName: String, errMsg: String) : MessageException("err.cmd.unhandled", cmdName, errName, errMsg)

class OnlyPlayerException(name: String) : MessageException("err.cmd.only-player", name)

class InvalidArgCmdException(arg: String?, possibilites: String) : MessageException("err.cmd.arg.invalid", arg?.let { " $it" } ?: "", possibilites)

class ArgCountCmdException(name: String, expectedSize: Int) : MessageException("err.cmd.arg.count", name, expectedSize)

class ArgNumberCmdException(arg: String) : MessageException("err.cmd.arg.number", arg)

class ArgNumberRangeCmdException(arg: Number, first: Number, last: Number) : MessageException("err.cmd.arg.number-range", arg, first, last)

class ArgOnlinePlayerCmdException(name: String) : MessageException("err.cmd.arg.online-player", name)

class ArgOfflinePlayerCmdException(name: String) : MessageException("err.cmd.arg.offline-player", name)

// ---------------------------------------------------------------------------------------------------------------------
