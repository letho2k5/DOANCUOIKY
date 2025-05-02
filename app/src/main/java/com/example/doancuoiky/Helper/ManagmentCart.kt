package com.example.doancuoiky.Helper

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.Model.ChangeNumberItemsListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ManagmentCart(val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid
    private val databaseRef: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("users").child(userId ?: "").child("cart")

    fun insertItem(item: FoodModel) {
        if (userId == null) return

        databaseRef.child(item.Title).setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
    }

    fun getListCart(onComplete: (ArrayList<FoodModel>) -> Unit) {
        if (userId == null) {
            onComplete(arrayListOf())
            return
        }

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartList = arrayListOf<FoodModel>()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(FoodModel::class.java)
                    if (item != null) {
                        cartList.add(item)
                    }
                }
                onComplete(cartList)
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(arrayListOf())
            }
        })
    }

    fun minusItem(item: FoodModel, listener: ChangeNumberItemsListener) {
        val newQuantity = item.numberInCart - 1
        if (newQuantity <= 0) {
            databaseRef.child(item.Title).removeValue()
        } else {
            databaseRef.child(item.Title).child("numberInCart").setValue(newQuantity)
        }
        listener.onChanged()
    }

    fun plusItem(item: FoodModel, listener: ChangeNumberItemsListener) {
        val newQuantity = item.numberInCart + 1
        databaseRef.child(item.Title).child("numberInCart").setValue(newQuantity)
        listener.onChanged()
    }

    fun getTotalFee(cartItems: List<FoodModel>): Double {
        var fee = 0.0
        for (item in cartItems) {
            fee += item.Price * item.numberInCart
        }
        return fee
    }

    fun removeItem(item: FoodModel, callback: () -> Unit) {
        databaseRef.child(item.Title).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
                callback()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
            }
    }
}
