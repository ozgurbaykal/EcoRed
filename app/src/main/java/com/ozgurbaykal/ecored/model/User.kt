package com.ozgurbaykal.ecored.model

data class User(
    val id: Int?,
    val name: String,
    val lastName: String,
    val email: String,
    val firebaseUid: String?,
)
