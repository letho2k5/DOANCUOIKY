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

@Composable
fun AdminScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kebab Ngon",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(
                painter = painterResource(id = R.drawable.bell_icon),
                contentDescription = "Notifications",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { }
            )
        }
        Text(
            text = "Fast Order - Big Flavor - Kebab Ngon NOW!",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BannerSection()
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionCard("Category", painterResource(id = R.drawable.category_icon)) {
                context.startActivity(Intent(context, AdminCategoryActivity::class.java))
            }
            ActionCard("Order", painterResource(id = R.drawable.order_icon)) {
                context.startActivity(Intent(context, OrderAdActivity::class.java))
            }
            ActionCard("Revenue", painterResource(id = R.drawable.revenue_icon)) {
                context.startActivity(Intent(context, RevenueScreen::class.java))
            }
            ActionCard("Account", painterResource(id = R.drawable.user_icon)) {
                context.startActivity(Intent(context, UserActivity::class.java))
            }
        }
    }
}

@Composable
fun BannerSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E8C7))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "First order 30% off",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Free delivery",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "the perfect your pizza journey",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
            Button(
                onClick = { /* Handle Order Now click */ },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
            ) {
                Text("Order Now", color = Color.White)
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.painter.Painter) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Black
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ActionCard(title: String, icon: androidx.compose.ui.graphics.painter.Painter, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
            .padding(4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
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