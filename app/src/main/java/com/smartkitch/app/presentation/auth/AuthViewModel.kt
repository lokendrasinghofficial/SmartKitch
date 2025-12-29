package com.smartkitch.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.smartkitch.app.domain.repository.ProfileRepository

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser = authRepository.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                if (authRepository.isEmailVerified()) {
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Unverified
                }
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = when (exception) {
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No user found create your account"
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Wrong Account Details."
                    else -> exception?.message ?: "Login failed"
                }
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(email, password)
            if (result.isSuccess) {
                // Send verification email immediately
                val verificationResult = authRepository.sendEmailVerification()
                if (verificationResult.isFailure) {
                    android.util.Log.e("AuthViewModel", "Failed to send verification email", verificationResult.exceptionOrNull())
                } else {
                    android.util.Log.d("AuthViewModel", "Verification email sent successfully")
                }
                _uiState.value = AuthUiState.Unverified
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            authRepository.sendEmailVerification()
        }
    }

    fun checkVerificationStatus() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.reloadUser()
            if (authRepository.isEmailVerified()) {
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Unverified
            }
        }
    }

    fun loginWithLine(result: com.linecorp.linesdk.auth.LineLoginResult) {
        viewModelScope.launch {
            if (result.responseCode == com.linecorp.linesdk.LineApiResponseCode.SUCCESS) {
                _uiState.value = AuthUiState.Loading
                val authResult = authRepository.signInAnonymously()
                if (authResult.isSuccess) {
                    val firebaseUser = authResult.getOrNull()
                    if (firebaseUser != null) {
                        // Extract LINE Profile Data
                        val lineProfile = result.lineProfile
                        val name = lineProfile?.displayName ?: ""
                        val email = result.lineIdToken?.email ?: ""
                        
                        val pictureUrl = lineProfile?.pictureUrl?.toString()
                        
                        // Create UserProfile
                        val userProfile = com.smartkitch.app.data.model.UserProfile(
                            id = firebaseUser.uid,
                            name = name,
                            profileImageUrl = pictureUrl
                        )
                        
                        // Save to Firestore
                        try {
                            profileRepository.saveUserProfile(firebaseUser.uid, userProfile)
                            android.util.Log.d("AuthViewModel", "LINE Profile saved: $name")
                        } catch (e: Exception) {
                            android.util.Log.e("AuthViewModel", "Failed to save LINE profile", e)
                        }
                    }
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("Firebase Login Failed: ${authResult.exceptionOrNull()?.message}")
                }
            } else {
                _uiState.value = AuthUiState.Error("LINE Login Failed: ${result.errorData.message}")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.loginWithGoogle(idToken)
            if (result.isSuccess) {
                // Check if we need to save profile data (optional, but good for consistency)
                // GoogleAuthProvider usually returns name and photoUrl in the FirebaseUser object directly
                val user = result.getOrNull()
                if (user != null) {
                     val userProfile = com.smartkitch.app.data.model.UserProfile(
                        id = user.uid,
                        name = user.displayName ?: "",
                        profileImageUrl = user.photoUrl?.toString()
                    )
                    try {
                        profileRepository.saveUserProfile(user.uid, userProfile)
                    } catch (e: Exception) {
                         android.util.Log.e("AuthViewModel", "Failed to save Google profile", e)
                    }
                }
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
            }
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signInAnonymously()
            if (result.isSuccess) {
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Anonymous sign-in failed")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.resetPassword(email)
            if (result.isSuccess) {
                _uiState.value = AuthUiState.PasswordResetSent(email)
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState.Initial
    }
    
    fun resetState() {
        _uiState.value = AuthUiState.Initial
    }
}

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    object Unverified : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class PasswordResetSent(val email: String) : AuthUiState()
}
