package com.example.doancuoiky.Activity.Order

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun OrderHistoryScreen() {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val user = firebaseAuth.currentUser
    val coroutineScope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(user) {
        if (user != null) {
            val historyRef = database.getReference("users")
                .child(user.uid)
                .child("histories")

            historyRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedOrders = mutableListOf<Order>()
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
                    Toast.makeText(context, "Lỗi khi tải lịch sử đơn hàng", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            })
        } else {
            Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show()
            isLoading = false
        }
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
            Text(
                text = "Lịch sử đơn hàng",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (orders.isEmpty()) {
                    Text("Không có đơn hàng nào trong lịch sử.")
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
                                    Text(text = "Estimated Arrival: Đã giao")
                                    Text(text = "Trạng thái: ${order.status}")
                                    Text(text = "Payment: Đã thanh toán")
                                    Spacer(modifier = Modifier.height(16.dp))
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