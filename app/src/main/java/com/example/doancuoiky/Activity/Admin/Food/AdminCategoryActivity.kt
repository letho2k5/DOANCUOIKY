package com.example.doancuoiky.Activity.Admin.Food

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class AdminCategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminCategoryScreen()
        }
    }
}
