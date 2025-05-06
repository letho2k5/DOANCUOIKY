package com.example.doancuoiky.Activity.Dashboard

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.Cart.CartActivity
import com.example.doancuoiky.Activity.Order.OrderActivity
import com.example.doancuoiky.Activity.Profile.ProfileActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun MyBottomBar() {
    val database = FirebaseDatabase.getInstance()

    // State để lưu số lượng sản phẩm trong giỏ hàng và đơn hàng
    var cartItemCount by remember { mutableStateOf(0) }
    var orderItemCount by remember { mutableStateOf(0) }

    // Lấy userId từ Firebase Authentication hoặc dùng mặc định
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user_id"
    val cartRef = database.getReference("users/$userId/cart")
    val ordersRef = database.getReference("users/$userId/orders")

    // Lấy context trong ngữ cảnh composable
    val context = LocalContext.current

    // Lấy số lượng sản phẩm trong giỏ hàng từ Firebase Realtime Database
    LaunchedEffect(Unit) {
        // Đăng ký sự kiện thay đổi số lượng giỏ hàng
        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MyBottomBar", "Cart snapshot: $snapshot")
                cartItemCount = snapshot.childrenCount.toInt() // Đếm số lượng item trong cart
                Log.d("MyBottomBar", "Updated cartItemCount: $cartItemCount")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load cart data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("MyBottomBar", "Cart listener cancelled: ${error.message}")
            }
        })

        // Đăng ký sự kiện thay đổi số lượng đơn hàng
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MyBottomBar", "Orders snapshot: $snapshot")
                orderItemCount = snapshot.childrenCount.toInt() // Đếm số lượng đơn hàng
                Log.d("MyBottomBar", "Updated orderItemCount: $orderItemCount")
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load order data: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("MyBottomBar", "Orders listener cancelled: ${error.message}")
            }
        })
    }

    val bottomMenuItemsList = prepareBottommenu()
    var selectedItem by remember { mutableStateOf("Home") }

    BottomAppBar(
        backgroundColor = colorResource(R.color.grey),
        elevation = 3.dp
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            BottomNavigationItem(
                selected = (selectedItem == bottomMenuItem.label),
                onClick = {
                    selectedItem = bottomMenuItem.label
                    when (bottomMenuItem.label) {
                        "Cart" -> {
                            context.startActivity(Intent(context, CartActivity::class.java))
                        }
                        "Order" -> {
                            context.startActivity(Intent(context, OrderActivity::class.java))
                        }
                        "Profile" -> {
                            val intent = Intent(context, ProfileActivity::class.java).apply {
                                putExtra("userName", "Nguyen Van A")
                                putExtra("userEmail", "nguyenvana@example.com")
                            }
                            context.startActivity(intent)
                        }
                        else -> {
                            Toast.makeText(context, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                icon = {
                    Box {
                        Icon(
                            painter = bottomMenuItem.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(20.dp)
                        )
                        if (bottomMenuItem.label == "Cart" && cartItemCount > 0) {
                            CartOrderBadge(count = cartItemCount, modifier = Modifier.align(Alignment.TopEnd))
                        }
                        if (bottomMenuItem.label == "Order" && orderItemCount > 0) {
                            CartOrderBadge(count = orderItemCount, modifier = Modifier.align(Alignment.TopEnd))
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun CartOrderBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(20.dp)
            .background(
                color = Color.Red,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$count",
            color = Color.White,
            style = TextStyle.Default.copy(fontSize = 12.sp),
            modifier = Modifier
        )
    }
}

@Composable
fun prepareBottommenu(): List<BottomMenuItem> {
    val bottomMenuItemsList = arrayListOf<BottomMenuItem>()
    bottomMenuItemsList.add(BottomMenuItem(label = "Home", icon = painterResource(R.drawable.btn_1)))
    bottomMenuItemsList.add(BottomMenuItem(label = "Cart", icon = painterResource(R.drawable.btn_2)))
    bottomMenuItemsList.add(BottomMenuItem(label = "Favorite", icon = painterResource(R.drawable.btn_3)))
    bottomMenuItemsList.add(BottomMenuItem(label = "Order", icon = painterResource(R.drawable.btn_4)))
    bottomMenuItemsList.add(BottomMenuItem(label = "Profile", icon = painterResource(R.drawable.btn_5)))
    return bottomMenuItemsList
}

data class BottomMenuItem(
    val label: String,
    val icon: Painter
)