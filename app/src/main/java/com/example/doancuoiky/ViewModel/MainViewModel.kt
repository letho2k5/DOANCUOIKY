package com.example.doancuoiky.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.doancuoiky.Domain.BannerModel
import com.example.doancuoiky.Domain.CategoryModel
import com.example.doancuoiky.Domain.FoodModel
import com.example.doancuoiky.Repository.MainRepository

class MainViewModel: ViewModel() {
    private val repository= MainRepository()
    fun loadBanner(): LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }
    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }
    fun loadFiltered(id: String): LiveData<MutableList<FoodModel>> {
        return repository.loadFiltered(id)
    }
}