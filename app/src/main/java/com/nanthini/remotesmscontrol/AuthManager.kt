package com.nanthini.remotesmscontrol

object AuthManager {
    private var otp: String? = null

    fun generateOtp(): String {
        otp = (100000..999999).random().toString()
        return otp!!
    }

    fun verifyOtp(input: String): Boolean {
        return otp != null && otp == input
    }
}