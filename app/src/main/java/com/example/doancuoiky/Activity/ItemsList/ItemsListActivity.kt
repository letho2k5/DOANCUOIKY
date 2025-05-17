package com.example.doancuoiky.Activity.ItemsList

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
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

class ItemsListActivity : BaseActivity() {
    private val viewModel = MainViewModel()
    private var id: String = ""
    private var title: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getStringExtra("id")?.toString() ?: ""
        title = intent.getStringExtra("title") ?: ""
        Log.d("ItemsListActivity", "Received id: $id, title: $title")

        setContent {
            ItemListScreen(
                title = title,
                onBackClick = { finish() },
                viewModel = viewModel,
                id = id,
                onAddProduct = {
                    viewModel.addProduct(
                        FoodModel(
                            BestFood = false,
                            CategoryId = id,
                            Description = "New product description",
                            Id = 0, // Will be set by addProduct
                            ImagePath = "https://example.com/default.jpg",
                            LocationId = 1,
                            Price = 10.0,
                            PriceId = 1,
                            TimeId = 1,
                            Title = "New Product",
                            Calorie = 200,
                            numberInCart = 0,
                            Star = 4.0,
                            TimeValue = 15
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun ItemListScreen(
    title: String,
    onBackClick: () -> Unit,
    viewModel: MainViewModel,
    id: String,
    onAddProduct: () -> Unit
) {
    val items by viewModel.loadFiltered(id).observeAsState(emptyList())
    var isLoading by remember { mutableStateOf(true) }
    var userRole by remember { mutableStateOf<String?>(null) }

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
        }
    }

    LaunchedEffect(id) {
        isLoading = true
        viewModel.loadFiltered(id)
    }

    LaunchedEffect(items) {
        Log.d("ItemListScreen", "Items loaded: $items")
        isLoading = false
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
                    onClick = {
                        onAddProduct()
                        viewModel.loadFiltered(id) // Refresh list after adding
                    },
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
        if (isLoading && userRole == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No products found in this category")
            }
        } else {
            ItemsList(items = items, userRole = userRole ?: "", viewModel = viewModel)
        }
    }
}