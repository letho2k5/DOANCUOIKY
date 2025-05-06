package com.example.doancuoiky.Activity.Order

import com.example.doancuoiky.Domain.FoodModel

data class Order(
    var id: String = "",
    var items: List<FoodModel> = listOf(),
    var total: Double = 0.0,
    var tax: Double = 0.0,
    var deliveryFee: Double = 0.0,
    var status: String = "To Pay",
    var userId: String = ""
)