package com.example.doancuoiky.Domain

data class ReviewModel(
    var userName: String = "", // Full name of the user
    var rating: Float = 0f,
    var comment: String = "",
    var isPharmacist: Boolean = false,
    var parentReviewId: String? = null, // Firebase key of the parent review (null for top-level reviews)
    var reviewId: String? = null, // Firebase key of this review
    var uid: String? = null, // UID to identify the user who wrote the review
    var timestamp: Long = 0L // Epoch time in milliseconds when the review was posted
)