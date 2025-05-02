package com.example.doancuoiky.Activity.Profile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.doancuoiky.ui.theme.DOANCUOIKYTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DOANCUOIKYTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditProfileScreen()
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    // Tải dữ liệu khi màn hình mở ra
    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fullName = snapshot.child("fullName").getValue(String::class.java) ?: ""
                    email = snapshot.child("email").getValue(String::class.java) ?: ""
                    phone = snapshot.child("phone").getValue(String::class.java) ?: ""
                    gender = snapshot.child("gender").getValue(String::class.java) ?: ""
                    birthDate = snapshot.child("birthDate").getValue(String::class.java) ?: ""
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Các ô nhập
        ProfileTextField("Tên người dùng", fullName) { fullName = it }
        ProfileTextField("Email", email) { email = it }
        ProfileTextField("Số điện thoại", phone) { phone = it }
        ProfileTextField("Giới tính", gender) { gender = it }
        ProfileTextField("Ngày sinh", birthDate) { birthDate = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Nút lưu
        Button(
            onClick = {
                val updatedInfo = mapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "gender" to gender,
                    "birthDate" to birthDate
                )

                dbRef.updateChildren(updatedInfo)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                        (context as? ComponentActivity)?.finish() // ➜ Tự động quay lại
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show()
                    }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Lưu", fontWeight = FontWeight.Bold, color = Color.White)
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Nút quay lại
        Button(
            onClick = {
                (context as? ComponentActivity)?.finish()
            },
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

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color(0xFF1976D2),
            unfocusedIndicatorColor = Color.Gray
        ),
        shape = RoundedCornerShape(16.dp)
    )
}
