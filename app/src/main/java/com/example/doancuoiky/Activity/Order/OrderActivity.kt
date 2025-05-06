package com.example.doancuoiky.Activity.Order

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme

class OrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = intent.getStringExtra("order_filter") ?: "To Pay"

        setContent {
            DOANCUOIKYTheme {
                OrderScreen(
                    initialFilter = filter,
                    onHistoryClick = {
                        val intent = Intent(this, OrderActivity::class.java)
                        intent.putExtra("order_filter", "history")
                        startActivity(intent)
                    },
                    onItemClick = { orderId ->
                        val intent = Intent(this, OrderDetailActivity::class.java)
                        intent.putExtra("order_id", orderId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}