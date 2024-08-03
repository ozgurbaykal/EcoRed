package com.ozgurbaykal.ecored.model


import com.google.firebase.Timestamp
import com.ozgurbaykal.ecored.Order

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val address: String = "",
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val favorites: List<String> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val addresses: List<Address> = emptyList(),
    val orders: List<Order> = emptyList()

)