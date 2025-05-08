package com.example.doancuoiky.Activity.Order

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.Auth.LoginActivity
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.Activity.Order.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

    var showLoginDialog by remember { mutableStateOf(false) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    if (user != null) {
        LaunchedEffect(user.uid, initialFilter) {
            val ordersRef = database.getReference("users").child(user.uid).child("orders")
            ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val fetchedOrders = mutableListOf<Order>()
                    for (orderSnapshot in snapshot.children) {
                        val order = orderSnapshot.getValue(Order::class.java)
                        if (order != null && order.status == initialFilter) {
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
                        modifier = Modifier.clickable(onClick = onHistoryClick)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (initialFilter == "upcoming") {
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
                                                .size(200.dp) // Tăng kích thước ảnh để chiếm ~1/2 khung
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(30.dp)) // Bo tròn góc hơn
                                                .border(2.dp, Color.Gray, RoundedCornerShape(30.dp)) // Viền cho ảnh
                                        )

                                        Text(
                                            text = "${order.items?.size ?: 0} items • #${order.id}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = "Estimated Arrival: Now")
                                    Text(text = "Status: ${order.status}")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(
                                            onClick = { /* Cancel action */ },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Cancel")
                                        }
                                        Button(
                                            onClick = { /* Track action */ },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Track Order")
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                                .size(120.dp) // Tăng kích thước ảnh
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(24.dp)) // Bo tròn góc hơn
                                                .border(2.dp, Color.Gray, RoundedCornerShape(24.dp)) // Viền cho ảnh
                                        )
                                        Text(
                                            text = "${order.items?.size ?: 0} items • $${order.total}",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // Xóa Rate button
                                        Button(
                                            onClick = { onItemClick(order.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(text = "${order.status}")
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