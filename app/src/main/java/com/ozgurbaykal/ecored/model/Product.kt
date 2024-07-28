package com.ozgurbaykal.ecored.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    @DocumentId
    val id: String = "",
    val brand: String = "",
    val categoryId: String = "",
    val createdAt: Timestamp? = null,
    val description: String = "",
    val discountPercentage: Int = 0,
    val discountedPrice: Double = 0.0,
    val images: List<String> = emptyList(),
    val price: Double = 0.0,
    val title: String = "",

) : Parcelable