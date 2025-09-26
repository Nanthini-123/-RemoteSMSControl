package com.nanthini.remotesmscontrol

import androidx.compose.runtime.mutableStateOf

object IoTCommandManager {
    val lightState = mutableStateOf(false)
    val fanState = mutableStateOf(false)

    fun setLightState(on: Boolean) {
        lightState.value = on
    }

}