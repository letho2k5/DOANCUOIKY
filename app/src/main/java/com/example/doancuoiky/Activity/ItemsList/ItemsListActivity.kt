package com.example.doancuoiky.Activity.ItemsList

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.doancuoiky.Activity.BaseActivity
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.R
import com.example.doancuoiky.ViewModel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log

class ItemsListActivity : BaseActivity() {
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getStringExtra("id") ?: ""
        title = intent.getStringExtra("title") ?: ""

        setContent {
            ItemListScreen(
                title = title,
                onBackClick = { finish() },
                viewModel = viewModel,
                id = id
            )
        }
    }
}

@Composable
private fun ItemListScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: MainViewModel,
    id: String
) {
    // Tạo LiveData một lần và quan sát nó
    val filteredLiveData = remember(id) { viewModel.loadFiltered(id) }
    val items by filteredLiveData.observeAsState(emptyList())

    var isLoading by remember { mutableStateOf(true) }
    var userRole by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // State for new product input fields
    val newTitle = remember { mutableStateOf("") }
    val newPrice = remember { mutableStateOf("") }
    val newDescription = remember { mutableStateOf("") }
    val newCalorie = remember { mutableStateOf("") }
    val newTimeValue = remember { mutableStateOf("") }
    val newStar = remember { mutableStateOf("") }
    val newImagePath = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("users/$userId/role")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userRole = snapshot.getValue(String::class.java) ?: ""
                        Log.d("ItemListScreen", "User role: $userRole")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        userRole = ""
                        Log.e("ItemListScreen", "Failed to load user role: ${error.message}")
                    }
                })
        } else {
            userRole = ""
            Log.d("ItemListScreen", "No user logged in")
        }
    }

    LaunchedEffect(items) {
        Log.d("ItemListScreen", "Items loaded: ${items.size} items")
        isLoading = false // Đặt loading = false khi có dữ liệu hoặc không có dữ liệu
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(
            modifier = Modifier.padding(top = 36.dp, start = 16.dp, end = 16.dp)
        ) {
            val (backBtn, cartTxt, addBtn) = createRefs()

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(cartTxt) {
                        centerTo(parent)
                    },
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                text = title
            )

            Image(
                painter = painterResource(R.drawable.back_grey),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .constrainAs(backBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )

            if (userRole == "admin") {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .constrainAs(addBtn) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                ) {
                    Text("Add Product")
                }
            }
        }

        // Add Product Dialog
        if (showAddDialog && userRole == "admin") {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add New Product") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTitle.value,
                            onValueChange = { newTitle.value = it },
                            label = { Text("Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newPrice.value,
                            onValueChange = { newPrice.value = it },
                            label = { Text("Price") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newDescription.value,
                            onValueChange = { newDescription.value = it },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newCalorie.value,
                            onValueChange = { newCalorie.value = it },
                            label = { Text("Calorie") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newTimeValue.value,
                            onValueChange = { newTimeValue.value = it },
                            label = { Text("Time Value (min)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newStar.value,
                            onValueChange = { newStar.value = it },
                            label = { Text("Star Rating") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = newImagePath.value,
                            onValueChange = { newImagePath.value = it },
                            label = { Text("Image URL") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newProduct = FoodModel(
                                BestFood = false,
                                CategoryId = id,
                                Description = newDescription.value,
                                Id = 0, // Will be set by addProduct
                                ImagePath = newImagePath.value.ifEmpty { "https://example.com/default.jpg" },
                                LocationId = 1,
                                Price = newPrice.value.toDoubleOrNull() ?: 10.0,
                                PriceId = 1,
                                TimeId = 1,
                                Title = newTitle.value,
                                Calorie = newCalorie.value.toIntOrNull() ?: 200,
                                numberInCart = 0,
                                Star = newStar.value.toDoubleOrNull() ?: 4.0,
                                TimeValue = newTimeValue.value.toIntOrNull() ?: 15
                            )

                            Log.d("ItemListScreen", "Adding product: $newProduct")
                            viewModel.addProduct(newProduct)

                            showAddDialog = false
                            // Reset input fields
                            newTitle.value = ""
                            newPrice.value = ""
                            newDescription.value = ""
                            newCalorie.value = ""
                            newTimeValue.value = ""
                            newStar.value = ""
                            newImagePath.value = ""
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    Button(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items found in this category")
                }
            } else {
                ItemsList(items = items, userRole = userRole ?: "", viewModel = viewModel)
            }
        }
    }
}