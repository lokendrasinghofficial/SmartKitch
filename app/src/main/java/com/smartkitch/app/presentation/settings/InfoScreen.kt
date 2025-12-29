package com.smartkitch.app.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class InfoBlock {
    data class Header(val text: String) : InfoBlock()
    data class Body(val text: String) : InfoBlock()
    data class Bullet(val text: String) : InfoBlock()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    title: String,
    content: List<InfoBlock>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp) // Increased horizontal padding for better readability
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            content.forEach { block ->
                when (block) {
                    is InfoBlock.Header -> {
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                    }
                    is InfoBlock.Body -> {
                        Text(
                            text = block.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 8.dp),
                            lineHeight = 24.sp
                        )
                    }
                    is InfoBlock.Bullet -> {
                        Text(
                            text = "• ${block.text}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 4.dp, start = 8.dp),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

object InfoContent {
    const val TYPE_ABOUT = "about"
    const val TYPE_TERMS = "terms"

    fun getContent(type: String): Pair<String, List<InfoBlock>> {
        return when (type) {
            TYPE_ABOUT -> "About SmartKitch" to aboutContent
            TYPE_TERMS -> "Terms & Privacy Policy" to termsContent
            else -> "Info" to emptyList()
        }
    }

    private val aboutContent = listOf(
        InfoBlock.Body("SmartKitch is an AI-powered smart kitchen assistant designed to help users:"),
        InfoBlock.Bullet("Manage kitchen inventory easily"),
        InfoBlock.Bullet("Reduce food waste using expiry-based recommendations"),
        InfoBlock.Bullet("Discover recipes using available ingredients"),
        InfoBlock.Bullet("Create and share shopping lists with family members"),
        InfoBlock.Bullet("Cook smarter with step-by-step AI guidance"),
        InfoBlock.Body("This app is built by a student of National Taipei University (NTPU) Loki with a focus on households and students living in dormitories, where managing food efficiently and avoiding waste is especially important."),
        InfoBlock.Body("Our mission is to make everyday cooking simple, intelligent, and waste-free."),
        InfoBlock.Body("SmartKitch is developed using modern Android technologies with a strong emphasis on usability, performance, and user privacy.")
    )

    private val termsContent = listOf(
        InfoBlock.Header("Privacy Policy"),
        InfoBlock.Body("SmartKitch respects your privacy and is committed to protecting your personal data."),
        
        InfoBlock.Header("Information We Collect"),
        InfoBlock.Bullet("Account information such as email (for authentication)"),
        InfoBlock.Bullet("Inventory and shopping list data entered by the user"),
        InfoBlock.Bullet("App usage data to improve features and performance"),
        
        InfoBlock.Header("How We Use Your Data"),
        InfoBlock.Bullet("To provide personalized recipe recommendations"),
        InfoBlock.Bullet("To manage inventory and expiry alerts"),
        InfoBlock.Bullet("To enable features like shopping list sharing"),
        InfoBlock.Bullet("To improve app functionality and user experience"),
        
        InfoBlock.Header("Data Storage & Security"),
        InfoBlock.Bullet("User data is securely stored using trusted cloud services"),
        InfoBlock.Bullet("We do not sell or share personal data with third parties"),
        InfoBlock.Bullet("All authentication is handled securely via trusted providers"),
        
        InfoBlock.Header("User Control"),
        InfoBlock.Bullet("You can edit or delete your data at any time"),
        InfoBlock.Bullet("You can delete your account permanently from the app settings"),
        
        InfoBlock.Header("Terms & Conditions"),
        InfoBlock.Body("By using SmartKitch, you agree to the following terms:"),
        InfoBlock.Bullet("SmartKitch is provided for personal, non-commercial use"),
        InfoBlock.Bullet("Recipe suggestions are generated using AI and should be followed at the user’s discretion"),
        InfoBlock.Bullet("The app does not guarantee nutritional, medical, or dietary outcomes"),
        InfoBlock.Bullet("Users are responsible for verifying food safety and expiry information"),
        InfoBlock.Bullet("Misuse, abuse, or unauthorized access may result in account suspension"),
        
        InfoBlock.Body("SmartKitch may update features or policies from time to time to improve the service.")
    )
}
