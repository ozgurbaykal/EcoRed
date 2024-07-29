package com.ozgurbaykal.ecored.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Catalog(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val image: String = "",
    val createdAt: Timestamp? = null
)