package com.ozgurbaykal.ecored.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}