package com.example.doancuoiky.Activity.Cart

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.doancuoiky.Activity.Auth.LoginActivity
import com.example.doancuoiky.Helper.ManagmentCart
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.BaseActivity
import com.example.doancuoiky.Activity.Order.Order
import com.example.doancuoiky.Activity.Cart.BankPaymentInfo
import com.example.doancuoiky.Domain.FoodModel
import com.google.firebase.database.FirebaseDatabase

class CartActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartScreen(
                managmentCart = ManagmentCart(this),
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun CartScreen(
    managmentCart: ManagmentCart,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
    val userId = sharedPref.getString("userId", "") ?: ""
    var showLoginPrompt by remember { mutableStateOf(!isLoggedIn) }
    var allowRender by remember { mutableStateOf(isLoggedIn) }

    var cartItems by remember { mutableStateOf(listOf<FoodModel>()) }
    var tax by remember { mutableStateOf(0.0) }
    var selectedTotal by remember { mutableStateOf(0.0) }
    val selectedItems = remember { mutableStateMapOf<String, Boolean>() }
    var address by remember { mutableStateOf("NY-downtown-no97") }
    var paymentMethod by remember { mutableStateOf("Cash on Delivery") }
    var bankPaymentInfo by remember { mutableStateOf<BankPaymentInfo?>(null) }

    // ✅ Nếu chưa đăng nhập, hiển thị hộp thoại yêu cầu đăng nhập
    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Thông báo") },
            text = { Text("Bạn cần đăng nhập để xem giỏ hàng.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLoginPrompt = false
                    allowRender = false
                    onBackClick()
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (!allowRender) return

    // ✅ Load dữ liệu giỏ hàng khi mở
    LaunchedEffect(Unit) {
        managmentCart.getListCart {
            cartItems = it
            it.forEach { item -> selectedItems[item.Title] = true }
            tax = calculateTax(calculateSelectedTotal(it, selectedItems))
            selectedTotal = calculateSelectedTotal(it, selectedItems)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            ConstraintLayout(modifier = Modifier.padding(top = 16.dp)) {
                val (backBtn, cartTxt) = createRefs()
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(cartTxt) { centerTo(parent) },
                    text = "Your Cart",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                Image(
                    painter = painterResource(R.drawable.back_grey),
                    contentDescription = null,
                    modifier = Modifier
                        .constrainAs(backBtn) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .clickable { onBackClick() }
                )
            }
        }

        if (cartItems.isEmpty()) {
            item {
                Text(
                    text = "Cart Is Empty",
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(cartItems) { item ->
                val isChecked = selectedItems[item.Title] ?: true
                CartItem(
                    item = item,
                    isChecked = isChecked,
                    onCheckedChange = {
                        selectedItems[item.Title] = it
                        tax = calculateTax(calculateSelectedTotal(cartItems, selectedItems))
                        selectedTotal = calculateSelectedTotal(cartItems, selectedItems)
                    },
                    managmentCart = managmentCart,
                    onItemChange = {
                        managmentCart.getListCart {
                            cartItems = it
                            tax = calculateTax(calculateSelectedTotal(it, selectedItems))
                            selectedTotal = calculateSelectedTotal(it, selectedItems)
                        }
                    },
                    onDeleteItem = {
                        managmentCart.removeItem(item) {
                            managmentCart.getListCart {
                                cartItems = it
                                tax = calculateTax(calculateSelectedTotal(it, selectedItems))
                                selectedTotal = calculateSelectedTotal(it, selectedItems)
                            }
                        }
                    }
                )
            }

            item {
                Text(
                    text = "Order Summary",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                CartSummary(
                    itemTotal = selectedTotal,
                    tax = tax,
                    delivery = 10.0
                )
            }

            item {
                Text(
                    text = "Information",
                    color = colorResource(R.color.darkPurple),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                DeliveryInfoBox(
                    onAddressChange = { address = it },
                    onPaymentMethodChange = { paymentMethod = it },
                    onBankPaymentInfoChange = { bankPaymentInfo = it },
                    totalAmount = selectedTotal + tax + 10.0,
                    userId = userId,
                    onPayConfirmed = { amount ->
                        // Deduct the amount from the user's balance in Firebase
                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                        userRef.get().addOnSuccessListener { snapshot ->
                            val currentBalance = snapshot.child("balance").getValue(Double::class.java) ?: 0.0
                            val newBalance = currentBalance - amount
                            userRef.child("balance").setValue(newBalance).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Proceed with order placement
                                    val userOrderRef = FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(userId)
                                        .child("orders")

                                    val selectedItemsList = cartItems.filter { selectedItems[it.Title] == true }
                                    val orderId = userOrderRef.push().key ?: return@addOnCompleteListener
                                    userRef.get().addOnSuccessListener { userSnapshot ->
                                        val fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "Unknown"
                                        val order = Order(
                                            id = orderId,
                                            items = selectedItemsList,
                                            total = calculateSelectedTotal(cartItems, selectedItems),
                                            tax = tax,
                                            deliveryFee = 10.0,
                                            status = "Wait Confirmed",
                                            userId = userId,
                                            userName = fullName,
                                            address = address,
                                            paymentMethod = paymentMethod,
                                            bankPaymentInfo = if (paymentMethod == "Bank Payment") bankPaymentInfo else null
                                        )

                                        userOrderRef.child(orderId).setValue(order).addOnCompleteListener { orderTask ->
                                            if (orderTask.isSuccessful) {
                                                Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                                                onBackClick()
                                            } else {
                                                Toast.makeText(context, "Lỗi khi đặt hàng!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, "Lỗi khi cập nhật số dư!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }

            // "Place Order" button only shown if payment method is not Bank Payment
            if (paymentMethod != "Bank Payment") {
                item {
                    Button(
                        onClick = {
                            val userOrderRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .child("orders")

                            val selectedItemsList = cartItems.filter { selectedItems[it.Title] == true }

                            // ✅ Kiểm tra nếu chưa chọn món nào
                            if (selectedItemsList.isEmpty()) {
                                Toast.makeText(context, "Bạn chưa chọn món nào để đặt hàng.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val orderId = userOrderRef.push().key ?: return@Button
                            // Fetch userName from Firebase
                            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                            userRef.get().addOnSuccessListener { snapshot ->
                                val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Unknown"
                                val order = Order(
                                    id = orderId,
                                    items = selectedItemsList,
                                    total = calculateSelectedTotal(cartItems, selectedItems),
                                    tax = tax,
                                    deliveryFee = 10.0,
                                    status = "Wait Confirmed",
                                    userId = userId,
                                    userName = fullName,
                                    address = address,
                                    paymentMethod = paymentMethod,
                                    bankPaymentInfo = if (paymentMethod == "Bank Payment") bankPaymentInfo else null
                                )

                                userOrderRef.child(orderId).setValue(order).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show()
                                        onBackClick()
                                    } else {
                                        Toast.makeText(context, "Lỗi khi đặt hàng!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.darkPurple))
                    ) {
                        Text(
                            text = "Place Order",
                            color = colorResource(R.color.white),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun calculateSelectedTotal(cartItems: List<FoodModel>, selectedItems: Map<String, Boolean>): Double {
    return cartItems.filter { selectedItems[it.Title] == true }
        .sumOf { it.Price * it.numberInCart }
}

fun calculateTax(selectedTotal: Double): Double {
    val percentTax = 0.02
    return Math.round((selectedTotal * percentTax) * 100) / 100.0
}