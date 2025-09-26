package com.nanthini.remotesmscontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class IoTHouseScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    IoTHouseUI()
                }
            }
        }
    }
}

@Composable
fun IoTHouseUI() {
    var lightOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Base 3D house layout
        Image(
            painter = painterResource(id = R.drawable.house_layout213),
            contentDescription = "3D House Layout",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Light glow overlay
        Image(
            painter = painterResource(
                id = if (lightOn) R.drawable.light_glow_on213 else R.drawable.light_glow_off213
            ),
            contentDescription = if (lightOn) "Light ON" else "Light OFF",
            modifier = Modifier
                .fillMaxSize()
                .clickable { lightOn = !lightOn }
                .zIndex(1f),
            contentScale = ContentScale.Crop
        )

        // Status text at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(
                text = if (lightOn) "Light is ON" else "Light is OFF",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}