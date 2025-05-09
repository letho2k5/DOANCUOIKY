package com.example.doancuoiky.Activity.Admin.Order

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme

class OrderAdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = intent.getStringExtra("order_filter") ?: "Wait Confirmed"

        setContent {
            DOANCUOIKYTheme {
                OrderAdScreen(
                    initialFilter = filter,
                    onHistoryClick = {
                        val intent = Intent(this, OrderAdActivity::class.java)
                        intent.putExtra("order_filter", "history")
                        startActivity(intent)
                    },
                    onItemClick = { orderId ->
                        val intent = Intent(this, OrderAdDetailActivity::class.java)
                        intent.putExtra("order_id", orderId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}