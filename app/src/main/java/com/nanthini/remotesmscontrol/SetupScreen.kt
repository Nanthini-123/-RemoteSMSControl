package com.nanthini.remotesmscontrol

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class SetupScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PasswordSetupScreen()
            }
        }
    }
}

@Composable
fun PasswordSetupScreen() {
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("What was your first pet’s name?") }
    var securityAnswer by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        Toast.makeText(
            context,
            if (allGranted) "✅ All permissions granted" else "⚠️ Some permissions denied",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Request permissions at startup
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Remote SMS Control Setup", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Set Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Security Question
        OutlinedTextField(
            value = securityQuestion,
            onValueChange = { securityQuestion = it },
            label = { Text("Security Question") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Security Answer
        OutlinedTextField(
            value = securityAnswer,
            onValueChange = { securityAnswer = it },
            label = { Text("Security Answer") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Phone Number
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Registered Phone Number (+91...)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (password.length >= 4 && securityAnswer.isNotEmpty() && phoneNumber.isNotEmpty()) {
                    context.getSharedPreferences("RemoteSMSPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("sms_pass", password)
                        .putString("security_question", securityQuestion)
                        .putString("security_answer", securityAnswer.hashCode().toString()) // hashed
                        .putString("registered_number", phoneNumber)
                        .apply()

                    Toast.makeText(context, "✅ Setup saved!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    intent.putExtra(
                        Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
                        context.packageName
                    )
                    context.startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        "❌ Fill all fields (Password ≥ 4 chars, Answer & Phone required)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save & Set Default App")
        }
    }
}