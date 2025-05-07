package com.example.doancuoiky.Activity.Auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doancuoiky.Activity.Auth.ForgotPasswordScreen
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme
class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOANCUOIKYTheme {
                ForgotPasswordScreen()
            }
        }
    }
}
