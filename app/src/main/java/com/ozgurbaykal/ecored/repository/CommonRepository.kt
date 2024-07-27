package com.ozgurbaykal.ecored.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ozgurbaykal.ecored.model.Catalog
import com.ozgurbaykal.ecored.model.Product
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CommonRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    suspend fun getDiscountedProducts(): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .whereGreaterThan("discountPercentage", 0)
                .orderBy("discountPercentage", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCatalogs(): List<Catalog> {
        return try {
            val snapshot = db.collection("categories")
                .orderBy("createdAt")
                .get()
                .await()
            snapshot.toObjects(Catalog::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}