package com.example.doancuoiky.Activity.Favourite

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.doancuoiky.Activity.Favourite.FavouriteScreen
import com.example.doancuoiky.R

class FavouriteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                FavouriteScreen() // Call the FavouriteScreen composable here
            }
        }
    }
}
