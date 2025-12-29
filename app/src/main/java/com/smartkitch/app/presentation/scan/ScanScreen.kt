package com.smartkitch.app.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.core.content.ContextCompat

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onScanComplete: (List<com.smartkitch.app.data.model.FoodItem>) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    if (hasCameraPermission) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                onPhotoCaptured = { uri ->
                    viewModel.scanImage(uri)
                },
                onError = { exc ->
                    android.widget.Toast.makeText(context, "Error: ${exc.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            )

            // Overlay for Loading and Results
            when (val state = uiState) {
                is ScanUiState.Loading -> {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = com.smartkitch.app.ui.theme.SageGreen)
                    }
                }
                is ScanUiState.Success -> {
                    // Simple overlay to confirm adding items
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Found ${state.items.size} items:",
                                style = MaterialTheme.typography.titleLarge,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                            
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(androidx.compose.ui.graphics.Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                items(state.items.size) { index ->
                                    val item = state.items[index]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = androidx.compose.ui.graphics.Color.Black
                                            )
                                            Text(
                                                text = "${item.quantity} ${item.unit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = androidx.compose.ui.graphics.Color.Gray
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Minus Button
                                            androidx.compose.material3.IconButton(
                                                onClick = { viewModel.updateItemQuantity(item, item.quantity - 1.0) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Decrease",
                                                    tint = com.smartkitch.app.ui.theme.SageGreen
                                                )
                                            }

                                            // Plus Button
                                            androidx.compose.material3.IconButton(
                                                onClick = { viewModel.updateItemQuantity(item, item.quantity + 1.0) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase",
                                                    tint = com.smartkitch.app.ui.theme.SageGreen
                                                )
                                            }

                                            // Delete Button
                                            androidx.compose.material3.IconButton(
                                                onClick = { viewModel.deleteItem(item) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                androidx.compose.material3.Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = androidx.compose.ui.graphics.Color.Red
                                                )
                                            }
                                        }
                                    }
                                    androidx.compose.material3.HorizontalDivider(color = androidx.compose.ui.graphics.Color.LightGray)
                                }
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { viewModel.resetState() },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.Gray)
                                ) {
                                    Text("Retake")
                                }
                                Button(
                                    onClick = {
                                        viewModel.saveItems(state.items) {
                                            onScanComplete(state.items)
                                        }
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.smartkitch.app.ui.theme.SageGreen)
                                ) {
                                    Text("Add to Inventory")
                                }
                            }
                        }
                    }
                }
                is ScanUiState.Error -> {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${state.message}", color = androidx.compose.ui.graphics.Color.White)
                            Button(onClick = { viewModel.resetState() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {}
            }
            
            // Back Button removed as it is now a main tab
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Camera permission is required to scan items.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
fun CameraPreview(
    onPhotoCaptured: (android.net.Uri) -> Unit,
    onError: (androidx.camera.core.ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    
    val previewView = remember { androidx.camera.view.PreviewView(context) }
    val imageCapture = remember { androidx.camera.core.ImageCapture.Builder().build() }
    
    LaunchedEffect(lifecycleOwner) {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                android.util.Log.e("CameraPreview", "Use case binding failed", e)
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }
    
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )
        
        Button(
            onClick = {
                val name = "scan_${System.currentTimeMillis()}.jpg"
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
                        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/KitchApp-Scans")
                    }
                }
                
                val outputOptions = androidx.camera.core.ImageCapture.OutputFileOptions.Builder(
                    context.contentResolver,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).build()
                
                imageCapture.takePicture(
                    outputOptions,
                    androidx.core.content.ContextCompat.getMainExecutor(context),
                    object : androidx.camera.core.ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: androidx.camera.core.ImageCaptureException) {
                            android.util.Log.e("CameraPreview", "Photo capture failed: ${exc.message}", exc)
                            onError(exc)
                        }
                        
                        override fun onImageSaved(output: androidx.camera.core.ImageCapture.OutputFileResults) {
                            output.savedUri?.let { uri ->
                                onPhotoCaptured(uri)
                            }
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        ) {
            Text("Capture")
        }
    }
}
