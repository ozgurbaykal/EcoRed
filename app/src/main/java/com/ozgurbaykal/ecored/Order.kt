package com.ozgurbaykal.ecored

import com.google.firebase.Timestamp
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CartItem
import com.ozgurbaykal.ecored.model.CreditCard
import java.util.UUID

data class Order(
    val orderId: String = UUID.randomUUID().toString(),
    val address: Address = Address(),
    val card: CreditCard = CreditCard(),
    val totalPrice: Double = 0.0,
    val items: List<CartItem> = emptyList(),
    val orderDate: Timestamp = Timestamp.now()
)