package com.example.doancuoiky.Activity.Profile

data class UserProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val balance: Double = 1000000.0 // Default balance of 1,000,000
)