package com.example.doancuoiky.Activity.Cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.Helper.ManagmentCart
import com.example.doancuoiky.R
import java.text.DecimalFormat
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CartItem(
    item: FoodModel,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    managmentCart: ManagmentCart,
    onItemChange: () -> Unit,
    onDeleteItem: (FoodModel) -> Unit  // Thay đổi để chuyển FoodModel vào khi xóa
) {
    // State để kiểm tra xem hộp thoại có hiển thị không
    var showDeleteDialog by remember { mutableStateOf(false) }

    // State để lưu món hàng cần xóa
    var itemToDelete by remember { mutableStateOf<FoodModel?>(null) }

    ConstraintLayout(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .border(1.dp, colorResource(R.color.grey), shape = RoundedCornerShape(10.dp))
    ) {
        val (checkboxRef, pic, titleTxt, feeEachTime, totalEachItem, quantity, deleteBtn) = createRefs()
        var numberInCart by remember { mutableStateOf(item.numberInCart) }
        val decimalFormat = DecimalFormat("#.00")

        // Checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) },
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(checkboxRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )

        // Image
        Image(
            painter = rememberAsyncImagePainter(item.ImagePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(135.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .constrainAs(pic) {
                    start.linkTo(checkboxRef.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )

        // Title
        Text(
            text = item.Title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 8.dp, top = 8.dp)
                .constrainAs(titleTxt) {
                    start.linkTo(pic.end)
                    top.linkTo(pic.top)
                }
        )

        // Price
        Text(
            text = "$${decimalFormat.format(item.Price)}",
            fontSize = 16.sp,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier
                .padding(start = 8.dp)
                .constrainAs(feeEachTime) {
                    start.linkTo(titleTxt.start)
                    top.linkTo(titleTxt.bottom)
                }
        )

        // Total Price per Item
        Text(
            text = "$${decimalFormat.format(numberInCart * item.Price)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(8.dp)
                .constrainAs(totalEachItem) {
                    end.linkTo(parent.end)
                    bottom.linkTo(pic.bottom)
                }
        )

        // Delete Button (Positioned below the Checkbox)
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Red,
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    // Khi nhấn vào thùng rác, hiển thị hộp thoại xác nhận
                    itemToDelete = item
                    showDeleteDialog = true
                }
                .constrainAs(deleteBtn) {
                    top.linkTo(checkboxRef.bottom)  // Position below the checkbox
                    start.linkTo(parent.start)
                }
        )

        // Quantity Box (Plus and Minus Buttons)
        ConstraintLayout(
            modifier = Modifier
                .width(100.dp)
                .padding(start = 8.dp)
                .constrainAs(quantity) {
                    start.linkTo(titleTxt.start)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            val (plusBtn, minusBtn, quantityText) = createRefs()

            Text(
                text = numberInCart.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.constrainAs(quantityText) {
                    centerTo(parent)
                }
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        managmentCart.plusItem(item) {
                            numberInCart++
                            onItemChange()
                        }
                    }
                    .constrainAs(plusBtn) {
                        end.linkTo(parent.end)
                        centerVerticallyTo(parent)
                    }
            ) {
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.orange),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        managmentCart.minusItem(item) {
                            numberInCart = maxOf(0, numberInCart - 1)
                            onItemChange()
                        }
                    }
                    .constrainAs(minusBtn) {
                        start.linkTo(parent.start)
                        centerVerticallyTo(parent)
                    }
            ) {
                Text(
                    text = "-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.orange),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Hiển thị hộp thoại xác nhận khi muốn xóa
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa món này khỏi giỏ hàng?") },
            confirmButton = {
                TextButton(onClick = {
                    // Xóa món khi nhấn "Có"
                    itemToDelete?.let { onDeleteItem(it) }
                    showDeleteDialog = false
                }) {
                    Text("Có")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Không")
                }
            }
        )
    }
}
