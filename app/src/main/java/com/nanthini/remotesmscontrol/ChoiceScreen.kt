package com.nanthini.remotesmscontrol

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class ChoiceScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChoiceScreenUI(
                onRemoteSMSClick = {
                    startActivity(Intent(this, RemoteSMSActivity::class.java))
                },
                onIoTClick = {
                    startActivity(Intent(this, IoTCommandScreen::class.java))
                }
            )
        }
    }
}

@Composable
fun ChoiceScreenUI(
    onRemoteSMSClick: () -> Unit,
    onIoTClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onRemoteSMSClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Remote SMS")
            }
            Button(
                onClick = onIoTClick,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("IoT Commands")
            }
        }
    }
}