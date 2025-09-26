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

class IoTCommandScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IoTCommandUI(
                onNextClick = {
                    startActivity(Intent(this, IoTHouseScreen::class.java))
                }
            )
        }
    }
}

@Composable
fun IoTCommandUI(onNextClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("IoT Commands Guide:")
            Spacer(modifier = Modifier.height(8.dp))
            Text("#LIGHT_ON — Turn on the light")
            Text("#LIGHT_OFF — Turn off the light")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNextClick) {
                Text("Go to IoT House")
            }
        }
    }
}