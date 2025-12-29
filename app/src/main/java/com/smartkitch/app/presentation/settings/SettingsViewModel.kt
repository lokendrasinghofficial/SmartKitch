package com.smartkitch.app.presentation.settings

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
import javax.inject.Inject

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val userEmail: StateFlow<String?> = authRepository.currentUser
        .map { it?.email }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Notification Settings
    private val _expiryAlerts = MutableStateFlow(true)
    val expiryAlerts: StateFlow<Boolean> = _expiryAlerts.asStateFlow()

    private val _aiSuggestions = MutableStateFlow(true)
    val aiSuggestions: StateFlow<Boolean> = _aiSuggestions.asStateFlow()

    // Cooking Preferences
    private val _preferredCuisine = MutableStateFlow("Italian")
    val preferredCuisine: StateFlow<String> = _preferredCuisine.asStateFlow()

    private val _spiceLevel = MutableStateFlow(0.5f) // 0.0 to 1.0
    val spiceLevel: StateFlow<Float> = _spiceLevel.asStateFlow()

    private val _cookingTime = MutableStateFlow("30 min")
    val cookingTime: StateFlow<String> = _cookingTime.asStateFlow()

    private val _voiceAssistant = MutableStateFlow(false)
    val voiceAssistant: StateFlow<Boolean> = _voiceAssistant.asStateFlow()

    // Inventory Settings
    private val _autoRemoveExpired = MutableStateFlow(false)
    val autoRemoveExpired: StateFlow<Boolean> = _autoRemoveExpired.asStateFlow()

    // Language & Region
    private val _appLanguage = MutableStateFlow("English")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _region = MutableStateFlow("United States")
    val region: StateFlow<String> = _region.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            profileRepository.getUserProfile(userId).collect { profile ->
                _userProfile.value = profile
                profile?.let {
                    _expiryAlerts.value = it.expiryAlerts
                    _aiSuggestions.value = it.aiSuggestions
                    _preferredCuisine.value = it.preferredCuisine
                    _spiceLevel.value = it.spiceLevel
                    _cookingTime.value = it.cookingTime
                    _voiceAssistant.value = it.voiceAssistantEnabled
                    _autoRemoveExpired.value = it.autoRemoveExpired
                    _appLanguage.value = it.appLanguage
                    _region.value = it.region
                }
            }
        }
    }

    private fun saveSettings() {
        val currentProfile = _userProfile.value
        
        val updatedProfile = if (currentProfile != null) {
            currentProfile.copy(
                expiryAlerts = _expiryAlerts.value,
                aiSuggestions = _aiSuggestions.value,
                preferredCuisine = _preferredCuisine.value,
                spiceLevel = _spiceLevel.value,
                cookingTime = _cookingTime.value,
                voiceAssistantEnabled = _voiceAssistant.value,
                autoRemoveExpired = _autoRemoveExpired.value,
                appLanguage = _appLanguage.value,
                region = _region.value
            )
        } else {
            // Create new profile if it doesn't exist
            UserProfile(
                id = userId,
                expiryAlerts = _expiryAlerts.value,
                aiSuggestions = _aiSuggestions.value,
                preferredCuisine = _preferredCuisine.value,
                spiceLevel = _spiceLevel.value,
                cookingTime = _cookingTime.value,
                voiceAssistantEnabled = _voiceAssistant.value,
                autoRemoveExpired = _autoRemoveExpired.value,
                appLanguage = _appLanguage.value,
                region = _region.value
            )
        }
        
        viewModelScope.launch {
            profileRepository.saveUserProfile(userId, updatedProfile)
            // If we created a new profile, update the local state so subsequent saves work efficiently
            if (currentProfile == null) {
                _userProfile.value = updatedProfile
            }
        }
    }

    fun toggleExpiryAlerts(enabled: Boolean) { 
        _expiryAlerts.value = enabled 
        saveSettings()
    }
    fun toggleAiSuggestions(enabled: Boolean) { 
        _aiSuggestions.value = enabled 
        saveSettings()
    }
    fun setPreferredCuisine(cuisine: String) { 
        _preferredCuisine.value = cuisine 
        saveSettings()
    }
    fun setSpiceLevel(level: Float) { 
        _spiceLevel.value = level 
        saveSettings()
    }
    fun setCookingTime(time: String) { 
        _cookingTime.value = time 
        saveSettings()
    }
    fun toggleVoiceAssistant(enabled: Boolean) { 
        _voiceAssistant.value = enabled 
        saveSettings()
    }
    fun toggleAutoRemoveExpired(enabled: Boolean) { 
        _autoRemoveExpired.value = enabled 
        saveSettings()
    }
    fun setAppLanguage(language: String) { 
        _appLanguage.value = language 
        saveSettings()
    }
    fun setRegion(region: String) { 
        _region.value = region 
        saveSettings()
    }

    fun logout() {
        authRepository.logout()
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.changePassword(newPassword)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to change password")
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to delete account")
            }
        }
    }

    private val _connectedProviders = MutableStateFlow<List<String>>(emptyList())
    val connectedProviders: StateFlow<List<String>> = _connectedProviders.asStateFlow()

    init {
        loadUserProfile()
        loadConnectedProviders()
    }

    private fun loadConnectedProviders() {
        _connectedProviders.value = authRepository.getConnectedProviders()
    }

    fun linkWithLine(loginResult: com.linecorp.linesdk.auth.LineLoginResult, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val idToken = loginResult.lineIdToken?.rawString
            if (idToken != null) {
                val result = authRepository.linkWithLine(idToken)
                if (result.isSuccess) {
                    loadConnectedProviders() // Refresh the list
                    onSuccess()
                } else {
                    onError(result.exceptionOrNull()?.message ?: "Failed to link LINE account")
                }
            } else {
                onError("Failed to get LINE ID Token: ${loginResult.errorData.message}")
            }
        }
    }
}
