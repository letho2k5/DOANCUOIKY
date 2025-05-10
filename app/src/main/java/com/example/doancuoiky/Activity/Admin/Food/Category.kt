package com.example.doancuoiky.Activity.Admin.Food

import com.google.firebase.database.PropertyName

data class Category(
    @get:PropertyName("Id") @set:PropertyName("Id")
    var Id: Int = 0,

    @get:PropertyName("Name") @set:PropertyName("Name")
    var Name: String = "",

    @get:PropertyName("ImagePath") @set:PropertyName("ImagePath")
    var ImagePath: String = ""
)
