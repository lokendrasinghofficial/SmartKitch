package com.smartkitch.app.presentation.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkitch.app.data.model.FoodItem
import com.smartkitch.app.domain.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val authRepository: com.smartkitch.app.domain.repository.AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Initial)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun scanImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ScanUiState.Loading
            try {
                val bitmap = getBitmapFromUri(uri)
                if (bitmap != null) {
                    val items = repository.scanImage(bitmap)
                    _uiState.value = ScanUiState.Success(items)
                } else {
                    _uiState.value = ScanUiState.Error("Failed to load image")
                }
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun saveItems(items: List<FoodItem>, onComplete: () -> Unit) {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _uiState.value = ScanUiState.Loading
            try {
                items.forEach { item ->
                    repository.addItem(userId, item)
                }
                onComplete()
            } catch (e: Exception) {
                _uiState.value = ScanUiState.Error("Failed to save items: ${e.message}")
            }
        }
    }
    
    fun updateItemQuantity(item: FoodItem, newQuantity: Double) {
        val currentState = _uiState.value
        if (currentState is ScanUiState.Success) {
            val updatedList = currentState.items.map {
                if (it == item) it.copy(quantity = newQuantity.coerceAtLeast(0.0)) else it
            }
            _uiState.value = ScanUiState.Success(updatedList)
        }
    }

    fun deleteItem(item: FoodItem) {
        val currentState = _uiState.value
        if (currentState is ScanUiState.Success) {
            val updatedList = currentState.items.toMutableList().apply {
                remove(item)
            }
            _uiState.value = ScanUiState.Success(updatedList)
        }
    }

    fun resetState() {
        _uiState.value = ScanUiState.Initial
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

sealed class ScanUiState {
    object Initial : ScanUiState()
    object Loading : ScanUiState()
    data class Success(val items: List<FoodItem>) : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}
