package com.example.doancuoiky.Activity.Favourite

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.doancuoiky.Domain.FoodModel
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FavouriteScreen() {
    val selectedTab = remember { mutableStateOf("Food Items") }
    val tabs = listOf("Food Items", "Resturents")

    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid
    val database = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "").child("favourites")
    val foodItems = remember { mutableStateOf<List<FoodModel>>(emptyList()) }

    // 👇 Dialog hiển thị khi chưa đăng nhập
    var showLoginDialog by remember { mutableStateOf(user == null) }

    // 👉 Nếu chưa đăng nhập thì hiện hộp thoại cảnh báo
    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLoginDialog = false
                    val intent = android.content.Intent(context, com.example.doancuoiky.Activity.Auth.LoginActivity::class.java)
                    context.startActivity(intent)
                }) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginDialog = false }) {
                    Text("Hủy")
                }
            },
            title = { Text("Yêu cầu đăng nhập") },
            text = { Text("Bạn cần đăng nhập để xem danh sách yêu thích.") }
        )
    }

    // Nếu đã đăng nhập thì load dữ liệu
    if (user != null) {
        LaunchedEffect(Unit) {
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<FoodModel>()
                    for (itemSnapshot in snapshot.children) {
                        val food = itemSnapshot.getValue(FoodModel::class.java)
                        if (food != null) {
                            list.add(food)
                        }
                    }
                    foodItems.value = list
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // UI
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Favorites", style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab.value == tab
                Button(
                    onClick = { selectedTab.value = tab },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFF75C3E) else Color.Transparent,
                        contentColor = if (isSelected) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(tab)
                }
            }
        }

        if (user != null) {
            LazyColumn {
                items(foodItems.value) { food ->
                    FoodCard(food)
                }
            }
        }
    }
}


@Composable
fun FoodCard(food: FoodModel) {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().getReference("users").child(userId ?: "").child("favourites")
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    database.orderByChild("Id").equalTo(food.Id.toDouble()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (child in snapshot.children) {
                                child.ref.removeValue()
                            }
                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Lỗi khi xóa", Toast.LENGTH_SHORT).show()
                        }
                    })
                    showDialog = false
                }) {
                    Text("Có")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Không")
                }
            },
            title = { Text("Xác nhận") },
            text = { Text("Bạn có muốn xóa món này khỏi danh sách yêu thích không?") }
        )
    }

    // 👇 Wrap bằng Clickable để xử lý điều hướng khi click vào món
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = android.content.Intent(context, com.example.doancuoiky.Activity.DetailEachFood.DetailEachFoodActivity::class.java)
                intent.putExtra("object", food) // Gửi object
                context.startActivity(intent)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            Box {
                Image(
                    painter = rememberAsyncImagePainter(model = food.ImagePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                Text(
                    text = "$${food.Price}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                IconButton(
                    onClick = {
                        showDialog = true
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(food.Title, style = MaterialTheme.typography.titleMedium)
                Text(food.Description, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("${food.Star} (25)", fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}


