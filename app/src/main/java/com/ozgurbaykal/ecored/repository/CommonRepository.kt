package com.ozgurbaykal.ecored.repository


import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
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
                .limit(8)
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

    suspend fun getRandomDiscountedProducts(limit: Int = 2): List<Product> {
        return try {
            val snapshot = db.collection("products")
                .whereGreaterThan("discountPercentage", 0)
                .get()
                .await()
            val products = snapshot.toObjects(Product::class.java)
            products.shuffled().take(limit)
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

    suspend fun getProducts(
        limit: Long,
        lastVisible: DocumentSnapshot? = null,
        categoryId: String? = null
    ): Pair<List<Product>, DocumentSnapshot?> {
        return try {
            val query = if (categoryId != null) {
                db.collection("products")
                    .whereEqualTo("categoryId", categoryId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit)
            } else {
                db.collection("products")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit)
            }

            val snapshot = if (lastVisible != null) {
                query.startAfter(lastVisible).get().await()
            } else {
                query.get().await()
            }

            val products = snapshot.toObjects(Product::class.java)
            val lastVisibleDocument = snapshot.documents.lastOrNull()
            Pair(products.mapIndexed { index, product -> product.copy(id = snapshot.documents[index].id) }, lastVisibleDocument)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }



}