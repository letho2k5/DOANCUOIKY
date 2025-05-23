package com.example.doancuoiky.Activity.Admin.Order

import android.content.Intent
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
import com.example.doancuoiky.Activity.Order.Order
import com.example.doancuoiky.Activity.Order.OrderHistoryActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun OrderAdScreen(
    initialFilter: String,
    onHistoryClick: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(initialFilter) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    val user = firebaseAuth.currentUser

    if (user == null) {
        showLoginDialog = true
    } else {
        LaunchedEffect(selectedFilter) {
            val usersRef = database.getReference("users")
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allOrders = mutableListOf<Order>()
                    for (userSnapshot in snapshot.children) {
                        val fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "Unknown"
                        val userId = userSnapshot.key ?: continue
                        val ordersSnapshot = userSnapshot.child("orders")
                        for (orderSnapshot in ordersSnapshot.children) {
                            val order = orderSnapshot.getValue(Order::class.java)
                            if (order != null && (selectedFilter.isEmpty() || order.status == selectedFilter)) {
                                order.id = orderSnapshot.key ?: ""
                                order.userName = fullName
                                order.userId = userId
                                allOrders.add(order)
                            }
                        }
                    }
                    orders = allOrders
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Không thể tải đơn hàng", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        }
    }

    if (showLoginDialog) {
        LoginDialog(
            onDismiss = { showLoginDialog = false },
            onLoginClick = { context.startActivity(Intent(context, LoginActivity::class.java)) },
            onCancelClick = { (context as? android.app.Activity)?.onBackPressed() }
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
                        text = "Tất cả đơn hàng",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lịch sử",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = {
                            val intent = Intent(context, OrderAdHistoryActivity::class.java)
                            context.startActivity(intent)
                        })
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("", "Wait Confirmed", "Shipping", "Received").forEach { filter ->
                        FilterButton(
                            text = if (filter.isEmpty()) "Tất cả" else filter,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (orders.isEmpty()) {
                    Text(
                        text = "Không có đơn hàng nào",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
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
                                                .size(120.dp)
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
                                        )
                                        Column(
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${order.items?.size ?: 0} món • #${order.id}",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(text = "Người đặt: ${order.userName}")
                                        }
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

                                    val buttonText = when (order.status) {
                                        "Wait Confirmed" -> "Confirmed"
                                        "Shipping" -> "Shipped"
                                        "Received" -> "Completed"
                                        else -> "Unknown"
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(
                                            onClick = {
                                                val userId = order.userId
                                                val orderId = order.id
                                                val orderRef = database.getReference("users").child(userId).child("orders").child(orderId)
                                                val updatedOrders = orders.toMutableList()
                                                val orderIndex = updatedOrders.indexOfFirst { it.id == orderId }
                                                if (orderIndex != -1) {
                                                    when (order.status) {
                                                        "Wait Confirmed" -> {
                                                            updatedOrders[orderIndex] = updatedOrders[orderIndex].copy(status = "Shipping")
                                                            orders = updatedOrders
                                                            orderRef.child("status").setValue("Shipping")
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(context, "Đơn hàng đã được xác nhận", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                                                                    updatedOrders[orderIndex] = updatedOrders[orderIndex].copy(status = "Wait Confirmed")
                                                                    orders = updatedOrders
                                                                }
                                                        }
                                                        "Shipping" -> {
                                                            updatedOrders[orderIndex] = updatedOrders[orderIndex].copy(status = "Received")
                                                            orders = updatedOrders
                                                            orderRef.child("status").setValue("Received")
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(context, "Đơn hàng đã được giao", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                                                                    updatedOrders[orderIndex] = updatedOrders[orderIndex].copy(status = "Shipping")
                                                                    orders = updatedOrders
                                                                }
                                                        }
                                                        "Received" -> {
                                                            updatedOrders.removeAt(orderIndex)
                                                            orders = updatedOrders
                                                            orderRef.removeValue()
                                                                .addOnSuccessListener {
                                                                    Toast.makeText(context, "Đơn hàng đã hoàn tất và được xóa", Toast.LENGTH_SHORT).show()
                                                                }
                                                                .addOnFailureListener {
                                                                    Toast.makeText(context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                                                                    updatedOrders.add(orderIndex, updatedOrders[orderIndex].copy(status = "Received"))
                                                                    orders = updatedOrders
                                                                }
                                                        }
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(text = buttonText)
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
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Order Summary
                    Text(
                        text = "Thông tin đơn hàng",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Người đặt: ${order.userName}")
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
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(32.dp)
            .padding(horizontal = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
            Text("Bạn cần đăng nhập để xem đơn hàng.")
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