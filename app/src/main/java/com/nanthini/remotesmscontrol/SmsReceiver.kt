package com.nanthini.remotesmscontrol

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent?.action != "android.provider.Telephony.SMS_RECEIVED") return

        val bundle = intent.extras ?: return
        val pdus = bundle["pdus"] as? Array<*> ?: return

        for (pdu in pdus) {
            val format = bundle.getString("format")
            val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SmsMessage.createFromPdu(pdu as ByteArray, format)
            } else {
                SmsMessage.createFromPdu(pdu as ByteArray)
            }

            val sender = sms.originatingAddress ?: continue
            val message = sms.messageBody.trim()

            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val savedPassword = prefs.getString("app_password", "") ?: ""

            // Expected format: #password #COMMAND
            val parts = message.split(" ", limit = 2)
            if (parts.size < 2) {
                sendSMS(sender, "❌ Format: #password #COMMAND", context)
                continue
            }

            val inputPassword = parts[0].removePrefix("#").trim()
            if (inputPassword != savedPassword) {
                sendSMS(sender, "❌ Incorrect password", context)
                continue
            }

            val command = parts[1].trim().uppercase()

            val reply = try {
                when (command) {
                    // 📱 Mobile Remote Commands
                    "#GET_LOGS", "#GET_CALLLOGS" -> {
                        if (hasPermission(context, Manifest.permission.READ_CALL_LOG)) {
                            Utils.getCallLogs(context)
                        } else "❌ Permission READ_CALL_LOG missing"
                    }
                    "#GET_SMS", "#GET_SMSLOG", "#GET_SMSLOGS" -> {
                        if (hasPermission(context, Manifest.permission.READ_SMS)) {
                            Utils.getSmsInbox(context)
                        } else "❌ Permission READ_SMS missing"
                    }
                    "#GET_BATTERY" -> Utils.getBatteryLevel(context)
                    "#GET_GPS", "#GET_LOCATION" -> {
                        if (hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            Utils.getLocation(context)
                        } else "❌ Permission LOCATION missing"
                    }

                    // 💡 IoT Commands
                    "#LIGHT_ON" -> {
                        MainActivity.lightOnState?.value = true
                        "💡 Light turned ON"
                    }
                    "#LIGHT_OFF" -> {
                        MainActivity.lightOnState?.value = false
                        "💡 Light turned OFF"
                    }

                    else -> "❌ Unknown command: $command"
                }
            } catch (e: Exception) {
                "❌ Error: ${e.localizedMessage}"
            }

            sendSMS(sender, reply, context)
        }
    }

    // ✅ Check runtime permission
    private fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ✅ Send SMS back
    private fun sendSMS(to: String, msg: String, context: Context) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            val parts = smsManager.divideMessage(msg)
            smsManager.sendMultipartTextMessage(to, null, parts, null, null)
        } catch (e: Exception) {
            Log.e("SmsReceiver", "❌ SMS send failed: ${e.message}")
            Toast.makeText(context, "❌ SMS send error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}