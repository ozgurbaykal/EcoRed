package com.ozgurbaykal.ecored.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.model.Order
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CreditCard
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.model.User
import com.ozgurbaykal.ecored.repository.CommonRepository
import com.ozgurbaykal.ecored.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val commonRepository: CommonRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isCardAdded = MutableLiveData<Boolean>()
    val isCardAdded: LiveData<Boolean> get() = _isCardAdded


    private val _isAddressAdded = MutableLiveData<Boolean>()
    val isAddressAdded: LiveData<Boolean> get() = _isAddressAdded


    private val _isOrderCompleted = MutableLiveData<Boolean>()
    val isOrderCompleted: LiveData<Boolean> get() = _isOrderCompleted


    private val _orderHistory = MutableLiveData<List<Order>>()
    val orderHistory: LiveData<List<Order>> get() = _orderHistory

    fun addUser(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.addUser(user)
                _currentUser.value = user
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchUser(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _currentUser.value = userRepository.getUser(userId)
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun addCreditCard(creditCard: CreditCard) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.addCreditCard(auth.currentUser?.uid ?: return@launch, creditCard)
                _isLoading.value = false
                _isCardAdded.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _isCardAdded.value = false
                _errorMessage.value = e.message
            }
        }
    }


    fun addAddress(address: Address) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.addAddress(auth.currentUser?.uid ?: return@launch, address)
                _isLoading.value = false
                _isAddressAdded.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _isAddressAdded.value = false
                _errorMessage.value = e.message
            }
        }
    }


    fun deleteAddress(address: Address) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.deleteAddress(auth.currentUser?.uid ?: return@launch, address.addressTitle)
                fetchUser(auth.currentUser?.uid ?: return@launch)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun deleteCreditCard(card: CreditCard) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.deleteCreditCard(auth.currentUser?.uid ?: return@launch, card.cardTitle)
                fetchUser(auth.currentUser?.uid ?: return@launch)
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun completeOrder(order: Order) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.addOrder(auth.currentUser?.uid ?: return@launch, order)
                _isLoading.value = false
                _isOrderCompleted.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _isOrderCompleted.value = false
                _errorMessage.value = e.message
            }
        }
    }

    fun fetchOrderHistory(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val orders = userRepository.getOrderHistory(userId)
                _orderHistory.value = orders
                _isLoading.value = false
                _errorMessage.value = null
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message
            }
        }
    }
    fun getProductById(productId: String, callback: (Product?) -> Unit) {
        viewModelScope.launch {
            try {
                val product = commonRepository.getProductById(productId)
                callback(product)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

}
