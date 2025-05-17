package com.example.doancuoiky.Activity.Order

import com.example.doancuoiky.Activity.Cart.BankPaymentInfo
import com.example.doancuoiky.Domain.FoodModel

data class Order(
    var id: String = "",
    var items: List<FoodModel> = listOf(),
    var total: Double = 0.0,
    var tax: Double = 0.0,
    var deliveryFee: Double = 0.0,
    var status: String = "Wait Confirmed",
    var userId: String = "",
    var userName: String? = null,
    var address: String = "",
    var paymentMethod: String = "",
    var bankPaymentInfo: BankPaymentInfo? = null // New field for bank payment details
)