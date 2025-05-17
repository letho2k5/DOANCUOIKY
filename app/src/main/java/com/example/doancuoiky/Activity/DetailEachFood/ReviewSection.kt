package com.example.doancuoiky.Activity.DetailEachFood

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Slider
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.Domain.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewSection(foodId: Int, modifier: Modifier = Modifier) {
    var reviews by remember { mutableStateOf<List<ReviewModel>>(emptyList()) }
    var userNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) } // Store fullName by uid
    var replyingTo by remember { mutableStateOf<String?>(null) } // Store reviewId of the review being replied to
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Fetch fullName for all users who have reviews
    LaunchedEffect(reviews) {
        val uids = reviews.mapNotNull { it.uid }.distinct()
        val namesMap = mutableMapOf<String, String>()
        uids.forEach { uid ->
            FirebaseDatabase.getInstance().getReference("users")
                .child(uid)
                .child("fullName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val fullName = snapshot.getValue(String::class.java) ?: "Người dùng"
                        namesMap[uid] = fullName
                        userNames = namesMap.toMap()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ReviewSection", "Error fetching fullName: ${error.message}")
                        namesMap[uid] = "Người dùng"
                        userNames = namesMap.toMap()
                    }
                })
        }
    }

    fun loadReviews() {
        fetchReviewsForFood(foodId) {
            reviews = it
        }
    }

    LaunchedEffect(foodId) {
        loadReviews()
    }

    Column(modifier.padding(16.dp)) {
        ReviewInputSection(
            foodId = foodId,
            onReviewSubmitted = {
                loadReviews()
                replyingTo = null // Reset reply state after submission
            },
            currentUser = currentUser?.uid,
            replyingTo = replyingTo
        )

        val userReviews = reviews.filter { !it.isPharmacist && it.parentReviewId == null }

        Text(
            text = "Đánh giá sản phẩm (${userReviews.size} đánh giá)",
            style = MaterialTheme.typography.titleMedium
        )

        val avgStar = if (userReviews.isNotEmpty()) userReviews.map { it.rating }.average() else 0.0
        Text(
            text = "Trung bình: ${"%.1f".format(avgStar)} ★",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display top-level reviews and their nested replies
        userReviews.forEach { review ->
            val displayName = if (review.uid == currentUser?.uid) {
                "Bạn"
            } else {
                userNames[review.uid] ?: review.userName
            }
            ReviewItem(
                review = review,
                displayName = displayName,
                setReplyingTo = { replyingTo = it },
                level = 0,
                reviews = reviews,
                userNames = userNames,
                currentUser = currentUser
            )
        }
    }
}

@Composable
fun ReviewItem(
    review: ReviewModel,
    displayName: String,
    setReplyingTo: (String) -> Unit,
    level: Int,
    reviews: List<ReviewModel>,
    userNames: Map<String, String>,
    currentUser: com.google.firebase.auth.FirebaseUser?
) {
    val currentTime = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 17) // 05:14 PM
        set(Calendar.MINUTE, 14)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val timeDiffMillis = currentTime - review.timestamp
    val timeDiffHours = timeDiffMillis / (1000 * 60 * 60)
    val timeDiffDays = timeDiffMillis / (1000 * 60 * 60 * 24)
    val elapsedTime = when {
        timeDiffDays > 0 -> "${timeDiffDays} ngày trước"
        timeDiffHours > 0 -> "${timeDiffHours} giờ trước"
        else -> "vừa xong"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = (level * 16).dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 8.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_myplaces), // Placeholder avatar
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // User name
                    Text(
                        text = displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Timestamp
                    Text(
                        text = elapsedTime,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Rating (for top-level reviews only)
                if (review.parentReviewId == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        repeat(review.rating.toInt()) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = Color.Yellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${review.rating}",
                            fontSize = 14.sp
                        )
                    }
                }

                // Comment
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Reply button (hidden for current user's reviews)
                if (review.uid != currentUser?.uid) {
                    TextButton(
                        onClick = { setReplyingTo(review.reviewId ?: "") },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "Trả lời",
                            fontSize = 12.sp,
                            color = Color.Blue
                        )
                    }
                }
            }
        }

        // Display nested replies
        val replies = reviews.filter { it.parentReviewId == review.reviewId }
        replies.forEach { reply ->
            val replyDisplayName = if (reply.uid == currentUser?.uid) {
                "Bạn"
            } else {
                userNames[reply.uid] ?: reply.userName
            }
            ReviewItem(
                review = reply,
                displayName = replyDisplayName,
                setReplyingTo = setReplyingTo,
                level = level + 1,
                reviews = reviews,
                userNames = userNames,
                currentUser = currentUser
            )
        }
    }
}

