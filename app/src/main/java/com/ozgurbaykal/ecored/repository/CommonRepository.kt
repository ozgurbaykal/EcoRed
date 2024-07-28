package com.ozgurbaykal.ecored.repository


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ozgurbaykal.ecored.model.Banner
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
    suspend fun getProductsWithCategoryId(categoryId: String, currentProductId: String = ""): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)

            if(currentProductId.isNotEmpty()){
                return products.filter { it.id != currentProductId }
            }

            return products
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

    suspend fun getBanners(): List<Banner> {
        return try {
            val snapshot = db.collection("banners")
                .get()
                .await()
            snapshot.toObjects(Banner::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}