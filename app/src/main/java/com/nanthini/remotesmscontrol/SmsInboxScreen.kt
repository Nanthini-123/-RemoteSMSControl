package com.nanthini.remotesmscontrol

import android.content.Context
import android.provider.Telephony
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

data class SMS(val sender: String, val body: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsInboxScreen() {
    val context = LocalContext.current
    val messages = remember { mutableStateListOf<SMS>() }

    LaunchedEffect(Unit) {
        messages.clear()
        messages.addAll(loadSmsInbox(context))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📨 SMS Inbox") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(messages) { sms ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("From: ${sms.sender}", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(sms.date, style = MaterialTheme.typography.labelSmall)
                        Spacer(Modifier.height(8.dp))
                        Text(sms.body)
                    }
                }
            }
        }
    }
}

fun loadSmsInbox(context: Context): List<SMS> {
    val list = mutableListOf<SMS>()
    try {
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
        ) ?: return list

        cursor.use {
            val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext() && list.size < 10) {
                val sender = it.getString(addressIdx) ?: "Unknown"
                val body = it.getString(bodyIdx) ?: ""
                val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                    .format(Date(it.getLong(dateIdx)))
                list.add(SMS(sender, body, date))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}