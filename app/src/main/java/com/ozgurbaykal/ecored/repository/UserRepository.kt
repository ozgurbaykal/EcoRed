package com.ozgurbaykal.ecored.repository


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
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
}