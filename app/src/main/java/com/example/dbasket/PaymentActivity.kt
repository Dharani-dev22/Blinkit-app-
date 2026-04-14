package com.example.dbasket // Change to your new package name

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Define the different screens. This replaces the old android:visibility logic.
enum class PaymentState {
    OPTIONS, PROCESSING, SUCCESS
}

class PaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the data passed from the product list (this logic remains the same from your original project)
        val amount = intent.getIntExtra("amount", 0)
        val address = intent.getStringExtra("address") ?: ""

        setContent {
            MaterialTheme {
                PaymentScreen(
                    amount = amount,
                    address = address,
                    onPaymentComplete = {
                        // Return home on success.
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentScreen(amount: Int, address: String, onPaymentComplete: () -> Unit) {
    var paymentState by remember { mutableStateOf(PaymentState.OPTIONS) }
    var selectedMethod by remember { mutableStateOf("") }

    // This handles the automatic delay on the processing and success screens.
    LaunchedEffect(paymentState) {
        if (paymentState == PaymentState.PROCESSING) {
            delay(2000)
            paymentState = PaymentState.SUCCESS
            delay(2500)
            onPaymentComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Here we define the three different UIs in one screen based on the state.
        when (paymentState) {
            PaymentState.OPTIONS -> {
                Text(text = "Amount: ₹$amount", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Delivery Address: $address", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        selectedMethod = "UPI"
                        paymentState = PaymentState.PROCESSING
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Pay via UPI")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        selectedMethod = "Card"
                        paymentState = PaymentState.PROCESSING
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Pay via Card")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        selectedMethod = "Cash on Delivery"
                        paymentState = PaymentState.PROCESSING
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Cash on Delivery")
                }
            }
            PaymentState.PROCESSING -> {
                CircularProgressIndicator() // Clean loading spinner
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Paying via $selectedMethod...", style = MaterialTheme.typography.headlineSmall)
            }
            PaymentState.SUCCESS -> {
                Text(
                    text = "Payment Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary // Clean Material success color
                )
            }
        }
    }
}