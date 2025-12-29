package com.smartkitch.app.domain.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<FirebaseUser?>
    suspend fun login(email: String, password: String): Result<FirebaseUser>
    suspend fun register(email: String, password: String): Result<FirebaseUser>
    fun logout()
    fun getCurrentUserId(): String?
    suspend fun sendEmailVerification(): Result<Unit>
    suspend fun reloadUser(): Result<Unit>
    fun isEmailVerified(): Boolean
    suspend fun signInAnonymously(): Result<FirebaseUser>
    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun changePassword(newPassword: String): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
    fun getConnectedProviders(): List<String>
    suspend fun linkWithLine(idToken: String): Result<FirebaseUser>
    suspend fun resetPassword(email: String): Result<Unit>
}
