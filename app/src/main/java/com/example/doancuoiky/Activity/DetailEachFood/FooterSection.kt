package com.example.doancuoiky.Activity.DetailEachFood

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.doancuoiky.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import java.text.DecimalFormat

@Composable
fun FooterSection(onAddToCartClick: () -> Unit, totalPrice: Double, modifier: Modifier = Modifier) {
    var isAdmin by remember { mutableStateOf(false) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val database = FirebaseDatabase.getInstance().reference

    // Check if user is admin
    LaunchedEffect(userId) {
        userId?.let {
            val roleRef = database.child("users").child(it).child("role")
            roleRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isAdmin = snapshot.getValue(String::class.java) == "admin"
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error checking user role", error.toException())
                }
            })
        }
    }

    ConstraintLayout(
        modifier = modifier
            .height(75.dp)
            .fillMaxWidth()
            .background(color = colorResource(R.color.grey))
            .padding(horizontal = 16.dp)
    ) {
        val (orderBtn, price) = createRefs()

        if (!isAdmin) {
            // Non-admin: Show Order button
            Button(
                onClick = onAddToCartClick,
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.orange)
                ),
                modifier = Modifier
                    .width(140.dp)
                    .height(50.dp)
                    .constrainAs(orderBtn) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.cart),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Order", fontSize = 20.sp, color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .width(140.dp)
                .height(50.dp)
                .constrainAs(price) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
        ) {
            Text(
                text = "Total Price",
                fontSize = 18.sp,
                color = colorResource(R.color.darkPurple)
            )
            val decimalFormat = DecimalFormat("#.00")
            Text(
                text = "$${decimalFormat.format(totalPrice)}",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp),
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.darkPurple)
            )
        }
    }
}