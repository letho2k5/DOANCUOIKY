package com.example.doancuoiky.Activity.ItemsList

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.AsyncImage
import com.example.doancuoiky.Activity.DetailEachFood.DetailEachFoodActivity
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.R
import com.example.doancuoiky.ViewModel.MainViewModel
import android.util.Log

@Composable
fun ItemsList(items: List<FoodModel>, userRole: String, viewModel: MainViewModel) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        itemsIndexed(items, key = { _, item -> item.Id }) { index, item ->
            Items(item = item, index = index, userRole = userRole, viewModel = viewModel)
        }
    }
}

@Composable
fun Items(item: FoodModel, index: Int, userRole: String, viewModel: MainViewModel) {
    val context = LocalContext.current
    val isEvenRow = index % 2 == 0
    val showEditDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    // State for editable fields
    val title = remember { mutableStateOf(item.Title) }
    val price = remember { mutableStateOf(item.Price.toString()) }
    val description = remember { mutableStateOf(item.Description) }
    val calorie = remember { mutableStateOf(item.Calorie.toString()) }
    val timeValue = remember { mutableStateOf(item.TimeValue.toString()) }
    val star = remember { mutableStateOf(item.Star.toString()) }

    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
            .wrapContentHeight()
            .clickable {
                val intent = Intent(context, DetailEachFoodActivity::class.java).apply {
                    putExtra("object", item)
                }
                startActivity(context, intent, null)
            }
    ) {
        if (isEvenRow) {
            FoodImage(item = item)
            FoodDetails(
                item = item,
                userRole = userRole,
                viewModel = viewModel,
                onEditClick = { showEditDialog.value = true },
                onDeleteClick = { showDeleteDialog.value = true }
            )
        } else {
            FoodDetails(
                item = item,
                userRole = userRole,
                viewModel = viewModel,
                onEditClick = { showEditDialog.value = true },
                onDeleteClick = { showDeleteDialog.value = true }
            )
            FoodImage(item = item)
        }
    }

    // Edit Dialog
    if (showEditDialog.value && userRole == "admin") {
        AlertDialog(
            onDismissRequest = { showEditDialog.value = false },
            title = { Text("Edit Product") },
            text = {
                Column {
                    OutlinedTextField(
                        value = title.value,
                        onValueChange = { title.value = it },
                        label = { Text("Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = price.value,
                        onValueChange = { price.value = it },
                        label = { Text("Price") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = description.value,
                        onValueChange = { description.value = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = calorie.value,
                        onValueChange = { calorie.value = it },
                        label = { Text("Calorie") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = timeValue.value,
                        onValueChange = { timeValue.value = it },
                        label = { Text("Time Value (min)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = star.value,
                        onValueChange = { star.value = it },
                        label = { Text("Star Rating") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updatedFood = item.copy(
                            Title = title.value,
                            Price = price.value.toDoubleOrNull() ?: item.Price,
                            Description = description.value,
                            Calorie = calorie.value.toIntOrNull() ?: item.Calorie,
                            TimeValue = timeValue.value.toIntOrNull() ?: item.TimeValue,
                            Star = star.value.toDoubleOrNull() ?: item.Star,
                            CategoryId = item.CategoryId // Preserve CategoryId
                        )
                        Log.d("ItemsList", "Updating food: $updatedFood")
                        viewModel.updateProduct(updatedFood)
                        viewModel.loadFiltered(item.CategoryId) // Refresh list
                        showEditDialog.value = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog.value && userRole == "admin") {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete ${item.Title}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(item.Id)
                        viewModel.loadFiltered(item.CategoryId) // Refresh list
                        showDeleteDialog.value = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FoodImage(item: FoodModel) {
    AsyncImage(
        model = item.ImagePath,
        contentDescription = null,
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun RowScope.FoodDetails(
    item: FoodModel,
    userRole: String,
    viewModel: MainViewModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 8.dp)
            .fillMaxHeight()
            .weight(1f)
    ) {
        Text(
            text = item.Title,
            color = colorResource(R.color.darkPurple),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        TimingRow(item.TimeValue)
        RatingBarRow(item.Star)
        Text(
            text = "$${item.Price}",
            color = colorResource(R.color.darkPurple),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (userRole == "admin") {
            Row(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = onDeleteClick
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun RatingBarRow(star: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.star),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = "$star")
    }
}

@Composable
fun TimingRow(timeValue: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.time),
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(text = "$timeValue min")
    }
}