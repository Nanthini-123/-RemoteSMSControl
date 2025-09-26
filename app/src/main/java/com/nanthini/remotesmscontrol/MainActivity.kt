package com.nanthini.remotesmscontrol

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nanthini.remotesmscontrol.ui.theme.RemoteSMSControlTheme

class MainActivity : ComponentActivity() {

    companion object {
        // Light ON/OFF shared state
        var lightOnState: MutableState<Boolean>? = null

        // Permission request code
        const val REQUEST_CODE_PERMISSIONS = 123
    }

    // Permissions needed
    private val APP_PERMISSIONS = arrayOf(
        android.Manifest.permission.RECEIVE_SMS,
        android.Manifest.permission.SEND_SMS,
        android.Manifest.permission.READ_SMS,
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // Permission launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val denied = permissions.filterValues { !it }.keys
            if (denied.isEmpty()) {
                Toast.makeText(this, "✅ All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ Denied: $denied", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // ✅ Ask for permissions as soon as app launches
        checkAndRequestPermissions()

        setContent {
            RemoteSMSControlTheme {
                val navController = rememberNavController()
                val lightOnState = rememberSaveable { mutableStateOf(false) }
                MainActivity.lightOnState = lightOnState

                NavHost(
                    navController = navController,
                    startDestination = "password"
                ) {
                    composable("password") { PasswordScreen(navController, this@MainActivity) }
                    composable("choice") { ChoiceScreen(navController) }
                    composable("sms_info") { RemoteSMSInfoScreen() }
                    composable("iot_instructions") { IoTInstructionsScreen(navController) }
                    composable("iot_house") {
                        IoTHouseScreenUI(lightOn = lightOnState.value)
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(navController, this@MainActivity)
                    }
                }
            }
        }
    }

    // ✅ Check if all permissions granted
    private fun allPermissionsGranted(): Boolean {
        return APP_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // ✅ Request missing permissions
    private fun checkAndRequestPermissions() {
        val missing = APP_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}
////////////////////////////////////////////////////////
// 📱 Password Screen
////////////////////////////////////////////////////////
/* ---------------- Password + Recovery ---------------- */
@Composable
fun PasswordScreen(navController: NavHostController, context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val savedPassword = prefs.getString("app_password", null)

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var recoveryCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (savedPassword == null) "🔑 Set Up Password" else "Enter App Password",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(16.dp))

        // Case 1: First-time setup
        if (savedPassword == null) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = recoveryCode,
                onValueChange = { recoveryCode = it },
                label = { Text("Set Recovery Code (for reset)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password.isNotBlank() && password == confirmPassword && recoveryCode.isNotBlank()) {
                        prefs.edit()
                            .putString("app_password", password)
                            .putString("recovery_code", recoveryCode)
                            .apply()
                        navController.navigate("choice")
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save & Continue")
            }
        } else {
            // Case 2: Returning user login
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password == savedPassword) {
                        navController.navigate("choice")
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("forgot_password") }) {
                Text("Forgot Password?")
            }
        }

        if (showError) {
            Spacer(Modifier.height(8.dp))
            Text("❌ Please check your entries", color = MaterialTheme.colorScheme.error)
        }
    }
}

////////////////////////////////////////////////////////
// 📱 Forgot Password Screen (Full with Recovery Code)
////////////////////////////////////////////////////////
@Composable
fun ForgotPasswordScreen(navController: NavHostController, context: Context) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val savedRecovery = prefs.getString("recovery_code", null) ?: ""

    var inputCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNew by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔑 Reset Password", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = inputCode,
            onValueChange = { inputCode = it },
            label = { Text("Enter Recovery Code") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmNew,
            onValueChange = { confirmNew = it },
            label = { Text("Confirm New Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (inputCode == savedRecovery && newPassword == confirmNew && newPassword.isNotBlank()) {
                    prefs.edit().putString("app_password", newPassword).apply()
                    showSuccess = true
                    showError = false
                } else {
                    showSuccess = false
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reset Password") }

        if (showError) {
            Spacer(Modifier.height(8.dp))
            Text("❌ Invalid code or mismatch", color = MaterialTheme.colorScheme.error)
        }
        if (showSuccess) {
            Spacer(Modifier.height(8.dp))
            Text("✅ Password updated. Please login again.")
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("password") },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Go to Login") }
        }
    }
}

////////////////////////////////////////////////////////
// 📱 Choice Screen
////////////////////////////////////////////////////////
@Composable
fun ChoiceScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { navController.navigate("sms_info") }, modifier = Modifier.fillMaxWidth()) {
            Text("Remote Mobile Access")
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("iot_instructions") }, modifier = Modifier.fillMaxWidth()) {
            Text("IoT Light Simulation")
        }
    }
}

////////////////////////////////////////////////////////
// 📱 SMS Info Screen
////////////////////////////////////////////////////////
@Composable
fun RemoteSMSInfoScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Use SMS commands: #password #GET_LOGS, #GET_SMS, #GET_BATTERY, #GET_GPS, #LIGHT_ON, #LIGHT_OFF")
    }
}

////////////////////////////////////////////////////////
// 📱 IoT Instructions Screen
////////////////////////////////////////////////////////
@Composable
fun IoTInstructionsScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Send SMS: #password #LIGHT_ON or #LIGHT_OFF to control house lights")
        Spacer(Modifier.height(16.dp))
        Button(onClick = { navController.navigate("iot_house") }) {
            Text("View House Simulation")
        }
    }
}

////////////////////////////////////////////////////////
// 💡 IoT House Simulation
////////////////////////////////////////////////////////
@Composable
fun IoTHouseScreenUI(lightOn: Boolean) {
    // Full screen with black background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // ✅ set background to black
        contentAlignment = Alignment.Center
    ) {
        val imageRes = if (lightOn) R.drawable.light_glow_on213 else R.drawable.light_glow_off213

        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "House Light",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(250.dp) // ✅ fixed size for both ON/OFF
        )
    }
}

////////////////////////////////////////////////////////
// 📱 Simple Forgot Password Screen (renamed to avoid conflicts)
////////////////////////////////////////////////////////
@Composable
fun ForgotPasswordSimpleScreen(navController: NavHostController, context: Context) {
    var newPassword by rememberSaveable { mutableStateOf("") }
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("Enter New Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        )

        Button(onClick = {
            prefs.edit().putString("app_password", newPassword).apply()
            Toast.makeText(context, "Password reset successful!", Toast.LENGTH_SHORT).show()
            navController.navigate("password")
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Reset Password")
        }
    }
}