package io.github.manuelwiesner.holycraft.feature.features.cmds

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.feature.cmd.CmdBase

class CmdFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "CMD") {

    private val listOfCommands: MutableList<CmdBase<*>> = arrayListOf()

    init {
        this.children += SaveCmd(this)
        this.children += MsgCmd(this)
        this.children += RCmd(this)
        this.children += LangCmd(this)
        this.children += ResolveUUIDCmd(this)
        this.children += CmdListener(this)
        this.children += HelpCmd(this)
    }

    override fun loadFeature() {
        this.listOfCommands.clear()
        getManager().getFeatures().flatMap { it.getFeatureChildren() }.filterIsInstanceTo(this.listOfCommands)
    }

    override fun unloadFeature() {
        this.listOfCommands.clear()
    }

    fun getAllCommands(): List<CmdBase<*>> {
        return this.listOfCommands
    }
}