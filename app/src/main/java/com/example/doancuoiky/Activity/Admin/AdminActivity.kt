
package com.example.doancuoiky.Activity.Admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOANCUOIKYTheme {
                // Gọi AdminScreen để hiển thị giao diện quản trị
                AdminScreen()
            }
        }
    }
}
