package com.example.doancuoiky.Activity.Admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.doancuoiky.Activity.Admin.Food.AdminCategoryActivity
import com.example.doancuoiky.Activity.Admin.Order.OrderAdActivity
import com.example.doancuoiky.Activity.Admin.Order.OrderAdHistoryActivity
import com.example.doancuoiky.Activity.Admin.User.UserActivity
import androidx.compose.ui.tooling.preview.Preview
import com.example.doancuoiky.R
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import com.example.doancuoiky.Activity.Auth.LoginActivity
import androidx.compose.runtime.*


@Composable
fun AdminScreen() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) } // trạng thái hiển thị hộp thoại

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = "Logout")
            },
            text = {
                Text("Do you want to log out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kebab Ngon",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_logout),
                contentDescription = "Logout",
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        showDialog = true // hiển thị hộp thoại khi bấm
                    }
            )
        }

        Text(
            text = "Fast Order - Big Flavor - Kebab Ngon NOW!",
            fontSize = 14.sp,
            color = Color.Gray
        )

        // Banner
        BannerSection()

        // Action Cards Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) { index ->
                when (index) {
                    0 -> ActionCard("Category", painterResource(id = R.drawable.category_icon)) {
                        context.startActivity(Intent(context, AdminCategoryActivity::class.java))
                    }
                    1 -> ActionCard("Order", painterResource(id = R.drawable.order_icon)) {
                        context.startActivity(Intent(context, OrderAdActivity::class.java))
                    }
                    2 -> ActionCard("Revenue", painterResource(id = R.drawable.revenue_icon)) {
                        context.startActivity(Intent(context, RevenueScreen::class.java))
                    }
                    3 -> ActionCard("Account", painterResource(id = R.drawable.user_icon)) {
                        context.startActivity(Intent(context, UserActivity::class.java))
                    }
                }
            }
        }
    }
}


@Composable
fun BannerSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E8C7))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "First order 30% off",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Free delivery",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Start your pizza journey",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Button(
                onClick = { /* Handle Order Now click */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Order Now", color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ActionCard(title: String, icon: androidx.compose.ui.graphics.painter.Painter, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    AdminScreen()
}