package com.example.doancuoiky.Activity.Dashboard

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.DetailEachFood.DetailEachFoodActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun Search(context: Context) {
    // State for search text and focus
    var text by rememberSaveable { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    // State for products fetched from Realtime Database
    var products by remember { mutableStateOf<List<FoodModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch products from Realtime Database when the composable is first launched
    LaunchedEffect(Unit) {
        val db = Firebase.database.reference.child("Foods")
        db.get().addOnSuccessListener { snapshot ->
            val fetchedProducts = mutableListOf<FoodModel>()
            snapshot.children.forEach { childSnapshot ->
                try {
                    val food = childSnapshot.getValue(FoodModel::class.java)
                    food?.let {
                        // Ensure numberInCart is initialized
                        if (it.numberInCart == 0) it.numberInCart = 0
                        fetchedProducts.add(it)
                    }
                } catch (e: Exception) {
                    // Skip invalid documents
                }
            }
            products = fetchedProducts
            isLoading = false
        }.addOnFailureListener { exception ->
            errorMessage = "Failed to load products: ${exception.message}"
            isLoading = false
        }
    }

    // Filter products based on search text
    val filteredProducts = products.filter {
        it.Title.contains(text, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Search TextField
        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                isFocused = true
            },
            label = {
                Text(
                    text = "Bạn muốn ăn gì?",
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
            },
            trailingIcon = {
                Image(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = colorResource(R.color.grey),
                focusedBorderColor = Color.Transparent,
                unfocusedLabelColor = Color.Transparent,
                textColor = Color.DarkGray,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(colorResource(R.color.grey), CircleShape)
                .clickable { isFocused = true }
        )

        // Display loading, error, or suggestions
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
            isFocused && text.isNotEmpty() && filteredProducts.isNotEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductSuggestionItem(
                                product = product,
                                onClick = {
                                    // Hide suggestions immediately
                                    isFocused = false
                                    // Optionally clear the search text or keep it as the product title
                                    text = product.Title
                                    // Navigate to DetailEachFoodActivity
                                    val intent = Intent(context, DetailEachFoodActivity::class.java).apply {
                                        putExtra("object", product)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
            isFocused && text.isNotEmpty() && filteredProducts.isEmpty() -> {
                Text(
                    text = "No products found",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ProductSuggestionItem(product: FoodModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(product.ImagePath),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .background(Color.Gray, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = product.Title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$${product.Price}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}