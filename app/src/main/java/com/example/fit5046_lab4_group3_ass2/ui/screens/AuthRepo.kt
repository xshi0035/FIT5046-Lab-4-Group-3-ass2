package com.example.fit5046_lab4_group3_ass2.ui.screens

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

object AuthRepo {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun currentUser() = auth.currentUser

    fun signUp(
        email: String,
        password: String,
        onResult: (Result<AuthResult>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { t: Task<AuthResult> ->
                if (t.isSuccessful) onResult(Result.success(t.result))
                else onResult(Result.failure(t.exception ?: Exception("Sign up failed")))
            }
    }

    fun signIn(
        email: String,
        password: String,
        onResult: (Result<AuthResult>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { t: Task<AuthResult> ->
                if (t.isSuccessful) onResult(Result.success(t.result))
                else onResult(Result.failure(t.exception ?: Exception("Sign in failed")))
            }
    }

    fun signOut() = auth.signOut()
}