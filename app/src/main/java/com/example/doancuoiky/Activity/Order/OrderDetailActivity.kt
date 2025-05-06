package com.example.doancuoiky.Activity.Order

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.doancuoiky.Domain.FoodModel

class OrderDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val orderId = intent.getStringExtra("order_id")

        setContent {
            orderId?.let {
                OrderDetailScreen(orderId = it)
            }
        }
    }
}

@Composable
fun OrderDetailScreen(orderId: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val database = FirebaseDatabase.getInstance()
    var order by remember { mutableStateOf<Order?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(orderId) {
        if (user != null) {
            val orderRef = database.getReference("users")
                .child(user.uid)
                .child("orders")
                .child(orderId)

            orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    order = snapshot.getValue(Order::class.java)
                    isLoading = false
                }

                override fun onCancelled(error: DatabaseError) {
                    isLoading = false
                }
            })
        }
    }

    Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            order?.let {
                Column {
                    Text("Mã đơn: ${it.id}", style = MaterialTheme.typography.titleMedium)
                    Text("Trạng thái: ${it.status}")
                    Text("Tổng: ${it.total} VND")
                    Text("Thuế: ${it.tax} VND")
                    Text("Phí giao hàng: ${it.deliveryFee} VND")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Danh sách sản phẩm:", style = MaterialTheme.typography.titleSmall)
                    it.items.forEach { item ->
                        Text("- ${item.Title} x ${item.numberInCart}")
                    }
                }
            } ?: Text("Không tìm thấy đơn hàng.")
        }
    }
}
