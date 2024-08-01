package com.ozgurbaykal.ecored.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(
    val name: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val addressTitle: String = ""
) : Parcelable
