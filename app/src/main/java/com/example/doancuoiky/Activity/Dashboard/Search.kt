package com.example.doancuoiky.Activity.Dashboard


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.doancuoiky.R

@Composable
@Preview
fun Search() {
    var text by rememberSaveable { mutableStateOf(value = "") }

    TextField(value = text, onValueChange = { text =it },

        label = {
            Text(
                text = "Bạn muốn ăn gì?",
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                color= Color.DarkGray
            )
        },
        trailingIcon ={
            Image(painter = painterResource(R.drawable.search),
                contentDescription=null,
                modifier= Modifier.size(22.dp)
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
            .padding(16.dp)
            .height(50.dp)
            .background(colorResource(R.color.grey), CircleShape)
    )
}