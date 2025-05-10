package com.example.doancuoiky.Activity.Admin.Food

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage


@Composable
fun AdminCategoryScreen() {
    val database = FirebaseDatabase.getInstance().getReference("Category")
    val context = LocalContext.current
    val storage = FirebaseStorage.getInstance().reference.child("CategoryImages")

    var categories by remember { mutableStateOf<List<Pair<String, Category>>>(emptyList()) }
    var editingCategory by remember { mutableStateOf<Pair<String, Category>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Pair<String, Category>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker launcher (declared first)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        newImageUri = uri
        if (uri != null) {
            Toast.makeText(context, "Đã chọn ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for accessing storage
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Cần quyền truy cập bộ nhớ", Toast.LENGTH_SHORT).show()
        }
    }

    // Load categories from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Pair<String, Category>>()
                snapshot.children.forEach {
                    val category = it.getValue(Category::class.java)
                    category?.let { cat -> list.add(it.key!! to cat) }
                }
                categories = list
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi tải danh mục: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(categories) { (key, category) ->
                AdminCategoryCard(
                    category = category,
                    onEdit = {
                        editingCategory = key to category
                        newCategoryName = category.Name
                    },
                    onDelete = {
                        showDeleteConfirm = key to category
                    }
                )
            }
        }

        // Floating Add Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm")
        }
    }

    // Edit Dialog
    editingCategory?.let { (key, _) ->
        AlertDialog(
            onDismissRequest = { editingCategory = null },
            title = { Text("Sửa danh mục") },
            text = {
                TextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Tên mới") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        database.child(key).child("Name").setValue(newCategoryName)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Đã sửa tên", Toast.LENGTH_SHORT).show()
                                editingCategory = null
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Lỗi sửa: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Vui lòng nhập tên", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCategory = null }) {
                    Text("Huỷ")
                }
            }
        )
    }

    // Delete Confirm Dialog
    showDeleteConfirm?.let { (key, category) ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Xác nhận xoá") },
            text = { Text("Bạn có chắc muốn xoá '${category.Name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    database.child(key).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Đã xoá", Toast.LENGTH_SHORT).show()
                            showDeleteConfirm = null
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Lỗi xoá: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }) {
                    Text("Xoá", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Huỷ")
                }
            }
        )
    }

    // Add New Category Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newCategoryName = ""
                newImageUri = null
            },
            title = { Text("Thêm danh mục") },
            text = {
                Column {
                    TextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Tên danh mục") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }) {
                        Text("Chọn ảnh")
                    }
                    newImageUri?.let {
                        Text("Đã chọn ảnh", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        val newKey = database.push().key ?: return@TextButton
                        val createCategory: (String) -> Unit = { imageUrl ->
                            val newCategory = Category(
                                Id = newKey.hashCode(),
                                Name = newCategoryName,
                                ImagePath = imageUrl
                            )
                            database.child(newKey).setValue(newCategory)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Thêm thành công", Toast.LENGTH_SHORT).show()
                                    showAddDialog = false
                                    newCategoryName = ""
                                    newImageUri = null
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi lưu danh mục: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }

                        if (newImageUri != null) {
                            val imageRef = storage.child("${System.currentTimeMillis()}.jpg")
                            imageRef.putFile(newImageUri!!)
                                .addOnSuccessListener {
                                    imageRef.downloadUrl.addOnSuccessListener { url ->
                                        createCategory(url.toString())
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(context, "Lỗi lấy URL ảnh: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Lỗi tải ảnh: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            createCategory("") // Nếu không có ảnh, truyền chuỗi rỗng hoặc URL mặc định
                        }

                    } else {
                        Toast.makeText(context, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Thêm")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newCategoryName = ""
                    newImageUri = null
                }) {
                    Text("Huỷ")
                }
            }
        )
    }
}

@Composable
fun AdminCategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEEEDC))
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = category.ImagePath,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
        )
        Text(text = category.Name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Blue)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}