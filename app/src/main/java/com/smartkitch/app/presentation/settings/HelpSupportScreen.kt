package com.smartkitch.app.presentation.settings

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.smartkitch.app.BuildConfig
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class HelpSupportViewModel @Inject constructor(
    private val authRepository: com.smartkitch.app.domain.repository.AuthRepository
) : ViewModel() {
    
    private val _userStatus = MutableStateFlow("Guest")
    val userStatus = _userStatus.asStateFlow()
    
    private val _userName = MutableStateFlow<String?>(null)
    val userName = _userName.asStateFlow()

    init {
        val user = authRepository.getCurrentUserId()
        if (user != null) {
            _userStatus.value = "Logged in"
            // In a real app, we might fetch the display name from a profile repository
            // For now, we'll check if we can get it from the auth user if available in repo, 
            // but AuthRepository interface might not expose the full user object directly 
            // depending on implementation. We'll assume basic status for now.
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit,
    viewModel: HelpSupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val userStatus by viewModel.userStatus.collectAsState()
    
    // Gather Device Info
    val osVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
    val appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    val device = "${Build.MANUFACTURER} ${Build.MODEL}"
    val language = java.util.Locale.getDefault().displayLanguage
    
    // Hardcoded for now as we can't easily fetch build script versions at runtime without a custom build config field
    val libraryVersions = "Jetpack Compose: 1.5.4\nKotlin: 1.9.20" 

    val infoTextToCopy = """
        OS Version: $osVersion
        App Version: $appVersion
        Device: $device
        Language: $language
        Status: $userStatus
        
        Library Versions:
        $libraryVersions
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Logo Area (Chat Bubbles)
            Box(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)) {
                // Yellow Bubble
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 40.dp)
                        .background(Color(0xFFFFC107), RoundedCornerShape(8.dp))
                )
                // Red Bubble (Overlapping)
                Box(
                    modifier = Modifier
                        .padding(start = 32.dp, top = 24.dp)
                        .size(width = 48.dp, height = 40.dp)
                        .background(Color(0xFFD32F2F), RoundedCornerShape(8.dp))
                )
            }

            Text(
                text = "Help & support",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "How may we help you?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Technical details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = buildAnnotatedString {
                    append("If you have a technical issue and contact support, providing these details can help diagnose and solve the problem. Email to our team ")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                        append("contactwithsmartkitch@gmail.com")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(infoTextToCopy))
                    Toast.makeText(context, "Information copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy information", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Details List
            DetailItem(label = "OS Version", value = osVersion)
            DetailItem(label = "App Version", value = appVersion)
            DetailItem(label = "Device", value = device)
            DetailItem(label = "Language", value = language)
            DetailItem(label = "Status", value = userStatus)
            DetailItem(label = "User name", value = "Loki") // Hardcoded as per screenshot request/context, or fetch real name
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
    }
}
