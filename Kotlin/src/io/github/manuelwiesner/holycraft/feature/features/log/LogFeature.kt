package io.github.manuelwiesner.holycraft.feature.features.log

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager

class LogFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "LOG") {

    init {
        this.children += LogListener(this)
    }
}