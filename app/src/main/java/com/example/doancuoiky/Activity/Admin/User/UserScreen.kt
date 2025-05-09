package com.example.doancuoiky.Activity.Admin.User

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

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
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có muốn xóa người dùng ${userToDelete?.email}?") },
            confirmButton = {
                TextButton(onClick = {
                    database.child(userToDelete!!.id).removeValue()
                    Toast.makeText(context, "Đã xóa: ${userToDelete?.email}", Toast.LENGTH_SHORT).show()
                    userToDelete = null
                }) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Display users in a LazyColumn with admins at the top
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(userList) { user ->
                UserCard(
                    user = user,
                    onDeleteClick = { userToDelete = user }
                )
            }
        }
    }
}


@Composable
fun UserCard(user: User, onDeleteClick: () -> Unit) {
    val isAdmin = user.role == "admin"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(if (isAdmin) Color(0xFF4CAF50) else Color.White) // Green for admin, white for regular user
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // User details
            Text(
                text = "Email: ${user.email}",
                color = if (isAdmin) Color.White else Color.Black // White text for admin, black for regular user
            )
            Text(
                text = "Quyền: ${if (isAdmin) "Admin" else "Người dùng thường"}",
                color = if (isAdmin) Color.White else Color.Black
            )
        }

        // Delete button (✖) at top-right corner
        Text(
            text = "✖",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable { onDeleteClick() }
                .padding(8.dp),
            color = Color.Red // Red color for the delete icon
        )
    }
}
