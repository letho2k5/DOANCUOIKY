package com.example.doancuoiky.Activity.DetailEachFood

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.TextStyle


@Composable
fun DescriptionSection(description: String, foodId: String) {
    var isAdmin by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(description) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference
    val context = LocalContext.current

    // Check if user is admin
    LaunchedEffect(userId) {
        if (userId == null) {
            Log.e("Firebase", "User is not authenticated")
            Toast.makeText(context, "Please log in to edit description", Toast.LENGTH_SHORT).show()
            return@LaunchedEffect
        }
        val roleRef = database.child("users").child(userId).child("role")
        roleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isAdmin = snapshot.getValue(String::class.java) == "admin"
                if (!isAdmin) {
                    Log.d("Firebase", "User is not an admin, role: ${snapshot.getValue(String::class.java)}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error checking user role: ${error.message}", error.toException())
                Toast.makeText(context, "Error checking role: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column {
        Text(
            text = "Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (isAdmin) {
            // Admin: Editable TextField and Save button
            TextField(
                value = editedDescription,
                onValueChange = { editedDescription = it },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = colorResource(R.color.darkPurple)
                )
            )
            Button(
                onClick = {
                    if (foodId.isBlank()) {
                        Log.e("Firebase", "Invalid foodId: $foodId")
                        Toast.makeText(context, "Invalid food ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (editedDescription.isBlank()) {
                        Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // Save updated description to Firebase using the correct path "Foods"
                    val foodRef = database.child("Foods").child(foodId).child("Description")
                    foodRef.setValue(editedDescription)
                        .addOnSuccessListener {
                            Log.d("Firebase", "Description updated successfully for foodId: $foodId")
                            Toast.makeText(context, "Description saved successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firebase", "Failed to update description for foodId: $foodId, error: ${exception.message}", exception)
                            Toast.makeText(context, "Failed to save: ${exception.message}", Toast.LENGTH_LONG).show()
                        }
                },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                Text("Save Description")
            }
        } else {
            // Non-admin: Read-only description
            Text(
                text = description,
                fontSize = 16.sp,
                color = colorResource(R.color.darkPurple),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}