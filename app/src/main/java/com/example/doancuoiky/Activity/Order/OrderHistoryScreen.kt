package com.example.doancuoiky.Activity.Order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.doancuoiky.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun OrderHistoryScreen() {
    val context = LocalContext.current
    val firebaseAuth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val user = firebaseAuth.currentUser

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
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
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
