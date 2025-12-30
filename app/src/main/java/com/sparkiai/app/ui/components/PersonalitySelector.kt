package com.sparkiai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkiai.app.model.AIPersonality
import com.sparkiai.app.model.UserSubscription

// Free personalities available without premium
private val FREE_PERSONALITIES = setOf("default", "music_composer")

@Composable
fun PersonalitySelectorDialog(
    personalities: List<AIPersonality>,
    currentPersonality: AIPersonality,
    onPersonalitySelected: (AIPersonality) -> Unit,
    onDismiss: () -> Unit,
    subscription: UserSubscription = UserSubscription(),
    onShowUpgrade: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF505050), // Darker gray background for better contrast
        title = {
            Text(
                text = "Choose AI Personality",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .offset(y = (-8).dp)
            ) {
                Text(
                    text = "Select the traits for your AI",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                val shouldPromptUpgrade = !subscription.isPremium

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(personalities) { personality ->
                        val isLocked = !subscription.isPremium && 
                            !FREE_PERSONALITIES.contains(personality.id)
                        
                        PersonalityCard(
                            personality = personality,
                            isSelected = personality.id == currentPersonality.id,
                            isLocked = isLocked,
                            onClick = {
                                if (shouldPromptUpgrade) {
                                    onShowUpgrade()
                                }
                                if (isLocked) {
                                    return@PersonalityCard
                                }
                                onPersonalitySelected(personality)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Close",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
fun PersonalityCard(
    personality: AIPersonality,
    isSelected: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLocked -> Color(0xFFF5F5F5)
                isSelected -> Color(personality.color).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        border = when {
            isSelected -> androidx.compose.foundation.BorderStroke(2.dp, Color(personality.color))
            isLocked -> androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            else -> null
        }
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Personality icon in a consistent box, aligned to top
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .offset(y = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = personality.icon,
                        fontSize = if (personality.id == "ultimate") 44.sp else 36.sp,
                        textAlign = TextAlign.Center,
                        color = if (isLocked) Color.Gray.copy(alpha = 0.5f) else Color.Unspecified
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = personality.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isLocked) {
                            Color.Gray
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = personality.description,
                        fontSize = 13.sp,
                        color = if (isLocked) {
                            Color.Gray.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }

                // Lock icon or checkmark
                when {
                    isLocked -> {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF9C27B0)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    isSelected -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(personality.color),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalityChipRow(
    personalities: List<AIPersonality>,
    currentPersonality: AIPersonality,
    onPersonalityClick: (AIPersonality) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(personalities) { personality ->
            PersonalityChip(
                personality = personality,
                isSelected = personality.id == currentPersonality.id,
                onClick = { onPersonalityClick(personality) }
            )
        }
    }
}

@Composable
fun PersonalityChip(
    personality: AIPersonality,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) {
            Color(personality.color)
        } else {
            Color(personality.color).copy(alpha = 0.2f)
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = personality.name,
                color = if (isSelected) Color.White else Color(personality.color),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}