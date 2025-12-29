package com.smartkitch.app.presentation.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TipsSection(tips: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)), // Light Yellow
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb, // Need to ensure this is imported or use fallback
                    contentDescription = "Tips",
                    tint = Color(0xFFFBC02D) // Darker Yellow
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF57F17)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(top = 4.dp),
                    tint = Color(0xFF4CAF50) // Green
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tips,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ReferenceVideosSection(recipe: com.smartkitch.app.data.model.Recipe) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = Color.Red
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Video Tutorial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        if (recipe.youtubeVideoId != null || recipe.youtubeQuery != null) {
            val videoUrl = if (recipe.youtubeVideoId != null) {
                "https://www.youtube.com/watch?v=${recipe.youtubeVideoId}"
            } else {
                "https://www.youtube.com/results?search_query=${java.net.URLEncoder.encode(recipe.youtubeQuery ?: recipe.title + " recipe", "UTF-8")}"
            }
            
            val thumbnailUrl = if (recipe.youtubeVideoId != null) {
                "https://img.youtube.com/vi/${recipe.youtubeVideoId}/mqdefault.jpg"
            } else {
                null // Use placeholder for search
            }

            VideoThumbnailCard(
                title = if (recipe.youtubeVideoId != null) "Watch Tutorial" else "Search on YouTube",
                thumbnailUrl = thumbnailUrl,
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(videoUrl))
                    context.startActivity(intent)
                }
            )
        } else {
             Text(
                text = "No video available for this recipe.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun VideoThumbnailCard(title: String, thumbnailUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailUrl != null) {
            coil.compose.AsyncImage(
                model = thumbnailUrl,
                contentDescription = "Video Thumbnail",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder background
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE0E0E0))
            )
        }

        // Play Icon Overlay
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Red.copy(alpha = 0.9f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Title Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 2,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
