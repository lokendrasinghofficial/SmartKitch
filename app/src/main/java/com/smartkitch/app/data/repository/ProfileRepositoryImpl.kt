package com.smartkitch.app.data.repository

import android.net.Uri
import com.smartkitch.app.data.model.UserProfile
import com.smartkitch.app.domain.repository.ProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override fun getUserProfile(userId: String): Flow<UserProfile?> {
        return firestore.collection("users")
            .document(userId)
            .collection("profile")
            .document("info")
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(UserProfile::class.java)
            }
    }

    override suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        firestore.collection("users")
            .document(userId)
            .collection("profile")
            .document("info")
            .set(profile)
            .await()
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val storageRef = storage.reference.child("profile_images/$userId/profile.jpg")
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}
