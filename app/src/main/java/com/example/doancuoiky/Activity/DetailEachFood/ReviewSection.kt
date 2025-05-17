package com.example.doancuoiky.Activity.DetailEachFood

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.doancuoiky.Domain.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.widget.Toast
import androidx.compose.ui.text.style.TextOverflow
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.util.*

@Composable
fun ReviewSection(foodId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isCloudinaryInitialized by remember { mutableStateOf(false) }

    // Initialize Cloudinary with correct credentials
    LaunchedEffect(Unit) {
        if (!isCloudinaryInitialized) {
            try {
                val config = mapOf(
                    "cloud_name" to "djrlah4ry",
                    "api_key" to "847946367387831",
                    "api_secret" to "W25CO72-QmqlG1Nz1JBsLv7achU"
                )
                MediaManager.init(context, config)
                isCloudinaryInitialized = true
                Log.d("Cloudinary", "Cloudinary initialized successfully")
            } catch (e: Exception) {
                Log.e("Cloudinary", "Failed to initialize Cloudinary: ${e.message}")
                Toast.makeText(context, "Không thể kết nối với Cloudinary", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var reviews by remember { mutableStateOf<List<ReviewModel>>(emptyList()) }
    var userNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var replyingTo by remember { mutableStateOf<String?>(null) }
    val currentUser = FirebaseAuth.getInstance().currentUser

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
                replyingTo = null
            },
            currentUser = currentUser?.uid,
            replyingTo = replyingTo,
            context = context
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
    val currentTime = System.currentTimeMillis()
    val timeDiffMillis = currentTime - (review.timestamp.takeIf { it > 0 } ?: currentTime)
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
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = elapsedTime,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

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

                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (!review.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(review.imageUrl),
                        contentDescription = "Review Image",
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                }

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
    replyingTo: String? = null,
    context: Context
) {
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(5f) }
    var userFullName by remember { mutableStateOf("Người dùng") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Determine the appropriate permission based on Android version
    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        Log.d("Cloudinary", "Selected URI: $uri")
        uri?.let {
            if (isNetworkAvailable(context)) {
                isUploading = true
                Toast.makeText(context, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
                uploadImageToCloudinary(context, it) { url ->
                    imageUrl = url
                    isUploading = false
                    if (url != null) {
                        Toast.makeText(context, "Đã tải lên", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Không thể tải ảnh lên", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Vui lòng kiểm tra kết nối internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for storage access
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePickerLauncher.launch("image/*")
        } else {
            // Check if permission is permanently denied
            if (!shouldShowRequestPermissionRationale(context, storagePermission)) {
                Toast.makeText(
                    context,
                    "Quyền truy cập ảnh bị từ chối vĩnh viễn. Vui lòng cấp quyền trong Cài đặt.",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Cần quyền truy cập để chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

        Button(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(context, storagePermission) == PackageManager.PERMISSION_GRANTED -> {
                        imagePickerLauncher.launch("image/*")
                    }
                    shouldShowRequestPermissionRationale(context, storagePermission) -> {
                        Toast.makeText(
                            context,
                            "Ứng dụng cần quyền truy cập ảnh để chọn hình. Vui lòng cấp quyền.",
                            Toast.LENGTH_LONG
                        ).show()
                        permissionLauncher.launch(storagePermission)
                    }
                    else -> {
                        permissionLauncher.launch(storagePermission)
                    }
                }
            },
            modifier = Modifier.padding(top = 8.dp),
            enabled = !isUploading
        ) {
            Text(if (isUploading) "Đang tải ảnh..." else "Chọn ảnh")
        }

        if (imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Selected Image",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(100.dp)
                    .clip(CircleShape)
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
                            timestamp = System.currentTimeMillis(),
                            imageUrl = imageUrl
                        )
                        submitReview(foodId, newReview) { reviewId ->
                            newReview.reviewId = reviewId
                            Toast.makeText(context, "Gửi thành công!", Toast.LENGTH_SHORT).show()
                            onReviewSubmitted()
                        }
                        comment = ""
                        rating = 5f
                        selectedImageUri = null
                        imageUrl = null
                    } else {
                        Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isUploading
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

// Helper function to check if permission rationale should be shown
private fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    return androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
        context as androidx.activity.ComponentActivity,
        permission
    )
}

// Helper function to check network availability
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}

private fun uploadImageToCloudinary(context: Context, uri: Uri, onComplete: (String?) -> Unit) {
    try {
        Log.d("Cloudinary", "Starting upload with URI: $uri")
        MediaManager.get().upload(uri)
            .option("folder", "reviews") // Organize images in a folder
            .option("public_id", "review_${System.currentTimeMillis()}") // Unique public ID
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("Cloudinary", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    Log.d("Cloudinary", "Upload progress: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String
                    Log.d("Cloudinary", "Upload success: $imageUrl")
                    onComplete(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary", "Upload error: ${error.description}")
                    onComplete(null)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w("Cloudinary", "Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()
    } catch (e: Exception) {
        Log.e("Cloudinary", "Upload exception: ${e.message}")
        onComplete(null)
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
                    review.reviewId = child.key
                    reviews.add(review)
                    Log.d("fetchReviewsForFood", "Fetched review with timestamp: ${review.timestamp}")
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
            Log.d("ReviewSection", "Review/reply submitted successfully with ID: ${newReviewRef.key} and timestamp: ${review.timestamp}")
            onComplete(newReviewRef.key ?: "")
        } else {
            Log.e("ReviewSection", "Error submitting review/reply: ${task.exception?.message}")
        }
    }
}