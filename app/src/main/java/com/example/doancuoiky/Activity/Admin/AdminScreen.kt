package com.example.doancuoiky.Activity.Admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.doancuoiky.Activity.Admin.User.UserActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.example.doancuoiky.Activity.Admin.Order.OrderAdActivity


@Composable
fun AdminScreen() {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineSmall
        )
        // Button để quản lý thực đơn
        Button(onClick = { /* Chuyển hướng đến màn hình quản lý thực đơn */ }) {
            Text("Quản lý thực đơn")
        }
        // Button để quản lý người dùng
        Button(onClick = {
            context.startActivity(Intent(context, UserActivity::class.java))
        }) {
            Text("Quản lý người dùng")
        }
        // Button để quản lý đơn hàng
        Button(onClick = {
            context.startActivity(Intent(context, OrderAdActivity::class.java))
        }) {
            Text("Quản lý đơn hàng")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminScreenPreview() {
    AdminScreen()
}
