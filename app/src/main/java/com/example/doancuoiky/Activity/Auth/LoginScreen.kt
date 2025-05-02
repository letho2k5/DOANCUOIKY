package com.example.doancuoiky.Activity.auth

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.Activity.Auth.RegisterActivity
import com.example.doancuoiky.Activity.Dashboard.MainActivity
import com.example.doancuoiky.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) } // Thông báo lỗi hoặc thành công
    val scope = rememberCoroutineScope()

    // Ẩn message sau 3 giây
    LaunchedEffect(message) {
        if (message != null) {
            delay(3000)
            message = null
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Banner hiển thị thông báo ở trên cùng
            AnimatedVisibility(visible = message != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFE082)) // màu vàng nhạt
                        .padding(12.dp)
                ) {
                    Text(
                        text = message ?: "",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Image(
                painter = painterResource(id = R.drawable.login),
                contentDescription = "Food Banner",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Đăng nhập",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp),
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Lưu thông tin người dùng vào SharedPreferences
                                    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putString("userName", email.split("@")[0]) // Lấy phần trước @ làm userName
                                        putString("userEmail", email) // Lưu email
                                        putBoolean("isLoggedIn", true) // Đánh dấu đã đăng nhập
                                        apply()
                                    }
                                    message = "Đăng nhập thành công"
                                    // Điều hướng đến MainActivity
                                    context.startActivity(Intent(context, MainActivity::class.java))
                                    (context as? ComponentActivity)?.finish()
                                } else {
                                    message = "Lỗi: ${task.exception?.message}"
                                }
                            }
                    } else {
                        message = "Vui lòng điền đầy đủ thông tin"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đăng nhập", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chưa có tài khoản? Đăng ký",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                }
            )
        }
    }
}