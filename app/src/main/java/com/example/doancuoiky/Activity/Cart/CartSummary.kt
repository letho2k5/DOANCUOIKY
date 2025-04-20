package com.example.doancuoiky.Activity.Cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doancuoiky.R
import java.text.DecimalFormat

@Composable
fun CartSummary(itemTotal: Double, tax: Double, delivery: Double) {
    val total = itemTotal + tax + delivery
    val decimalFormat = DecimalFormat("#.000")
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
        .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
        .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Subtotal:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$${decimalFormat.format(itemTotal)}")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Delivery:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$delivery")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Total Tax:",
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$tax")
        }

        Box(
            modifier = Modifier
                .padding(top=16.dp)
                .height(1.dp)
                .fillMaxWidth()
                .background(Color.Gray)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Total:",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkPurple)
            )
            Text(text = "$${decimalFormat.format(total)}", fontWeight = FontWeight.Bold)
        }
    }
}