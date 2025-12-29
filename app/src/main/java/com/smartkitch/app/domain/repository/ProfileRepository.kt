package com.smartkitch.app.domain.repository

import android.net.Uri
import com.smartkitch.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(userId: String): Flow<UserProfile?>
    suspend fun saveUserProfile(userId: String, profile: UserProfile)
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String
}
