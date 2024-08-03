package com.ozgurbaykal.ecored.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CartItem(
    val productId: String = "",
    val quantity: Int = 0
) : Parcelable