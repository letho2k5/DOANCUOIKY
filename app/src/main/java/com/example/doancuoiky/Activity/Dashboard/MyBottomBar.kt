package com.example.doancuoiky.Activity.Dashboard

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.Dimension.Companion.value
import com.example.doancuoiky.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.doancuoiky.Activity.Cart.CartActivity


@Composable
@Preview
fun MyBottomBar() {
    val bottomMenuItemsList = prepareBottommenu()
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf("Home") }

    BottomAppBar(
        backgroundColor = colorResource(R.color.grey),
        elevation=3.dp
    ) {
        bottomMenuItemsList.forEach { bottomMenuItem ->
            BottomNavigationItem(
                selected =(selectedItem==bottomMenuItem.label),
                onClick={
                    selectedItem=bottomMenuItem.label
                    if(bottomMenuItem.label=="Cart"){
                        context.startActivity(Intent(context, CartActivity::class.java))
                    }else{
                        Toast.makeText(context,bottomMenuItem.label, Toast.LENGTH_SHORT).show()
                    }
                },
                icon={
                    Icon(painter= bottomMenuItem.icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top=8.dp)
                            .size(20.dp))
                }
            )
        }
    }
}

data class BottomMenuItem(
    val label:String,val icon:Painter
)
@Composable
fun prepareBottommenu():List<BottomMenuItem>{
    val bottomMenuItemsList=arrayListOf<BottomMenuItem>()
    bottomMenuItemsList.add(BottomMenuItem(label="Home",icon=painterResource(R.drawable.btn_1)))
    bottomMenuItemsList.add(BottomMenuItem(label="Cart",icon=painterResource(R.drawable.btn_2)))
    bottomMenuItemsList.add(BottomMenuItem(label="Favorite",icon=painterResource(R.drawable.btn_3)))
    bottomMenuItemsList.add(BottomMenuItem(label="Order",icon=painterResource(R.drawable.btn_4)))
    bottomMenuItemsList.add(BottomMenuItem(label="Profile",icon=painterResource(R.drawable.btn_5)))
    return bottomMenuItemsList
}