package com.ozgurbaykal.ecored.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CartItem
import com.ozgurbaykal.ecored.model.CreditCard
import kotlinx.android.parcel.Parcelize
import java.util.UUID

@Parcelize
data class Order(
    val orderId: String = UUID.randomUUID().toString(),
    val address: Address = Address(),
    val card: CreditCard = CreditCard(),
    val totalPrice: Double = 0.0,
    val items: List<CartItem> = emptyList(),
    val orderDate: Timestamp = Timestamp.now()
) : Parcelable