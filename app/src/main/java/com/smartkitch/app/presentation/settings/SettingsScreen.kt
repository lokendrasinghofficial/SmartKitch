package com.smartkitch.app.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToInfo: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val expiryAlerts by viewModel.expiryAlerts.collectAsState()
    val aiSuggestions by viewModel.aiSuggestions.collectAsState()
    val preferredCuisine by viewModel.preferredCuisine.collectAsState()
    val spiceLevel by viewModel.spiceLevel.collectAsState()
    val cookingTime by viewModel.cookingTime.collectAsState()
    val voiceAssistant by viewModel.voiceAssistant.collectAsState()
    val autoRemoveExpired by viewModel.autoRemoveExpired.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val region by viewModel.region.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()

    val cuisines = listOf(
        CuisineOption("Taiwanese", "🏯"),
        CuisineOption("Chinese", "🥘"),
        CuisineOption("Italian", "🍕"),
        CuisineOption("Indian", "🕌"), // Indian at 4th place
        CuisineOption("South Asian", "🌴"),
        CuisineOption("Vietnamese", "🍜"),
        CuisineOption("Thai", "🌿"),
        CuisineOption("Filipino", "☀️"),
        CuisineOption("Continental", "🍽️")
    )
    var showCuisineDialog by remember { mutableStateOf(false) }

    val regions = listOf(
        RegionOption("Taiwan", "🇹🇼"),
        RegionOption("Vietnam", "🇻🇳"),
        RegionOption("Macao", "🇲🇴"),
        RegionOption("Hong Kong", "🇭🇰"),
        RegionOption("China", "🇨🇳"),
        RegionOption("Thailand", "🇹🇭"),
        RegionOption("Philippines", "🇵🇭"),
        RegionOption("India", "🇮🇳"),
        RegionOption("Bangladesh", "🇧🇩"),
        RegionOption("Sri Lanka", "🇱🇰"),
        RegionOption("Pakistan", "🇵🇰"),
        RegionOption("Singapore", "🇸🇬"),
        RegionOption("Indonesia", "🇮🇩"),
        RegionOption("United States", "🇺🇸"),
        RegionOption("United Kingdom", "🇬🇧"),
        RegionOption("Canada", "🇨🇦"),
        RegionOption("Australia", "🇦🇺"),
        RegionOption("Germany", "🇩🇪"),
        RegionOption("Poland", "🇵🇱"),
        RegionOption("France", "🇫🇷"),
        RegionOption("Italy", "🇮🇹"),
        RegionOption("Japan", "🇯🇵"),
        RegionOption("South Korea", "🇰🇷"),
        RegionOption("Brazil", "🇧🇷"),
        RegionOption("New Zealand", "🇳🇿"),
        RegionOption("Russia", "🇷🇺"),
        RegionOption("Africa", "🌍"),
        RegionOption("Asia", "🌏"),
        RegionOption("South Asia", "🌏"),
        RegionOption("Europe", "🇪🇺")
    )
    var showRegionDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (showCuisineDialog) {
        CuisineSelectionDialog(
            options = cuisines,
            selectedOption = preferredCuisine,
            onOptionSelected = viewModel::setPreferredCuisine,
            onDismissRequest = { showCuisineDialog = false }
        )
    }

    if (showRegionDialog) {
        RegionSelectionDialog(
            options = regions,
            selectedOption = region,
            onOptionSelected = viewModel::setRegion,
            onDismissRequest = { showRegionDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settings", 
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    userProfile = userProfile,
                    email = userEmail,
                    onEditProfile = onEditProfile
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
            // Notifications Section
            item {
                SettingsSection(title = "Notifications") {
                    SettingsToggleRow(
                        title = "Expiry Alerts",
                        subtitle = "Get notified before items expire",
                        checked = expiryAlerts,
                        onCheckedChange = viewModel::toggleExpiryAlerts
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsToggleRow(
                        title = "AI Recipe Suggestions",
                        subtitle = "Daily recommendations based on inventory",
                        checked = aiSuggestions,
                        onCheckedChange = viewModel::toggleAiSuggestions
                    )
                }
            }

            // Cooking Preferences
            item {
                SettingsSection(title = "Cooking Preferences") {
                    SettingsDropdownRow(
                        title = "Preferred Cuisine",
                        value = preferredCuisine,
                        onClick = { showCuisineDialog = true }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = "Spice Level",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = spiceLevel,
                            onValueChange = viewModel::setSpiceLevel,
                            steps = 1, // Low, Medium, High
                            valueRange = 0f..1f
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Low", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("Medium", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("High", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                    
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            text = "Cooking Time Preference",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Under 15 min", "30 min", "1 hour").forEach { time ->
                                FilterChip(
                                    selected = cookingTime == time,
                                    onClick = { viewModel.setCookingTime(time) },
                                    label = { Text(time) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsToggleRow(
                        title = "Voice Assistant",
                        subtitle = "Hands-free cooking instructions",
                        checked = voiceAssistant,
                        onCheckedChange = viewModel::toggleVoiceAssistant
                    )
                }
            }

            // Inventory & Storage
            item {
                SettingsSection(title = "Inventory & Storage") {
                    SettingsToggleRow(
                        title = "Auto-remove Expired Items",
                        subtitle = "Automatically move expired items to trash",
                        checked = autoRemoveExpired,
                        onCheckedChange = viewModel::toggleAutoRemoveExpired
                    )
                }
            }

            // Language & Region
            item {
                SettingsSection(title = "Language & Region") {
                    SettingsDropdownRow(
                        title = "App Language",
                        value = appLanguage,
                        onClick = { /* Open selection dialog */ }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsDropdownRow(
                        title = "Region",
                        value = region,
                        onClick = { showRegionDialog = true }
                    )
                }
            }

            // Security and Account
            item {
                SettingsSection(title = "Security and Account") {
                    SettingsActionRow(
                        title = "Change Password",
                        icon = Icons.Default.Lock,
                        onClick = { showChangePasswordDialog = true }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    
                    // Connected Accounts
                    val connectedProviders by viewModel.connectedProviders.collectAsState()
                    var showConnectDialog by remember { mutableStateOf(false) }
                    val context = androidx.compose.ui.platform.LocalContext.current
                    
                    val lineLinkLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                    ) { result ->
                        val lineLoginResult = com.linecorp.linesdk.auth.LineLoginApi.getLoginResultFromIntent(result.data)
                        viewModel.linkWithLine(
                            loginResult = lineLoginResult,
                            onSuccess = {
                                showConnectDialog = false
                                android.widget.Toast.makeText(context, "LINE account linked successfully", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                android.widget.Toast.makeText(context, "Failed to link LINE: $error", android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConnectDialog = true }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connected Accounts",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (connectedProviders.isEmpty()) {
                                Text(
                                    text = "Tap to connect",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (connectedProviders.contains("google.com")) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = com.smartkitch.app.R.drawable.ic_google_logo),
                                            contentDescription = "Google",
                                            tint = Color.Unspecified, // Use original colors
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    if (connectedProviders.contains("facebook.com")) {
                                        Icon(Icons.Default.Face, contentDescription = "Facebook", tint = Color(0xFF1877F2), modifier = Modifier.size(16.dp))
                                    }
                                    if (connectedProviders.contains("oidc.line")) {
                                        // Using ChatBubble as a placeholder for LINE, tinted LINE Green
                                        Icon(Icons.Default.ChatBubble, contentDescription = "Line", tint = Color(0xFF00C300), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.LightGray
                        )
                    }
                    
                    if (showConnectDialog) {
                        AlertDialog(
                            onDismissRequest = { showConnectDialog = false },
                            title = { Text("Connect Accounts") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (!connectedProviders.contains("google.com")) {
                                        Button(
                                            onClick = { /* Implement Google Link */ },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                                        ) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Connect with Google", color = Color.Black)
                                        }
                                    }
                                    // Line
                                    Button(
                                        onClick = {
                                            try {
                                                val loginIntent = com.linecorp.linesdk.auth.LineLoginApi.getLoginIntent(
                                                    context,
                                                    context.getString(com.smartkitch.app.R.string.line_channel_id),
                                                    com.linecorp.linesdk.auth.LineAuthenticationParams.Builder()
                                                        .scopes(listOf(com.linecorp.linesdk.Scope.PROFILE, com.linecorp.linesdk.Scope("openid"), com.linecorp.linesdk.Scope.OC_EMAIL))
                                                        .build()
                                                )
                                                lineLinkLauncher.launch(loginIntent)
                                            } catch (e: Exception) {
                                                android.util.Log.e("SettingsScreen", "Error starting LINE link", e)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C300)) // Line Green
                                    ) {
                                        Text("Connect with Line", color = Color.White)
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showConnectDialog = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    
                    SettingsActionRow(
                        title = "Delete Account",
                        icon = Icons.Outlined.Delete,
                        onClick = { showDeleteAccountDialog = true },
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }


            
            // About & Legal
            item {
                SettingsSection(title = "About") {
                    SettingsActionRow(
                        title = "Terms & Privacy Policy",
                        icon = Icons.Default.Info,
                        onClick = { onNavigateToInfo(InfoContent.TYPE_TERMS) }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsActionRow(
                        title = "About SmartKitch",
                        icon = Icons.Default.Info,
                        onClick = { onNavigateToInfo(InfoContent.TYPE_ABOUT) }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsActionRow(
                        title = "Help & Support",
                        icon = Icons.Default.Support, // Using Support icon, or ChatBubble if Support not available
                        onClick = { onNavigateToInfo("help_support") } 
                    )
                }
            }

            // App Version
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SmartKitch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Version: 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Build: 1",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Platform: Android",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = "Release: 15 December 2025",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }


    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onConfirm = { newPassword ->
                viewModel.changePassword(
                    newPassword = newPassword,
                    onSuccess = {
                        showChangePasswordDialog = false
                        android.widget.Toast.makeText(context, "Password Changed Successfully", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onError = { error ->
                        android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { showChangePasswordDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onConfirm = {
                viewModel.deleteAccount(
                    onSuccess = {
                        showDeleteAccountDialog = false
                        onLogout() // Log out after deletion
                    },
                    onError = { error ->
                        showDeleteAccountDialog = false
                        android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

data class RegionOption(val name: String, val icon: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelectionDialog(
    options: List<RegionOption>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFEBF2FA)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Your Region",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(options.size) { index ->
                        val option = options[index]
                        val isSelected = option.name == selectedOption
                        
                        Card(
                            onClick = { onOptionSelected(option.name) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFD0E4FF) else Color.White
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4285F4)) else null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.icon,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1976D2))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply Filters", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userProfile: com.smartkitch.app.data.model.UserProfile?,
    email: String?,
    onEditProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (userProfile?.profileImageUrl != null) {
            AsyncImage(
                model = userProfile.profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userProfile?.name ?: "Guest User",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email ?: "No Email",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            if (!userProfile?.dietRestrictions.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = userProfile?.dietRestrictions?.firstOrNull() ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        TextButton(onClick = onEditProfile) {
            Text("Edit")
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)), // Light gray background for cards
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun SettingsDropdownRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color.Black,
    iconTint: Color = Color.Gray
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}

data class CuisineOption(val name: String, val icon: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuisineSelectionDialog(
    options: List<CuisineOption>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    // Using a full-screen dialog or a large modal bottom sheet style
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFEBF2FA) // Light blueish background like screenshot
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Your Cuisine",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(options.size) { index ->
                        val option = options[index]
                        val isSelected = option.name == selectedOption
                        
                        Card(
                            onClick = { onOptionSelected(option.name) }, // Select immediately or wait for apply? Screenshot has "Apply Filters". Let's update local state first.
                            // Wait, for "Apply Filters" to work, we need local state in the dialog.
                            // But for now, let's keep it simple: clicking updates immediately, "Apply" just closes. 
                            // Or better: clicking updates a local state, Apply commits it.
                            // Let's implement local state.
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFD0E4FF) else Color.White // Light blue if selected
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF4285F4)) else null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option.icon,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                RadioButton(
                                    selected = isSelected,
                                    onClick = null, // Handled by Card click
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1976D2))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onOptionSelected("") }) { // Clear selection
                        Text("Clear All", color = Color(0xFF1976D2))
                    }
                    
                    Button(
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply Filters", color = Color.White)
                    }
                }
            }
        }
    }
}
@Composable
fun ChangePasswordDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("New Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password.length < 6) {
                        error = "Password must be at least 6 characters"
                    } else if (password != confirmPassword) {
                        error = "Passwords do not match"
                    } else {
                        onConfirm(password)
                    }
                }
            ) {
                Text("Change")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


