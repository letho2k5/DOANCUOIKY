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
    private val database = FirebaseDatabase.getInstance()

    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    private fun getCartRef(): DatabaseReference? {
        val userId = getUserId()
        return if (userId != null)
            database.getReference("users").child(userId).child("cart")
        else
            null
    }

    fun insertItem(item: FoodModel) {
        val ref = getCartRef() ?: return
        ref.child(item.Title).setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            }
    }

    fun getListCart(onComplete: (ArrayList<FoodModel>) -> Unit) {
        val ref = getCartRef()
        if (ref == null) {
            onComplete(arrayListOf())
            return
        }

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
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
        val ref = getCartRef() ?: return
        val newQuantity = item.numberInCart - 1
        if (newQuantity <= 0) {
            ref.child(item.Title).removeValue()
        } else {
            ref.child(item.Title).child("numberInCart").setValue(newQuantity)
        }
        listener.onChanged()
    }

    fun plusItem(item: FoodModel, listener: ChangeNumberItemsListener) {
        val ref = getCartRef() ?: return
        val newQuantity = item.numberInCart + 1
        ref.child(item.Title).child("numberInCart").setValue(newQuantity)
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
        val ref = getCartRef() ?: return
        ref.child(item.Title).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Item removed", Toast.LENGTH_SHORT).show()
                callback()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
            }
    }
}


