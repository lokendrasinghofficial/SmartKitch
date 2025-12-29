package com.smartkitch.app.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.data.model.UserProfile
import com.smartkitch.app.domain.repository.AuthRepository
import com.smartkitch.app.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Form State
    var name = MutableStateFlow("")
    var age = MutableStateFlow("")
    var foodPreference = MutableStateFlow("")
    var dietRestrictions = MutableStateFlow<List<String>>(emptyList())
    var profileImageUri = MutableStateFlow<Uri?>(null)
    var profileImageUrl = MutableStateFlow<String?>(null)

    private val _events = kotlinx.coroutines.channels.Channel<ProfileEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        if (userId.isEmpty()) {
            _uiState.value = ProfileUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            profileRepository.getUserProfile(userId).collect { profile ->
                if (profile != null) {
                    name.value = profile.name
                    age.value = if (profile.age > 0) profile.age.toString() else ""
                    foodPreference.value = profile.foodPreference
                    dietRestrictions.value = profile.dietRestrictions
                    profileImageUrl.value = profile.profileImageUrl
                    _uiState.value = ProfileUiState.Success(profile)
                } else {
                    _uiState.value = ProfileUiState.Empty
                }
            }
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        if (userId.isEmpty()) return

        val currentName = name.value
        val currentAge = age.value.toIntOrNull()
        val currentFoodPref = foodPreference.value

        if (currentName.isBlank() || currentAge == null || currentAge <= 0 || currentFoodPref.isBlank()) {
            _uiState.value = ProfileUiState.Error("Please fill all required fields correctly.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                var imageUrl = profileImageUrl.value
                val imageUri = profileImageUri.value

                if (imageUri != null) {
                    imageUrl = profileRepository.uploadProfileImage(userId, imageUri)
                }

                val userProfile = UserProfile(
                    id = userId,
                    name = currentName,
                    age = currentAge,
                    foodPreference = currentFoodPref,
                    dietRestrictions = dietRestrictions.value,
                    profileImageUrl = imageUrl
                )

                profileRepository.saveUserProfile(userId, userProfile)
                _uiState.value = ProfileUiState.Success(userProfile)
                _events.send(ProfileEvent.ProfileSaved)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to save profile")
            }
        }
    }
    
    fun updateProfileImage(uri: Uri) {
        profileImageUri.value = uri
    }
    
    fun toggleDietRestriction(restriction: String) {
        val current = dietRestrictions.value.toMutableList()
        if (current.contains(restriction)) {
            current.remove(restriction)
        } else {
            current.add(restriction)
        }
        dietRestrictions.value = current
    }
}

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    object Empty : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileEvent {
    object ProfileSaved : ProfileEvent()
}
