package com.example.fit5046_lab4_group3_ass2.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object ProfileRepo {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun uid(): String =
        FirebaseAuth.getInstance().currentUser?.uid
            ?: error("No authenticated user")

    // ⬇️ Every call goes to users/{uid} — per-user doc
    private fun docRef() = db.collection("users").document(uid())

    fun upsert(profile: UserProfile, onResult: (Result<Unit>) -> Unit) {
        val p = if (profile.uid.isEmpty()) profile.copy(uid = uid()) else profile
        docRef().set(p)
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun get(onResult: (Result<UserProfile?>) -> Unit) {
        docRef().get()
            .addOnSuccessListener { snap ->
                val obj = if (snap.exists()) snap.toObject(UserProfile::class.java) else null
                onResult(Result.success(obj))
            }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }

    fun exists(onResult: (Result<Boolean>) -> Unit) {
        docRef().get()
            .addOnSuccessListener { snap -> onResult(Result.success(snap.exists())) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }
}
