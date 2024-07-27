package com.ozgurbaykal.ecored.model

import com.google.firebase.Timestamp

data class Product(
    val brand: String = "",
    val categoryId: String = "",
    val createdAt: Timestamp? = null,
    val description: String = "",
    val discountPercentage: Int = 0,
    val discountedPrice: Double = 0.0,
    val images: List<String> = emptyList(),
    val price: Double = 0.0,
    val title: String = ""
)