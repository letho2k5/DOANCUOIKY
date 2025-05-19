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
                listData.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Firebase error: ${error.message}")
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
                listData.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Firebase error: ${error.message}")
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
                val lists = mutableListOf<FoodModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(FoodModel::class.java)?.apply {
                        val key = childSnapshot.key?.toIntOrNull()
                        if (key != null) {
                            Id = key
                        }
                    }
                    if (list != null && list.CategoryId == id) {
                        lists.add(list)
                    }
                }
                Log.d("MainRepository", "Filtered items for CategoryId $id: $lists")
                listData.postValue(lists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Firebase error: ${error.message}")
            }
        })
        return listData
    }

    fun addProduct(food: FoodModel) {
        // Validate input data
        if (food.Title.isEmpty() || food.CategoryId.isEmpty() || food.Price <= 0.0) {
            Log.e("MainRepository", "Invalid product data: $food")
            return
        }

        // Generate a unique key for the new product
        val newKey = firebaseDatabase.getReference("Foods").push().key
            ?: run {
                Log.e("MainRepository", "Failed to generate new key")
                return
            }

        // Convert key to integer ID (for compatibility with existing integer Id field)
        val newId = newKey.hashCode()

        // Create new FoodModel with generated ID
        val newFood = food.copy(Id = newId)

        // Save to Firebase
        val ref = firebaseDatabase.getReference("Foods/$newId")
        ref.setValue(newFood)
            .addOnSuccessListener {
                Log.d("MainRepository", "Product added successfully with ID: $newId")
            }
            .addOnFailureListener { error ->
                Log.e("MainRepository", "Failed to add product: ${error.message}")
            }
    }

    fun updateProduct(food: FoodModel) {
        if (food.Id == 0 || food.CategoryId.isEmpty() || food.Title.isEmpty() || food.Price <= 0.0) {
            Log.e("MainRepository", "Invalid product data: $food")
            return
        }

        val updates = mapOf(
            "CategoryId" to food.CategoryId,
            "Title" to food.Title,
            "Price" to food.Price,
            "ImagePath" to food.ImagePath,
            "Description" to food.Description,
            "BestFood" to food.BestFood,
            "LocationId" to food.LocationId,
            "PriceId" to food.PriceId,
            "TimeId" to food.TimeId,
            "Calorie" to food.Calorie,
            "Star" to food.Star,
            "TimeValue" to food.TimeValue
            // Không cập nhật numberInCart vì nó có thể liên quan đến giỏ hàng
        )

        val ref = firebaseDatabase.getReference("Foods/${food.Id}")
        ref.updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainRepository", "Cập nhật sản phẩm thành công: ${food.Id}")
            } else {
                Log.e("MainRepository", "Cập nhật sản phẩm thất bại: ${task.exception?.message}")
            }
        }
    }

    fun deleteProduct(foodId: Int) {
        val ref = firebaseDatabase.getReference("Foods/$foodId")
        ref.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("MainRepository", "Product deleted successfully: $foodId")
            } else {
                Log.e("MainRepository", "Failed to delete product: ${task.exception?.message}")
            }
        }
    }
}