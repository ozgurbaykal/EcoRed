package com.ozgurbaykal.ecored.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.ozgurbaykal.ecored.model.Banner
import com.ozgurbaykal.ecored.model.Catalog
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.model.SearchHistoryItem
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

    private val _searchHistory = MutableLiveData<List<SearchHistoryItem>>()
    val searchHistory: LiveData<List<SearchHistoryItem>> get() = _searchHistory

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> get() = _searchResults

    private val _favorites = MutableLiveData<List<String>>()
    val favorites: LiveData<List<String>> get() = _favorites

    private val _favoriteProducts = MutableLiveData<List<Product>>()
    val favoriteProducts: LiveData<List<Product>> get() = _favoriteProducts


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
                val products =
                    commonRepository.getProductsWithCategoryId(categoryId, currentProductId)
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
            val (newProducts, newLastVisibleDocument) = commonRepository.getProducts(
                limit,
                lastVisibleDocument,
                categoryId
            )
            Log.i("CommonViewModel", "newProducts -> ${newProducts.toString()}")
            if (newProducts.isNotEmpty()) {
                lastVisibleDocument = newLastVisibleDocument
                loadedProducts.addAll(newProducts)
                _generalProducts.value = loadedProducts
            }
            _isLoading.value = false
        }
    }


    fun fetchSearchHistory(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val history = commonRepository.getSearchHistory(userId)
            _searchHistory.value = history
            _isLoading.value = false
        }
    }

    fun removeSearchHistoryItem(query: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            commonRepository.removeSearchHistoryItem(userId, query)
            fetchSearchHistory(userId)
            _isLoading.value = false
        }
    }

    fun clearSearchHistory(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            commonRepository.clearSearchHistory(userId)
            fetchSearchHistory(userId)
            _isLoading.value = false
        }
    }

    fun addSearchHistory(query: String, productId: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            commonRepository.addSearchToHistory(userId, query, productId)
            _isLoading.value = false
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val results = commonRepository.searchProducts(query)
            _searchResults.value = results
            _isLoading.value = false
        }
    }

    fun searchProducts(query: String, categoryId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            val results = commonRepository.searchProducts(query, categoryId)
            _generalProducts.value = results
            _isLoading.value = false
        }
    }

    fun getProductById(productId: String, callback: (Product?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val product = commonRepository.getProductById(productId)
            callback(product)
            _isLoading.value = false
        }
    }

    fun addFavorite(userId: String, productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            commonRepository.addFavorite(userId, productId)
            fetchFavorites(userId)
            _isLoading.value = false
        }
    }

    fun removeFavorite(userId: String, productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            commonRepository.removeFavorite(userId, productId)
            fetchFavorites(userId)
            _isLoading.value = false
        }
    }

    fun fetchFavorites(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val favorites = commonRepository.getFavorites(userId)
            _favorites.value = favorites
            _isLoading.value = false
        }
    }

    fun fetchFavoriteProducts(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val favorites = commonRepository.getFavoriteProducts(userId)
            _favoriteProducts.value = favorites
            _isLoading.value = false
        }
    }
}