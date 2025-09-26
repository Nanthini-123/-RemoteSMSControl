package com.nanthini.remotesmscontrol

import android.content.Context
import android.provider.CallLog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import java.text.SimpleDateFormat
import java.util.*

data class CallEntry(val number: String, val type: String, val date: String, val duration: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen() {
    val context = LocalContext.current
    val callLogs = remember { mutableStateListOf<CallEntry>() }

    LaunchedEffect(Unit) {
        callLogs.clear()
        callLogs.addAll(loadCallLogs(context))
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("📞 Call Logs") })
    }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(callLogs) { call ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Number: ${call.number}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Type: ${call.type}")
                        Text("Date: ${call.date}")
                        Text("Duration: ${call.duration}")
                    }
                }
            }
        }
    }
}

fun loadCallLogs(context: Context): List<CallEntry> {
    val list = mutableListOf<CallEntry>()

    if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
        != PackageManager.PERMISSION_GRANTED
    ) {
        list.add(CallEntry("❌", "Permission", "Not granted", "0"))
        return list
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
    ) ?: return list

    var count = 0
    cursor.use {
        val numberIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
        val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
        val dateIdx = it.getColumnIndex(CallLog.Calls.DATE)
        val durationIdx = it.getColumnIndex(CallLog.Calls.DURATION)

        while (it.moveToNext() && count++ < 5) {
            val number = it.getString(numberIdx) ?: "Unknown"
            val type = when (it.getInt(typeIdx)) {
                CallLog.Calls.INCOMING_TYPE -> "Incoming"
                CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                CallLog.Calls.MISSED_TYPE -> "Missed"
                else -> "Unknown"
            }
            val date = SimpleDateFormat("MMM dd, HH:mm").format(Date(it.getLong(dateIdx)))
            val duration = "${it.getInt(durationIdx)} sec"
            list.add(CallEntry(number, type, date, duration))
        }
    }

    return list
}