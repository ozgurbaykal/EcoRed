package com.ozgurbaykal.ecored.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.ozgurbaykal.ecored.model.Banner
import com.ozgurbaykal.ecored.model.Catalog
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.repository.CommonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommonViewModel @Inject constructor(
    private val commonRepository: CommonRepository
) : ViewModel() {

    private val _discountedProducts = MutableLiveData<List<Product>>()
    val discountedProducts: LiveData<List<Product>> get() = _discountedProducts


    private val _catalogs = MutableLiveData<List<Catalog>>()
    val catalogs: LiveData<List<Catalog>> get() = _catalogs

    private val _banners = MutableLiveData<List<Banner>>()
    val banners: LiveData<List<Banner>> get() = _banners

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _generalProducts = MutableLiveData<List<Product>>()
    val generalProducts: LiveData<List<Product>> get() = _generalProducts

    private val loadedProducts = mutableListOf<Product>()
    var lastVisibleDocument: DocumentSnapshot? = null

    fun fetchCatalogs() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val catalogs = commonRepository.getCatalogs()
                _catalogs.value = catalogs
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchRandomDiscountedProducts(limit: Int = 2) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val products = commonRepository.getRandomDiscountedProducts(limit)
                _generalProducts.value = products
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchBanners() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val banners = commonRepository.getBanners()
                _banners.value = banners
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchDiscountedProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val products = commonRepository.getDiscountedProducts()
                _discountedProducts.value = products
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }


    fun getProductsWithCategory(categoryId: String, currentProductId: String = "") {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val products = commonRepository.getProductsWithCategoryId(categoryId, currentProductId)
                _generalProducts.value = products
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun loadProducts(limit: Long, categoryId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val (newProducts, newLastVisibleDocument) = commonRepository.getProducts(limit, lastVisibleDocument, categoryId)
            Log.i("CommonViewModel", "newProducts -> ${newProducts.toString()}")
            if (newProducts.isNotEmpty()) {
                lastVisibleDocument = newLastVisibleDocument
                loadedProducts.addAll(newProducts)
                _generalProducts.value = loadedProducts
            }
            _isLoading.value = false
        }
    }

}