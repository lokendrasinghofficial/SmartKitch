package com.smartkitch.app.presentation.recipe

import android.content.Intent
import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import java.util.regex.Pattern

@Composable
fun CookingModeScreen(
    recipe: com.smartkitch.app.data.model.Recipe,
    initialVoiceEnabled: Boolean = true,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current
    
    // TTS State
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsEnabled by remember { mutableStateOf(initialVoiceEnabled) }
    
    // Initialize TTS
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val instructions = recipe.instructions
    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = instructions.getOrElse(currentStepIndex) { "" }

    // Speak when step changes
    LaunchedEffect(currentStepIndex, isTtsEnabled) {
        if (isTtsEnabled && tts != null) {
            tts?.speak(currentStep, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Timer Logic
    var timerDurationSeconds by remember { mutableStateOf(0L) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(0L) }

    // Parse time from step text
    LaunchedEffect(currentStep) {
        val matcher = Pattern.compile("(\\d+)\\s*(min|minute|sec|second)s?").matcher(currentStep)
        if (matcher.find()) {
            val value = matcher.group(1)?.toLongOrNull() ?: 0L
            val unit = matcher.group(2)
            timerDurationSeconds = if (unit?.startsWith("min") == true) value * 60 else value
            timeRemaining = timerDurationSeconds
            isTimerRunning = false // Don't auto-start
        } else {
            timerDurationSeconds = 0
            isTimerRunning = false
        }
    }

    // Timer Countdown
    LaunchedEffect(isTimerRunning, timeRemaining) {
        if (isTimerRunning && timeRemaining > 0) {
            kotlinx.coroutines.delay(1000L)
            timeRemaining--
        } else if (timeRemaining == 0L) {
            isTimerRunning = false
        }
    }

    // YouTube Dialog
    var showYoutubeDialog by remember { mutableStateOf(false) }

    if (showYoutubeDialog) {
        AlertDialog(
            onDismissRequest = { showYoutubeDialog = false },
            title = { Text("Watch Tutorial") },
            text = { Text("Search YouTube for \"${recipe.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/results?search_query=${recipe!!.title} recipe"))
                    context.startActivity(intent)
                    showYoutubeDialog = false
                }) { Text("Open YouTube") }
            },
            dismissButton = {
                TextButton(onClick = { showYoutubeDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val screenWidth = size.width
                        if (offset.x < screenWidth / 2) {
                            // Left Tap: Previous Step
                            if (currentStepIndex > 0) currentStepIndex--
                        } else {
                            // Right Tap: Next Step
                            if (currentStepIndex < instructions.size - 1) currentStepIndex++
                        }
                    }
                )
            }
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            
            Row {
                IconButton(onClick = { isTtsEnabled = !isTtsEnabled }) {
                    Icon(
                        imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = "Toggle TTS"
                    )
                }
                IconButton(onClick = { showYoutubeDialog = true }) {
                    Icon(Icons.Default.SmartDisplay, contentDescription = "YouTube", tint = Color.Red)
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Step ${currentStepIndex + 1} of ${instructions.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = currentStep,
                style = MaterialTheme.typography.headlineMedium.copy(
                    lineHeight = 40.sp
                ),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Timer UI
            if (timerDurationSeconds > 0) {
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = { isTimerRunning = !isTimerRunning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(72.dp) // Bigger button height
                        .fillMaxWidth(0.7f) // Wider button
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp) // Bigger icon
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (timeRemaining > 0) formatTime(timeRemaining) else "Timer Done!",
                        style = MaterialTheme.typography.headlineSmall // Bigger text
                    )
                }
            }
        }

        // Bottom Progress Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            val progress by animateFloatAsState(
                targetValue = (currentStepIndex + 1).toFloat() / instructions.size,
                label = "progress"
            )
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
