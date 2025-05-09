package com.example.doancuoiky.Activity.Admin.User

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme

class UserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOANCUOIKYTheme {
                UserScreen()
            }
        }
    }
}
