package com.ozgurbaykal.ecored.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreditCard(
    val cardNumber: String = "",
    val expirationMonth: String = "",
    val expirationYear: String = "",
    val ccv: String = "",
    val cardTitle: String = "",
) : Parcelable