package com.example.doancuoiky.Activity.Order

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.Auth.LoginActivity
import com.example.doancuoiky.Activity.Profile.EditProfileActivity
import com.example.doancuoiky.Domain.FoodModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun OrderScreen(
    initialFilter: String,
    onHistoryClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val user = firebaseAuth.currentUser
    val coroutineScope = rememberCoroutineScope()

    var showLoginDialog by remember { mutableStateOf(false) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    if (user != null) {
        LaunchedEffect(user.uid) {
            val ordersRef = database.getReference("users").child(user.uid).child("orders")
            ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedOrders = mutableListOf<Order>()
                    Log.d("OrderDebug", "initialFilter = '$initialFilter'")

                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
                            order.id = orderSnapshot.key ?: ""
                            fetchedOrders.add(order)
                        }
                    }
                    orders = fetchedOrders
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Không thể tải đơn hàng", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        }
    } else {
        showLoginDialog = true
    }

    if (showLoginDialog) {
        LoginDialog(
            onDismiss = { showLoginDialog = false },
            onLoginClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
                showLoginDialog = false
            },
            onCancelClick = {
                (context as? android.app.Activity)?.onBackPressed()
            }
        )
    } else {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "History",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = {
                            val intent = Intent(context, OrderHistoryActivity::class.java)
                            context.startActivity(intent)
                        })
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    if (orders.isEmpty()) {
                        Text(text = "No orders found.")
                    } else {
                        LazyColumn {
                            items(orders) { order ->
                                var clickCount by remember { mutableStateOf(0) }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onTap = {
                                                    clickCount++
                                                    if (clickCount == 2) {
                                                        selectedOrder = order
                                                        clickCount = 0
                                                    } else {
                                                        coroutineScope.launch {
                                                            delay(300)
                                                            clickCount = 0
                                                        }
                                                    }
                                                }
                                            )
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val firstItem = order.items?.firstOrNull()
                                            AsyncImage(
                                                model = firstItem?.ImagePath ?: R.drawable.logo,
                                                contentDescription = "Order Item",
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(30.dp))
                                                    .border(2.dp, Color.Gray, RoundedCornerShape(30.dp))
                                            )
                                            Text(
                                                text = "${order.items?.size ?: 0} items • #${order.id}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "Estimated Arrival: Now")
                                        Text(text = "Status: ${order.status}")
                                        Text(
                                            text = "Payment: ${
                                                when (order.paymentMethod) {
                                                    "Cash on Delivery" -> "Chưa thanh toán"
                                                    "Bank Payment" -> "Đã thanh toán"
                                                    else -> "Không xác định"
                                                }
                                            }"
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            val buttonColor = when (order.status) {
                                                "Shipping" -> Color(0xFFFFC107)
                                                "Received" -> Color(0xFFF44336)
                                                else -> Color.Gray
                                            }

                                            Button(
                                                onClick = {
                                                    if (order.status == "Received") {
                                                        val userId = user?.uid ?: return@Button
                                                        val orderRef = database.getReference("users")
                                                            .child(userId)
                                                            .child("orders")
                                                            .child(order.id)
                                                        val historyRef = database.getReference("users")
                                                            .child(userId)
                                                            .child("histories")
                                                            .child(order.id)

                                                        historyRef.setValue(order).addOnSuccessListener {
                                                            orderRef.removeValue()
                                                            orders = orders.filter { it.id != order.id }
                                                            Toast.makeText(context, "Đơn hàng đã chuyển vào lịch sử", Toast.LENGTH_SHORT).show()
                                                        }.addOnFailureListener {
                                                            Toast.makeText(context, "Lỗi khi chuyển vào lịch sử", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(
                                                            context,
                                                            "Chỉ có thể chuyển vào lịch sử khi trạng thái là 'Received'",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(text = order.status ?: "No Status")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Order Details Dialog with Scroll
    selectedOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { selectedOrder = null },
            title = { Text("Chi tiết đơn hàng #${order.id}") },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()) // Enable scrolling
                        .padding(16.dp)
                ) {
                    // Order Summary
                    Text(
                        text = "Thông tin đơn hàng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Địa chỉ: ${order.address}")
                    Text("Phương thức thanh toán: ${order.paymentMethod}")
                    Text(
                        text = "Trạng thái thanh toán: ${
                            when (order.paymentMethod) {
                                "Cash on Delivery" -> "Chưa thanh toán"
                                "Bank Payment" -> "Đã thanh toán"
                                else -> "Không xác định"
                            }
                        }"
                    )
                    Text("Tổng tiền hàng: $${order.total}")
                    Text("Thuế: $${order.tax}")
                    Text("Phí giao hàng: $${order.deliveryFee}")
                    Text("Tổng cộng: $${(order.total ?: 0.0) + (order.tax ?: 0.0) + (order.deliveryFee ?: 0.0)}")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Product List
                    Text(
                        text = "Sản phẩm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    order.items?.forEach { item ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tên: ${item.Title}")
                        Text("Số lượng: ${item.numberInCart}")
                        Text("Giá: $${item.Price}")
                        Text("Tổng: $${item.Price * item.numberInCart}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedOrder = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun LoginDialog(onDismiss: () -> Unit, onLoginClick: () -> Unit, onCancelClick: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Vui Lòng Đăng Nhập",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text("Bạn cần đăng nhập để xem đơn hàng của mình.")
        },
        confirmButton = {
            TextButton(onClick = onLoginClick) {
                Text("Đăng Nhập")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) {
                Text("Huỷ")
            }
        }
    )
}