package com.nanthini.remotesmscontrol

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.BatteryManager
import android.provider.CallLog
import android.provider.Telephony
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.telephony.SmsManager

import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun sendOtp(context: Context) {
        val prefs = context.getSharedPreferences("RemoteSMSPrefs", Context.MODE_PRIVATE)
        val number = prefs.getString("registered_number", null) ?: return
        val otp = AuthManager.generateOtp()

        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(number, null, "🔑 Your OTP is: $otp", null, null)
    }
    fun getBatteryLevel(context: Context): String {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            "🔋 Battery: $batLevel%"
        } catch (e: Exception) {
            "❌ Battery error: ${e.message}"
        }
    }

    fun getLocation(context: Context): String {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val hasFineLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (!hasFineLocation && !hasCoarseLocation) {
                return "❌ Location permission not granted"
            }

            val location = if (hasFineLocation) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }

            location?.let {
                "📍 Location: %.6f, %.6f\n⏱ Last update: %s".format(
                    it.latitude,
                    it.longitude,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(it.time))
                )
            } ?: "📍 Location: Not available (enable GPS)"
        } catch (e: SecurityException) {
            "❌ Location permission denied"
        } catch (e: Exception) {
            "❌ Error getting location: ${e.message}"
        }
    }

    fun getCallLogs(context: Context): String {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {
                return "❌ Call log permission not granted"
            }

            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            ) ?: return "❌ No call logs found"

            val result = StringBuilder("\uD83D\uDCDE Recent calls:\n")
            var count = 0
            cursor.use {
                val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext() && count < 10) {
                    val number = it.getString(numberIdx) ?: "Unknown"
                    val type = when (it.getInt(typeIdx)) {
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        else -> "Unknown"
                    }
                    val date = SimpleDateFormat("MMM dd, HH:mm").format(Date(it.getLong(dateIdx)))
                    val duration = it.getString(durationIdx) ?: "0"

                    result.append("$type: $number\n$date ($duration sec)\n\n")
                    count++
                }
            }
            result.toString()
        } catch (e: Exception) {
            "❌ Call log error: ${e.message}"
        }
    }

    fun getSmsInbox(context: Context): String {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                return "❌ SMS permission not granted"
            }

            val cursor = context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE
                ),
                null,
                null,
                Telephony.Sms.DATE + " DESC"
            ) ?: return "❌ No SMS found"

            val result = StringBuilder("\uD83D\uDCF1 Recent SMS:\n")
            var count = 0
            cursor.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext() && count < 10) {
                    val address = it.getString(addressIdx) ?: "Unknown"
                    val body = it.getString(bodyIdx) ?: ""
                    val date = SimpleDateFormat("MMM dd, HH:mm").format(Date(it.getLong(dateIdx)))

                    result.append("From: $address\n$date\n$body\n\n")
                    count++
                }
            }
            result.toString()
        } catch (e: Exception) {
            "❌ SMS error: ${e.message}"
        }
    }
}
