package com.ozgurbaykal.ecored.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.ozgurbaykal.ecored.model.Order
import com.ozgurbaykal.ecored.model.Address
import com.ozgurbaykal.ecored.model.CartItem
import com.ozgurbaykal.ecored.model.CreditCard
import com.ozgurbaykal.ecored.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun addUser(user: User) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        val document = db.collection("users").document(userId).get().await()
        return document.toObject(User::class.java)
    }

    suspend fun addCreditCard(userId: String, creditCard: CreditCard) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(User::class.java)
            val updatedCreditCards = user?.creditCards?.toMutableList() ?: mutableListOf()
            updatedCreditCards.add(creditCard)
            transaction.update(userRef, "creditCards", updatedCreditCards)
        }.await()
    }

    suspend fun addAddress(userId: String, address: Address) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(User::class.java)
            val updatedAddresses = user?.addresses?.toMutableList() ?: mutableListOf()
            updatedAddresses.add(address)
            transaction.update(userRef, "addresses", updatedAddresses)
        }.await()
    }


    suspend fun deleteAddress(userId: String, addressTitle: String) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(User::class.java)
            val updatedAddresses = user?.addresses?.toMutableList() ?: mutableListOf()
            updatedAddresses.removeAll { it.addressTitle == addressTitle }
            transaction.update(userRef, "addresses", updatedAddresses)
        }.await()
    }

    suspend fun deleteCreditCard(userId: String, cardTitle: String) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(User::class.java)
            val updatedCreditCards = user?.creditCards?.toMutableList() ?: mutableListOf()
            updatedCreditCards.removeAll { it.cardTitle == cardTitle }
            transaction.update(userRef, "creditCards", updatedCreditCards)
        }.await()
    }

    suspend fun addOrder(userId: String, order: Order) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val user = snapshot.toObject(User::class.java)
            val updatedOrders = user?.orders?.toMutableList() ?: mutableListOf()
            updatedOrders.add(order)
            transaction.update(userRef, "orders", updatedOrders)
            transaction.update(userRef, "cart", emptyList<CartItem>()) // Clear the cart after order success
        }.await()
    }
    suspend fun getOrderHistory(userId: String): List<Order> {
        val snapshot = db.collection("users")
            .document(userId)
            .get()
            .await()
        val user = snapshot.toObject(User::class.java)
        return user?.orders?.reversed() ?: emptyList()
    }

}