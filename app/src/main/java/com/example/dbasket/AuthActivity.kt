package com.example.dbasket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("is_logged_in", false)) {
            navigateToMain()
            return
        }

        setContent {
            MaterialTheme {
                AuthScreen(onLoginSuccess = { navigateToMain() })
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }

    var isLoginMode by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Color(0xFF1E3A8A),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "D-Basket",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E3A8A)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = if (isLoginMode) "Welcome Back" else "Join D-Basket",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!isLoginMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (Min 8 characters)") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$".toRegex()

                if (!email.matches(emailRegex)) {
                    Toast.makeText(context, "Invalid Email! Make sure to include @ and .com correctly", Toast.LENGTH_SHORT).show()
                } else if (password.length < 8) {
                    Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                } else {
                    if (isLoginMode) {
                        val savedPassword = sharedPrefs.getString("user_pass_$email", null)
                        if (savedPassword == null) {
                            Toast.makeText(context, "Account does not exist. Please Register first.", Toast.LENGTH_LONG).show()
                        } else if (savedPassword == password) {
                            sharedPrefs.edit().apply {
                                putBoolean("is_logged_in", true)
                                putString("current_user_email", email)
                                apply()
                            }
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Incorrect Password!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        if (name.isNotEmpty()) {
                            sharedPrefs.edit().apply {
                                putString("user_pass_$email", password)
                                putString("user_name_$email", name)
                                putString("current_user_email", email)
                                putBoolean("is_logged_in", true)
                                apply()
                            }
                            Toast.makeText(context, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        } else {
                            Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
        ) {
            Text(if (isLoginMode) "Log In" else "Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            isLoginMode = !isLoginMode
            email = ""
            password = ""
            name = ""
        }) {
            Text(
                text = if (isLoginMode) "New here? Create an account" else "Already have an account? Log In",
                color = Color(0xFF1E3A8A)
            )
        }
    }
}