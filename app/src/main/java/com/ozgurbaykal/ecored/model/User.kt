package com.ozgurbaykal.ecored.model


import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val address: String = "",
    val phoneNumber: String = "",
    val searchHistory: List<SearchHistoryItem> = emptyList(),
    val favorites: List<String> = emptyList()
)


data class SearchHistoryItem(
    val query: String = "",
    val productId: String = ""
)