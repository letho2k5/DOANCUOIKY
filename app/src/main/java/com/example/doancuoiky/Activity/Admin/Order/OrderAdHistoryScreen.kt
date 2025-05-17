package com.example.doancuoiky.Activity.Admin.Order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.doancuoiky.Activity.Order.Order
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun OrderAdHistoryScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(true) {
        val usersRef = database.getReference("users")
        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allOrders = mutableListOf<Order>()
                for (userSnapshot in snapshot.children) {
                    val fullName = userSnapshot.child("fullName").getValue(String::class.java) ?: "Unknown"
                    val userId = userSnapshot.key ?: continue
                    val historiesSnapshot = userSnapshot.child("histories")
                    for (orderSnapshot in historiesSnapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null) {
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
                Toast.makeText(context, "Không thể tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        })
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lịch sử đơn hàng", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (orders.isEmpty()) {
                Text("Không có đơn hàng nào", modifier = Modifier.align(Alignment.CenterHorizontally))
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
                                            .clip(RoundedCornerShape(24.dp))
                                            .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
                                    )
                                    Column {
                                        Text(
                                            text = "${order.items?.size ?: 0} món • #${order.id}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text("Người đặt: ${order.userName}")
                                        Text("Trạng thái: ${order.status}")
                                        Text("Payment: Đã thanh toán")
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
                    Text("Trạng thái thanh toán: Đã thanh toán")
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