@Composable
fun ReviewInputSection(
    foodId: Int,
    onReviewSubmitted: () -> Unit,
    currentUser: String?,
    replyingTo: String? = null
) {
    val context = LocalContext.current
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }
    var userFullName by remember { mutableStateOf("Người dùng") }

    // Fetch fullName of the current user
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser)
                .child("fullName")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        userFullName = snapshot.getValue(String::class.java) ?: "Người dùng"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ReviewInputSection", "Error fetching fullName: ${error.message}")
                        userFullName = "Người dùng"
                    }
                })
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (replyingTo == null) "Viết đánh giá" else "Trả lời đánh giá",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text(if (replyingTo == null) "Nhận xét của bạn" else "Trả lời của bạn") },
            modifier = Modifier.padding(top = 8.dp)
        )

        if (replyingTo == null) {
            Text("Chọn số sao: ${rating.toInt()} ★", modifier = Modifier.padding(top = 8.dp))
            Slider(
                value = rating,
                onValueChange = { rating = it },
                valueRange = 1f..5f,
                steps = 3,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (currentUser != null) {
                        val newReview = ReviewModel(
                            userName = userFullName,
                            rating = if (replyingTo == null) rating else 0f,
                            comment = comment,
                            isPharmacist = false,
                            parentReviewId = replyingTo,
                            uid = currentUser,
                            timestamp = System.currentTimeMillis()
                        )
                        submitReview(foodId, newReview) { reviewId ->
                            newReview.reviewId = reviewId
                            Toast.makeText(context, "Gửi thành công!", Toast.LENGTH_SHORT).show()
                            onReviewSubmitted()
                        }
                        comment = ""
                        rating = 5f
                    } else {
                        Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(if (replyingTo == null) "Gửi đánh giá" else "Gửi trả lời")
            }
            if (replyingTo != null) {
                TextButton(onClick = { onReviewSubmitted() }) {
                    Text("Hủy")
                }
            }
        }
    }
}

fun fetchReviewsForFood(foodId: Int, onComplete: (List<ReviewModel>) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("Reviews").child(foodId.toString())

    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val reviews = mutableListOf<ReviewModel>()
            for (child in snapshot.children) {
                val review = child.getValue(ReviewModel::class.java)
                if (review != null) {
                    review.reviewId = child.key // Assign Firebase key to reviewId
                    reviews.add(review)
                }
            }
            Log.d("ReviewSection", "Fetched ${reviews.size} reviews")
            onComplete(reviews)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("ReviewSection", "Error fetching reviews: ${error.message}")
            onComplete(emptyList())
        }
    })
}

fun submitReview(foodId: Int, review: ReviewModel, onComplete: (String) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("Reviews").child(foodId.toString())
    val newReviewRef = ref.push()
    newReviewRef.setValue(review).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("ReviewSection", "Review/reply submitted successfully with ID: ${newReviewRef.key}")
            onComplete(newReviewRef.key ?: "")
        } else {
            Log.e("ReviewSection", "Error submitting review/reply: ${task.exception?.message}")
        }
    }
}