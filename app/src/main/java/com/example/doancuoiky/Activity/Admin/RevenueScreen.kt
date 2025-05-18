package com.example.doancuoiky.Activity.Admin

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme

class RevenueScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOANCUOIKYTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    RevenueChart()
                }
            }
        }
    }
}

@Composable
fun RevenueChart() {
    // Mock data for categories and their revenue
    val categories = listOf(
        "Pizza" to 300f,
        "Burger" to 200f,
        "Sushi" to 150f,
        "Pasta" to 100f
    )
    val maxRevenue = categories.maxOf { it.second } // For scaling the bars

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Revenue by Category",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            categories.forEach { (category, revenue) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height((revenue / maxRevenue * 200).dp) // Scale height based on max revenue
                            .background(Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Revenue label
                    Text(
                        text = "$${revenue.toInt()}",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    // Category label
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun RevenueChartPreview() {
    RevenueChart()
}