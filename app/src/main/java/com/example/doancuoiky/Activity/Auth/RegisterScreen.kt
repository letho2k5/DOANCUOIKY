package com.example.doancuoiky.Activity.Auth

import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doancuoiky.R
import com.example.doancuoiky.Activity.Profile.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Nam") }
    var expanded by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }

    val genderOptions = listOf("Nam", "Nữ", "Khác")

    fun showDatePicker(context: android.content.Context, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                onDateSelected("%02d/%02d/%04d".format(day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    fun isNetworkAvailable(context: android.content.Context): Boolean {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnected
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Register Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Đăng ký",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Số điện thoại") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Địa chỉ") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = birthDate,
            onValueChange = {},
            label = { Text("Ngày sinh") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = "Calendar",
                    modifier = Modifier.clickable {
                        showDatePicker(context) { selectedDate -> birthDate = selectedDate }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDatePicker(context) { selectedDate -> birthDate = selectedDate }
                }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                label = { Text("Giới tính") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            gender = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Xác nhận mật khẩu") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isNetworkAvailable(context)) {
                    Toast.makeText(context, "Không có kết nối mạng. Vui lòng kiểm tra lại.", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (fullName.isBlank() || email.isBlank() || phone.isBlank() || address.isBlank() ||
                    birthDate.isBlank() || password.isBlank() || confirmPassword.isBlank()
                ) {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(context, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password != confirmPassword) {
                    Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password.length < 6) {
                    Toast.makeText(context, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (phone.length < 10) {
                    Toast.makeText(context, "Số điện thoại phải có ít nhất 10 chữ số", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                showConsentDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đăng ký", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Đã có tài khoản? Đăng nhập",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        )
    }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = { Text("Đồng ý chia sẻ dữ liệu") },
            text = {
                Text(
                    "Chúng tôi sẽ lưu trữ thông tin của bạn trên Firebase để cung cấp dịch vụ. " +
                            "Thông tin này có thể được sử dụng để cá nhân hóa trải nghiệm người dùng. Bạn có đồng ý không?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConsentDialog = false
                    coroutineScope.launch {
                        auth.fetchSignInMethodsForEmail(email)
                            .addOnSuccessListener { result ->
                                val signInMethods = result.signInMethods
                                if (signInMethods != null && signInMethods.isNotEmpty()) {
                                    Toast.makeText(context, "Email đã được sử dụng. Vui lòng chọn email khác.", Toast.LENGTH_LONG).show()
                                } else {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { result ->
                                            val uid = result.user?.uid ?: return@addOnSuccessListener
                                            val user = UserProfile(
                                                uid = uid,
                                                fullName = fullName,
                                                email = email,
                                                phone = phone,
                                                address = address,
                                                birthDate = birthDate,
                                                gender = gender,
                                                balance = 1000000.0 // Set default balance to 1,000,000
                                            )
                                            database.child("users").child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                                    context.startActivity(Intent(context, LoginActivity::class.java).apply {
                                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    })
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Lưu thông tin thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Đăng ký thất bại: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Không thể kiểm tra email: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                }) {
                    Text("Đồng ý")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConsentDialog = false
                    Toast.makeText(context, "Đăng ký bị hủy", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Hủy")
                }
            }
        )
    }
}