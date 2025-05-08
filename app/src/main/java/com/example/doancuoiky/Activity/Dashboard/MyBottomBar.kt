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
import com.example.doancuoiky.Activity.Favourite.FavouriteActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun MyBottomBar() {
    val database = FirebaseDatabase.getInstance()

    // State to store cart, order, and favorite item counts
    var cartItemCount by remember { mutableStateOf(0) }
    var orderItemCount by remember { mutableStateOf(0) }
    var favoriteItemCount by remember { mutableStateOf(0) }  // For favorite items count

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_user_id"
    val cartRef = database.getReference("users/$userId/cart")
    val ordersRef = database.getReference("users/$userId/orders")
    val favoriteRef = database.getReference("users/$userId/favourites")  // Reference for favorites

    val context = LocalContext.current

    // Fetch data from Firebase
    LaunchedEffect(Unit) {
        cartRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItemCount = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load cart data", Toast.LENGTH_SHORT).show()
            }
        })

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderItemCount = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load order data", Toast.LENGTH_SHORT).show()
            }
        })

        favoriteRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteItemCount = snapshot.childrenCount.toInt()  // Update favorite item count
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load favorite data", Toast.LENGTH_SHORT).show()
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
                        "Cart" -> context.startActivity(Intent(context, CartActivity::class.java))
                        "Order" -> context.startActivity(Intent(context, OrderActivity::class.java))
                        "Profile" -> context.startActivity(Intent(context, ProfileActivity::class.java))
                        "Favorite" -> context.startActivity(Intent(context, FavouriteActivity::class.java))
                        else -> Toast.makeText(context, bottomMenuItem.label, Toast.LENGTH_SHORT).show()
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
                        // Display Cart badge
                        if (bottomMenuItem.label == "Cart" && cartItemCount > 0) {
                            CartOrderBadge(count = cartItemCount, modifier = Modifier.align(Alignment.TopEnd))
                        }
                        // Display Order badge
                        if (bottomMenuItem.label == "Order" && orderItemCount > 0) {
                            CartOrderBadge(count = orderItemCount, modifier = Modifier.align(Alignment.TopEnd))
                        }
                        // Display Favorite badge
                        if (bottomMenuItem.label == "Favorite" && favoriteItemCount > 0) {
                            CartOrderBadge(count = favoriteItemCount, modifier = Modifier.align(Alignment.TopEnd))
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
    bottomMenuItemsList.add(BottomMenuItem(label = "Favorite", icon = painterResource(R.drawable.btn_3)))  // Added Favorite
    bottomMenuItemsList.add(BottomMenuItem(label = "Order", icon = painterResource(R.drawable.btn_4)))
    bottomMenuItemsList.add(BottomMenuItem(label = "Profile", icon = painterResource(R.drawable.btn_5)))
    return bottomMenuItemsList
}

data class BottomMenuItem(
    val label: String,
    val icon: Painter
)
