package com.example.doancuoiky.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.doancuoiky.Domain.BannerModel
import com.example.doancuoiky.Domain.CategoryModel
import com.example.doancuoiky.Domain.FoodModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let {
                        list.add(it)
                    }
                }
                Log.d("MainRepository", "Loaded ${list.size} categories")
                listData.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Firebase error in loadCategory: ${error.message}")
            }
        })
        return listData
    }

    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()
        val ref = firebaseDatabase.getReference("Banners")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<BannerModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(BannerModel::class.java)
                    item?.let {
                        list.add(it)
                    }
                }
                Log.d("MainRepository", "Loaded ${list.size} banners")
                listData.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Firebase error in loadBanner: ${error.message}")
            }
        })
        return listData
    }

    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        val listData = MutableLiveData<MutableList<FoodModel>>()
        val ref = firebaseDatabase.getReference("Foods")
        val query: Query = ref.orderByChild("CategoryId").equalTo(id)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("MainRepository", "Data changed for CategoryId: $id")
                val lists = mutableListOf<FoodModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(FoodModel::class.java)?.apply {
                        val key = childSnapshot.key?.toIntOrNull()
                        if (key != null) {
                            Id = key
                        }
                    }
                    if (list != null) {
                        Log.d("MainRepository", "Loaded product: $list")
                        lists.add(list)
                    }
                }
                Log.d("MainRepository", "Loaded ${lists.size} products for CategoryId: $id")
                listData.postValue(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Firebase error in loadFiltered: ${error.message}")
            }
        })
        return listData
    }

    fun addProduct(food: FoodModel) {
        val counterRef = firebaseDatabase.getReference("FoodCounter")
        counterRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
            override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
                val currentValue = currentData.getValue(Int::class.java) ?: 0
                val newId = currentValue + 1
                currentData.value = newId
                val ref = firebaseDatabase.getReference("Foods/$newId")
                val newFood = food.copy(Id = newId)
                ref.setValue(newFood)
                Log.d("MainRepository", "Added product: $newFood")
                return com.google.firebase.database.Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("MainRepository", "Transaction error in addProduct: ${error.message}")
                } else {
                    Log.d("MainRepository", "Product added successfully")
                }
            }
        })
    }

    fun updateProduct(food: FoodModel) {
        Log.d("MainRepository", "Updating product: $food")
        val ref = firebaseDatabase.getReference("Foods/${food.Id}")
        ref.setValue(food).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainRepository", "Product updated successfully")
            } else {
                Log.e("MainRepository", "Failed to update product: ${task.exception?.message}")
            }
        }
    }

    fun deleteProduct(foodId: Int) {
        Log.d("MainRepository", "Deleting product with Id: $foodId")
        val ref = firebaseDatabase.getReference("Foods/$foodId")
        ref.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainRepository", "Product deleted successfully")
            } else {
                Log.e("MainRepository", "Failed to delete product: ${task.exception?.message}")
            }
        }
    }
}