package com.smartkitch.app.data.repository

import com.smartkitch.app.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                android.util.Log.e("AuthRepository", "sendEmailVerification: currentUser is null")
                return Result.failure(Exception("No current user"))
            }
            user.sendEmailVerification().await()
            android.util.Log.d("AuthRepository", "sendEmailVerification: Email sent")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "sendEmailVerification: Failed", e)
            Result.failure(e)
        }
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.reload()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser
        return user?.isEmailVerified == true || user?.isAnonymous == true
    }

    override suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInAnonymously().await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("Anonymous login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            if (result.user != null) {
                Result.success(result.user!!)
            } else {
                Result.failure(Exception("Google login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun changePassword(newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return Result.failure(Exception("No current user"))
            }
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser
            if (user == null) {
                return Result.failure(Exception("No current user"))
            }
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getConnectedProviders(): List<String> {
        return firebaseAuth.currentUser?.providerData?.map { it.providerId } ?: emptyList()
    }

    override suspend fun linkWithLine(idToken: String): Result<FirebaseUser> {
        return try {
            val provider = com.google.firebase.auth.OAuthProvider.newBuilder("oidc.line")
            val credential = com.google.firebase.auth.OAuthProvider.newCredentialBuilder("oidc.line")
                .setIdToken(idToken)
                .build()

            val user = firebaseAuth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val result = user.linkWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
