package com.example.doancuoiky.Activity.Admin.Order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.doancuoiky.Activity.Order.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun OrderAdHistoryScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
