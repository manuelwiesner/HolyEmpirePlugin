package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.cmd.CmdArg
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseArgs
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBaseRoot
import io.github.manuelwiesner.holycraft.feature.msg.*
import org.bukkit.command.CommandSender

class LangCmd(feature: CmdFeature) : CmdBaseRoot<CmdFeature>(
    feature, "language", LangCmdGet(feature),
    LangCmdSet(feature), LangCmdList(feature)
)

class LangCmdGet(feature: CmdFeature) : CmdBaseArgs<CmdFeature>(feature, "") {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        return LanguageGetCmdMsg(this.feature.getHolyCraft().getLangManager().getLanguage(sender).getLanguageName())
    }
}

class LangCmdSet(feature: CmdFeature) : CmdBaseArgs<CmdFeature>(
    feature, "set",
    CmdArg.CUSTOM({ feature.getHolyCraft().getLangManager().getAvailableLanguages().map { it.toLowerCase() } })
) {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        this.feature.getHolyCraft().getLangManager().setLanguage(sender, args.text(0))
        return LanguageSetCmdMsg(args.text(0))
    }
}

class LangCmdList(feature: CmdFeature) : CmdBaseArgs<CmdFeature>(feature, "list") {

    override fun executeCommand(sender: CommandSender, args: List<Any>): Message {
        return LanguageListCmdMsg(this.feature.getHolyCraft().getLangManager().getAvailableLanguages().map { it.toLowerCase() }.fancyJoin())
    }
}