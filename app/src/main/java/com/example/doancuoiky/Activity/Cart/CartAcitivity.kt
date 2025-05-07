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
    managmentCart: ManagmentCart = ManagmentCart(LocalContext.current),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
    var showLoginPrompt by remember { mutableStateOf(!isLoggedIn) }
    var allowRender by remember { mutableStateOf(isLoggedIn) }

    var cartItems by remember { mutableStateOf(listOf<FoodModel>()) }
    var tax by remember { mutableStateOf(0.0) }
    val selectedItems = remember { mutableStateMapOf<String, Boolean>() }

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

    LaunchedEffect(Unit) {
        managmentCart.getListCart {
            cartItems = it
            it.forEach { item -> selectedItems[item.Title] = true }
            val selectedTotal = calculateSelectedTotal(it, selectedItems)
            tax = calculateTax(selectedTotal)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
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
                        val selectedTotal = calculateSelectedTotal(cartItems, selectedItems)
                        tax = calculateTax(selectedTotal)
                    },
                    managmentCart = managmentCart,
                    onItemChange = {
                        managmentCart.getListCart {
                            cartItems = it
                            val selectedTotal = calculateSelectedTotal(it, selectedItems)
                            tax = calculateTax(selectedTotal)
                        }
                    },
                    onDeleteItem = {
                        managmentCart.removeItem(item) {
                            managmentCart.getListCart {
                                cartItems = it
                                val selectedTotal = calculateSelectedTotal(it, selectedItems)
                                tax = calculateTax(selectedTotal)
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
                val selectedTotal = calculateSelectedTotal(cartItems, selectedItems)
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
                DeliveryInfoBox()
            }

            item {
                Button(
                    onClick = {
                        val userId = sharedPref.getString("userId", "") ?: return@Button
                        val userOrderRef = FirebaseDatabase.getInstance().getReference("users")
                            .child(userId)
                            .child("orders") // ✅ Sửa tại đây: lưu vào đúng chỗ trong users/{userId}/orders

                        val orderId = userOrderRef.push().key ?: return@Button
                        val selectedItemsList = cartItems.filter { selectedItems[it.Title] == true }

                        val order = Order(
                            id = orderId,
                            items = selectedItemsList,
                            total = calculateSelectedTotal(cartItems, selectedItems),
                            tax = tax,
                            deliveryFee = 10.0,
                            status = "To Pay",
                            userId = userId
                        )

                        // ✅ Ghi đúng vào users/{userId}/orders/{orderId}
                        userOrderRef.child(orderId).setValue(order).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Đặt hàng thành công!", Toast.LENGTH_SHORT)
                                    .show()
                                onBackClick()
                            } else {
                                Toast.makeText(context, "Lỗi khi đặt hàng!", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
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

fun calculateSelectedTotal(cartItems: List<FoodModel>, selectedItems: Map<String, Boolean>): Double {
    return cartItems.filter { selectedItems[it.Title] == true }
        .sumOf { it.Price * it.numberInCart }
}

fun calculateTax(selectedTotal: Double): Double {
    val percentTax = 0.02
    return Math.round((selectedTotal * percentTax) * 100) / 100.0
}
