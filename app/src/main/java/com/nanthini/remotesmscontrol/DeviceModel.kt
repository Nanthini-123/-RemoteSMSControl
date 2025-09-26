package com.nanthini.remotesmscontrol

data class Device(val name: String, var isOn: Boolean)

data class Room(val name: String, val devices: MutableList<Device>)