package com.ozgurbaykal.ecored.model

data class CreditCard(
    val cardNumber: String = "",
    val expirationMonth: String = "",
    val expirationYear: String = "",
    val ccv: String = "",
    val cardTitle: String = "",
)