package com.example.doancuoiky.Activity.Admin.User

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.database.*
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush

data class User(
    val id: String = "",
    val email: String = "",
    val role: String = ""
)

@Composable
fun UserScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("users")
    var userList by remember { mutableStateOf<List<User>>(emptyList()) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    // Fetch users from Firebase Realtime Database
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                snapshot.children.forEach { child ->
                    val id = child.key ?: return@forEach
                    val email = child.child("email").getValue(String::class.java) ?: "No Email"
                    val role = child.child("role").getValue(String::class.java) ?: ""
                    users.add(User(id, email, role))
                }
                // Sort users with admins first
                userList = users.sortedByDescending { it.role == "admin" }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Dialog for confirming deletion
    if (userToDelete != null) {
        Log.d("DialogDebug", "Showing dialog for user: ${userToDelete?.email}")
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text(
                    text = "Xác nhận xóa",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Bạn có muốn xóa người dùng ${userToDelete?.email}?",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        database.child(userToDelete!!.id).removeValue()
                        Toast.makeText(context, "Đã xóa: ${userToDelete?.email}", Toast.LENGTH_SHORT).show()
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Xóa", color = Color.White, fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { userToDelete = null },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Hủy", color = Color(0xFF0288D1), fontSize = 14.sp)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Display users in a LazyColumn with admins at the top
    Scaffold(
        containerColor = Color(0xFFF5F5F5), // Light gray background for modern look
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(userList) { user ->
                    UserCard(
                        user = user,
                        onDeleteClick = { userToDelete = user }
                    )
                }
            }
        }
    )
}

@Composable
fun UserCard(user: User, onDeleteClick: () -> Unit) {
    val isAdmin = user.role == "admin"
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Card click action if needed */ }
            .scale(if (isPressed) 0.98f else 1f), // Scale effect on press
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdmin) Color(0xFF4CAF50) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isAdmin) listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF388E3C)
                        ) else listOf(
                            Color.White,
                            Color(0xFFF5F5F5)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = "Email: ${user.email}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAdmin) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quyền: ${if (isAdmin) "Admin" else "Người dùng thường"}",
                    fontSize = 14.sp,
                    color = if (isAdmin) Color.White else Color.Black
                )
            }

            // Delete button with animation
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(Color(0xFFE53935), RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onDeleteClick()
                        isPressed = true
                    }
                    .scale(if (isPressed) 0.9f else 1f), // Scale effect on press
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✖",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Reset scale after press
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(200)
            isPressed = false
        }
    }
}