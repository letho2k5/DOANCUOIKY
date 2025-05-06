package com.example.doancuoiky.Activity.Order

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                        text = "Mua Sắm Của Tôi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Xem Lịch Sử Mua Hàng",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            val intent = Intent(context, OrderActivity::class.java)
                            intent.putExtra("order_filter", "history")
                            context.startActivity(intent)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OrderItem(R.drawable.ic_to_pay, "Cần Thanh Toán") {
                        val intent = Intent(context, OrderActivity::class.java)
                        intent.putExtra("order_filter", "To Pay")
                        context.startActivity(intent)
                    }
                    OrderItem(R.drawable.ic_to_ship, "Cần Vận Chuyển") {
                        val intent = Intent(context, OrderActivity::class.java)
                        intent.putExtra("order_filter", "To Ship")
                        context.startActivity(intent)
                    }
                    OrderItem(R.drawable.ic_to_receive, "Cần Nhận Hàng") {
                        val intent = Intent(context, OrderActivity::class.java)
                        intent.putExtra("order_filter", "To Receive")
                        context.startActivity(intent)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    if (orders.isNotEmpty()) {
                        OrderList(orders) { orderId ->
                            val intent = Intent(context, OrderDetailActivity::class.java)
                            intent.putExtra("order_id", orderId)
                            context.startActivity(intent)
                        }
                    } else {
                        Text(
                            text = "Không có đơn hàng.",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
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

@Composable
fun OrderItem(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp)
    }
}

@Composable
fun OrderList(orders: List<Order>, onClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(orders) { order ->
            OrderListItem(order, onClick)
        }
    }
}

@Composable
fun OrderListItem(order: Order, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(order.id) }
    ) {
        Text(text = "Đơn hàng ID: ${order.id}", fontWeight = FontWeight.Bold)
        Text(text = "Tổng: ${order.total} VND")
        Text(text = "Trạng thái: ${order.status}")
        Spacer(modifier = Modifier.height(8.dp))
    }
}
