package com.example.doancuoiky.Activity.Profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.Activity.Auth.LoginActivity
import com.example.doancuoiky.Activity.Dashboard.MainActivity
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.Profile.UserProfile
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOANCUOIKYTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        onBackClick = {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)

    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().getReference("users")

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLoginPrompt by remember { mutableStateOf(uid == null) }
    var allowRender by remember { mutableStateOf(uid != null) }

    // Load data from Firebase
    LaunchedEffect(uid) {
        uid?.let {
            database.child(it).get().addOnSuccessListener { snapshot ->
                val profile = snapshot.getValue(UserProfile::class.java)
                if (profile != null) {
                    userProfile = profile
                }
            }
        }
    }

    if (showLoginPrompt) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Thông báo") },
            text = { Text("Bạn cần đăng nhập để xem trang cá nhân.") },
            confirmButton = {
                TextButton(onClick = {
                    showLoginPrompt = false
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    (context as? ComponentActivity)?.finish()
                }) {
                    Text("Đăng nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLoginPrompt = false
                    allowRender = false
                    onBackClick()
                }) {
                    Text("Hủy")
                }
            }
        )
    }

    if (!allowRender) return

    // UI nếu đã đăng nhập
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .background(Color.White, shape = CircleShape)
                                .padding(5.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_user),
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = userProfile?.fullName ?: "Tên người dùng",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = userProfile?.email ?: "Email",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Thêm các thông tin khác
                        ProfileInfoLine("Số điện thoại", userProfile?.phone)
                        ProfileInfoLine("Giới tính", userProfile?.gender)
                        ProfileInfoLine("Ngày sinh", userProfile?.birthDate)
                        ProfileInfoLine("Địa chỉ", userProfile?.address)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Chuyển đến màn hình chỉnh sửa thông tin
                        val intent = Intent(context, EditProfileActivity::class.java)
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Chỉnh sửa thông tin", fontWeight = FontWeight.Bold, color = Color.White)
                }

                ProfileOptionItem(
                    iconRes = R.drawable.ic_logout,
                    title = "Đăng xuất",
                    onClick = { showLogoutDialog = true },
                    textColor = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Quay lại", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Xác nhận đăng xuất") },
                text = { Text("Bạn có chắc chắn muốn đăng xuất không?") },
                confirmButton = {
                    TextButton(onClick = {
                        with(sharedPref.edit()) {
                            clear()
                            apply()
                        }
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Text("Đăng xuất", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoLine(label: String, value: String?) {
    Text(
        text = "$label: ${value ?: "-"}",
        fontSize = 16.sp,
        color = Color.DarkGray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}

@Composable
fun ProfileOptionItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = textColor
            )
        }
    }
}
