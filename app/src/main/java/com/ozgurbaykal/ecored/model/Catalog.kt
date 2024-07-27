package com.ozgurbaykal.ecored.model

import com.google.firebase.Timestamp

data class Catalog(
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val createdAt: Timestamp? = null
)