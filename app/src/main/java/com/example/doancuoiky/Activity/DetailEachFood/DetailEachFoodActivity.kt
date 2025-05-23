package com.example.doancuoiky.Activity.DetailEachFood

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.doancuoiky.Activity.BaseActivity
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.Helper.ManagmentCart
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class DetailEachFoodActivity : BaseActivity() {
    private lateinit var item: FoodModel
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        item = intent.getSerializableExtra("object") as FoodModel
        item.numberInCart = 1
        managmentCart = ManagmentCart(context = this)

        setContent {
            DetailScreen(
                item = item,
                onBackClick = { finish() },
                onAddToCartClick = {
                    managmentCart.insertItem(item)
                }
            )
        }
    }
}

@Composable
private fun DetailScreen(
    item: FoodModel,
    onBackClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    var numberInCart by remember { mutableStateOf(item.numberInCart) }

    ConstraintLayout {
        val (footer, column) = createRefs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .constrainAs(column) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(footer.top) // Đảm bảo không bị footer che
                }
                .padding(bottom = 180.dp)
        ) {
            HeaderSection(
                item = item,
                numberInCart = numberInCart,
                onBackClick = onBackClick,
                onIncrement = {
                    numberInCart++
                    item.numberInCart = numberInCart
                },
                onDecrement = {
                    if (numberInCart > 1) {
                        numberInCart--
                        item.numberInCart = numberInCart
                    }
                }
            )

            DescriptionSection(description = item.Description, foodId = item.Id.toString())

            // ⭐ Thêm đánh giá vào đây, cuộn cùng nội dung
            ReviewSection(foodId = item.Id)
        }

        FooterSection(
            onAddToCartClick,
            totalPrice = (item.Price * numberInCart),
            Modifier.constrainAs(footer) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
                start.linkTo(parent.start)
            }
        )
    }
}