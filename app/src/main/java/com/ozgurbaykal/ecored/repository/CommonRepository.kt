package com.ozgurbaykal.ecored.repository


import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.ozgurbaykal.ecored.model.Banner
import com.ozgurbaykal.ecored.model.Catalog
import com.ozgurbaykal.ecored.model.Product
import com.ozgurbaykal.ecored.model.SearchHistoryItem
import com.ozgurbaykal.ecored.model.User
import kotlinx.coroutines.tasks.await
import java.util.Locale
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
    suspend fun getSearchHistory(userId: String): List<SearchHistoryItem> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .get()
                .await()
            val user = snapshot.toObject(User::class.java)
            user?.searchHistory?.takeLast(10)?.reversed() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun removeSearchHistoryItem(userId: String, query: String) {
        try {
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val searchHistory = snapshot.toObject(User::class.java)?.searchHistory?.toMutableList() ?: mutableListOf()
                searchHistory.removeAll { it.query == query }
                transaction.update(userRef, "searchHistory", searchHistory)
            }.await()
        } catch (e: Exception) {
        }
    }

    suspend fun clearSearchHistory(userId: String) {
        try {
            val userRef = db.collection("users").document(userId)
            userRef.update("searchHistory", emptyList<SearchHistoryItem>()).await()
        } catch (e: Exception) {
        }
    }

    suspend fun searchProducts(query: String, categoryId: String? = null): List<Product> {
        return try {
            val lowercaseQuery = query.lowercase(Locale.ROOT)
            val snapshot = if (categoryId != null) {
                db.collection("products")
                    .whereEqualTo("categoryId", categoryId)
                    .whereGreaterThanOrEqualTo("titleLowerCase", lowercaseQuery)
                    .whereLessThanOrEqualTo("titleLowerCase", lowercaseQuery + '\uf8ff')
                    .get()
                    .await()
            } else {
                db.collection("products")
                    .whereGreaterThanOrEqualTo("titleLowerCase", lowercaseQuery)
                    .whereLessThanOrEqualTo("titleLowerCase", lowercaseQuery + '\uf8ff')
                    .get()
                    .await()
            }
            snapshot.toObjects(Product::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSearchToHistory(userId: String, query: String, productId: String) {
        try {
            val userRef = db.collection("users").document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val searchHistory = snapshot.toObject(User::class.java)?.searchHistory?.toMutableList() ?: mutableListOf()

                if (!searchHistory.any { it.query == query }) {
                    if (searchHistory.size >= 10) { //MAX HISTORY ARRAY SIZE
                        searchHistory.removeAt(0)
                    }
                    searchHistory.add(SearchHistoryItem(query, productId))
                    transaction.update(userRef, "searchHistory", searchHistory)
                }
            }.await()
        } catch (e: Exception) {
        }
    }

    suspend fun getProductById(productId: String): Product? {
        return try {
            val snapshot = db.collection("products").document(productId).get().await()
            snapshot.toObject(Product::class.java)
        } catch (e: Exception) {
            null
        }
    }

}