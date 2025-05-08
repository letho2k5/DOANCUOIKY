package com.example.doancuoiky.Activity.DetailEachFood

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.rememberAsyncImagePainter
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun HeaderSection(
    item: FoodModel,
    numberInCart: Int,
    onBackClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(570.dp)
            .padding(bottom = 16.dp)
    ) {
        val (back, fav, mainImage, arcImg, title, detailRow, numberRow) = createRefs()

        Image(
            painter = rememberAsyncImagePainter(model = item.ImagePath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .constrainAs(mainImage) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
        )

        Image(
            painter = painterResource(R.drawable.arc_bg),
            contentDescription = null,
            modifier = Modifier
                .height(190.dp)
                .constrainAs(arcImg) {
                    top.linkTo(mainImage.bottom, margin = (-64).dp)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
        )

        BackButton(
            onBackClick,
            Modifier.constrainAs(back) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
        )

        FavoriteButton(
            item = item,
            modifier = Modifier.constrainAs(fav) {
                top.linkTo(parent.top)
                end.linkTo(parent.end)
            }
        )

        Text(
            text = item.Title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.darkPurple),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .constrainAs(title) {
                    top.linkTo(arcImg.top, margin = 32.dp)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                }
        )

        RowDetail(item, Modifier.constrainAs(detailRow) {
            top.linkTo(title.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        })

        NumberRow(
            item = item,
            numberInCart = numberInCart,
            onIncrement = onIncrement,
            onDecrement = onDecrement,
            modifier = Modifier.constrainAs(numberRow) {
                top.linkTo(detailRow.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
        )
    }
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.back),
        contentDescription = null,
        modifier = modifier
            .padding(start = 16.dp, top = 48.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun FavoriteButton(
    item: FoodModel,
    modifier: Modifier = Modifier
) {
    var isFavorite by remember { mutableStateOf(false) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference

    // Kiểm tra Firebase xem nó đã có trong danh sách yêu thích chưa
    LaunchedEffect(item.Id) {
        userId?.let {
            val favRef = database.child("users").child(it).child("favourites").child(item.Id.toString())
            favRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error checking favorites", error.toException())
                }
            })
        }
    }

    val iconRes = if (isFavorite) R.drawable.fullheart else R.drawable.heart

    Box(
        modifier = modifier
            .padding(end = 16.dp, top = 48.dp)
            .size(48.dp) // Đảm bảo đủ không gian cho vòng tròn
            .clickable {
                if (isFavorite) {
                    removeFromFavorites(item)
                    isFavorite = false
                } else {
                    addToFavorites(item)
                    isFavorite = true
                }
            }
    ) {
        // Vòng tròn mờ
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50)) // Vòng tròn
                .background(Color.Gray.copy(alpha = 0.3f)) // Vòng tròn mờ với màu xám
        )

        // Nút yêu thích (heart)
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center) // Căn giữa icon trong vòng tròn
                .size(24.dp) // Kích thước của icon yêu thích
        )
    }
}



private fun addToFavorites(item: FoodModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val database = FirebaseDatabase.getInstance().reference
        val favoriteRef = database.child("users").child(userId).child("favourites").child(item.Id.toString())

        val favoriteData = mapOf(
            "BestFood" to item.BestFood,
            "CategoryId" to item.CategoryId,
            "Description" to item.Description,
            "Id" to item.Id,
            "ImagePath" to item.ImagePath,
            "LocationId" to item.LocationId,
            "Price" to item.Price,
            "PriceId" to item.PriceId,
            "TimeId" to item.TimeId,
            "Title" to item.Title,
            "Calorie" to item.Calorie,
            "numberInCart" to item.numberInCart,
            "Star" to item.Star,
            "TimeValue" to item.TimeValue
        )

        favoriteRef.setValue(favoriteData)
            .addOnSuccessListener {
                Log.d("Firebase", "Thêm vào mục yêu thích thành công.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Thêm vào mục yêu thích thất bại.", it)
            }
    } else {
        Log.e("Firebase", "Người dùng chưa đăng nhập.")
    }
}

private fun removeFromFavorites(item: FoodModel) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        val database = FirebaseDatabase.getInstance().reference
        val favoriteRef = database.child("users").child(userId).child("favourites").child(item.Id.toString())

        favoriteRef.removeValue()
            .addOnSuccessListener {
                Log.d("Firebase", "Xoá khỏi mục yêu thích thành công.")
            }
            .addOnFailureListener {
                Log.e("Firebase", "Xoá khỏi mục yêu thích thất bại.", it)
            }
    }
}
