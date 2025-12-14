package com.sparkiai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkiai.app.model.AIPersonality

@Composable
fun PersonalitySelectorDialog(
    personalities: List<AIPersonality>,
    currentPersonality: AIPersonality,
    onPersonalitySelected: (AIPersonality) -> Unit,
    onDismiss: () -> Unit
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

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(personalities) { personality ->
                        PersonalityCard(
                            personality = personality,
                            isSelected = personality.id == currentPersonality.id,
                            onClick = {
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(personality.color).copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(personality.color))
        } else {
            null
        }
    ) {
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
                    textAlign = TextAlign.Center
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = personality.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
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