package com.example.doancuoiky.Activity.Cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.Activity.Cart.BankPaymentInfo
import com.example.doancuoiky.R
import com.google.firebase.database.FirebaseDatabase

@Composable
fun DeliveryInfoBox(
    onAddressChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onBankPaymentInfoChange: (BankPaymentInfo?) -> Unit,
    totalAmount: Double,
    userId: String,
    onPayConfirmed: (Double) -> Unit
) {
    val context = LocalContext.current
    var address by remember { mutableStateOf("NY-downtown-no97") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var expanded by remember { mutableStateOf(false) }
    val paymentOptions = listOf("Cash on Delivery", "Bank Payment")

    // Bank payment form fields
    var cardHolderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var balance by remember { mutableStateOf(0.0) }
    var balanceVisible by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Check if all payment fields are filled
    val isPaymentInfoComplete by remember(cardHolderName, cardNumber) {
        mutableStateOf(cardHolderName.isNotBlank() && cardNumber.isNotBlank())
    }

    // Fetch user's balance from Firebase
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                balance = snapshot.child("balance").getValue(Double::class.java) ?: 0.0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(color = colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        // Address Input
        Column {
            Text(text = "Your Delivery Address", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.location),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = address,
                    onValueChange = {
                        address = it
                        onAddressChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Payment Method Dropdown
        Column {
            Text(text = "Payment Method", fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.credit_card),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.grey)
                        )
                    ) {
                        Text(
                            text = paymentMethod,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        paymentOptions.forEach { option ->
                            DropdownMenuItem(onClick = {
                                paymentMethod = option
                                onPaymentMethodChange(option)
                                // Reset bank payment info if switching to Cash on Delivery
                                if (option != "Bank Payment") {
                                    onBankPaymentInfoChange(null)
                                    cardHolderName = ""
                                    cardNumber = ""
                                }
                                expanded = false
                            }) {
                                Text(text = option)
                            }
                        }
                    }
                }
            }
        }

        // Vietcombank Payment Form (shown only if Bank Payment is selected)
        if (paymentMethod == "Bank Payment") {
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Column {
                Text(
                    text = "Vietcombank Payment Details",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Cardholder Name
                TextField(
                    value = cardHolderName,
                    onValueChange = {
                        cardHolderName = it
                        onBankPaymentInfoChange(
                            BankPaymentInfo(
                                cardHolderName = it,
                                cardNumber = cardNumber
                            )
                        )
                    },
                    label = { Text("Cardholder Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Card Number
                TextField(
                    value = cardNumber,
                    onValueChange = {
                        cardNumber = it
                        onBankPaymentInfoChange(
                            BankPaymentInfo(
                                cardHolderName = cardHolderName,
                                cardNumber = it
                            )
                        )
                    },
                    label = { Text("Card Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Balance (displayed like a password field)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = balance.toString(),
                        onValueChange = {},
                        label = { Text("Account Balance") },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        enabled = false, // Read-only field
                        visualTransformation = if (balanceVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (balanceVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (balanceVisible) "Hide balance" else "Show balance",
                        modifier = Modifier.clickable { balanceVisible = !balanceVisible }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pay Button (disabled if payment info is incomplete)
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isPaymentInfoComplete, // Disable if fields are not filled
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPaymentInfoComplete) colorResource(R.color.darkPurple) else Color.Gray
                    )
                ) {
                    Text(
                        text = "Pay",
                        color = colorResource(R.color.white),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Optional: Show a message if the button is disabled
                if (!isPaymentInfoComplete) {
                    Text(
                        text = "Vui lòng điền đầy đủ thông tin thanh toán để tiếp tục.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Confirmation Dialog for Payment
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Xác nhận thanh toán") },
                text = { Text("Bạn có chắc muốn thanh toán số tiền $${totalAmount} không?") },
                confirmButton = {
                    TextButton(onClick = {
                        if (balance < totalAmount) {
                            // Not enough balance, show error dialog
                            showConfirmDialog = false
                            showErrorDialog = true
                        } else {
                            // Proceed with payment
                            onPayConfirmed(totalAmount)
                            showConfirmDialog = false
                        }
                    }) {
                        Text("Có")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Không")
                    }
                }
            )
        }

        // Error Dialog for Insufficient Balance
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("Lỗi") },
                text = { Text("Số dư tài khoản không đủ để thanh toán!") },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}