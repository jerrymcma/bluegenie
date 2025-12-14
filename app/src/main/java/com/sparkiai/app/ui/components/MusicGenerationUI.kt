package com.sparkiai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkiai.app.config.FeatureFlags
import com.sparkiai.app.ui.theme.PrimaryBlue
import com.sparkiai.app.model.GeneratedMusic
import com.sparkiai.app.ui.theme.PrimaryBlue
import com.sparkiai.app.utils.MusicUsageStats

/**
 * Generate Music Button - Shows in Music Composer when Lyria is enabled
 */
@Composable
fun GenerateMusicButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    isGenerating: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!FeatureFlags.MusicComposerConfig.SHOW_GENERATE_MUSIC_BUTTON) {
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "music-button-pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.995f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled && !isGenerating,
        modifier = modifier
            .fillMaxWidth(0.78f)
            .height(44.dp)
            .scale(if (!isGenerating) pulse else 1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryBlue,
            disabledContainerColor = Color.Gray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        val sparkleTransition = rememberInfiniteTransition(label = "generate-sparkles")
        val sparkleScale by sparkleTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "generate-sparkle-scale"
        )
        val sparkleAlpha by sparkleTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "generate-sparkle-alpha"
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (!isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = sparkleAlpha),
                            modifier = Modifier.scale(sparkleScale)
                        )
                        Text(
                            text = "✨",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = sparkleAlpha * 0.9f),
                            modifier = Modifier.scale(sparkleScale)
                        )
                    }
                }
            }

            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Creating Magic... 🎵",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Generate Music",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Music Usage Stats Display
 */
@Composable
fun MusicUsageStatsCard(
    stats: MusicUsageStats,
    modifier: Modifier = Modifier
) {
    if (!FeatureFlags.FreemiumConfig.SHOW_FREE_SONGS_COUNTER) {
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(0.78f),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFFFE0B2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (stats.isInFreeTier) {
                    Text(
                        text = "${stats.freeRemaining} of ${FeatureFlags.FreemiumConfig.FREE_SONGS_LIMIT} Free Songs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "💰 Pay-As-You-Go",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63)
                    )
                    Text(
                        text = "Total songs: ${stats.totalGenerated} • Cost: ${stats.totalCostFormatted}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Next generation cost (paid tier only)
            if (!stats.isInFreeTier) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryBlue
                ) {
                    Text(
                        text = stats.nextGenerationCostFormatted,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Show upgrade prompt if approaching limit
        if (stats.shouldShowUpgrade) {
            Divider(color = Color(0xFFE91E63).copy(alpha = 0.15f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Almost out of free songs! Next generations will cost $0.06 each.",
                    fontSize = 11.sp,
                    color = Color(0xFFD84315),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Music Generation Dialog - Allows user to enter prompt and generate
 */
@Composable
fun MusicGenerationDialog(
    onGenerate: (String) -> Unit,
    onDismiss: () -> Unit,
    stats: MusicUsageStats?
) {
    var lyrics by remember { mutableStateOf("") }
    var musicStyle by remember { mutableStateOf("") }

    val maxChars = remember { FeatureFlags.MusicComposerConfig.MAX_PROMPT_CHARACTERS }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎵 Generate Music",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                stats?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!it.isInFreeTier) {
                        Text(
                            text = "💰 Cost: ${it.nextGenerationCostFormatted} per song",
                            fontSize = 14.sp,
                            color = Color(0xFFFFB74D),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lyrics field
                Text(
                    text = "Lyrics",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = lyrics,
                    onValueChange = {
                        if (it.length <= maxChars) {
                            lyrics = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp, max = 150.dp),
                    placeholder = {
                        Text(
                            "Enter song lyrics or leave blank for instrumental",
                            fontSize = 12.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFE91E63)
                    ),
                    supportingText = {
                        Text(
                            text = "${lyrics.length}/$maxChars characters",
                            fontSize = 10.sp,
                            color = when {
                                lyrics.length > maxChars -> Color.Red
                                lyrics.length > maxChars * 0.9 -> Color(0xFFFF9800)
                                else -> Color.Gray
                            }
                        )
                    }
                )

                // Music style field
                Text(
                    text = "Prompt",
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = musicStyle,
                    onValueChange = {
                        if (it.length <= 200) {
                            musicStyle = it
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 120.dp),
                    placeholder = {
                        Text(
                            "Music style and mood (e.g., Jazz, Romantic, Dreamy or Upbeat Pop, Energetic)",
                            fontSize = 12.sp
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFE91E63)
                    ),
                    supportingText = {
                        Text(
                            text = "${musicStyle.length}/200 characters",
                            fontSize = 10.sp,
                            color = when {
                                musicStyle.length > 200 -> Color.Red
                                musicStyle.length > 180 -> Color(0xFFFF9800)
                                else -> Color.Gray
                            }
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Send lyrics and style as a simple combined string
                    // Lyrics first, then style separated by a pipe or just concatenated
                    val finalPrompt = if (lyrics.isNotBlank() && musicStyle.isNotBlank()) {
                        // Both provided: lyrics | style
                        "${lyrics.trim()}|${musicStyle.trim()}"
                    } else if (lyrics.isNotBlank()) {
                        // Just lyrics
                        lyrics.trim()
                    } else if (musicStyle.isNotBlank()) {
                        // Just style
                        musicStyle.trim()
                    } else {
                        ""
                    }

                    if (finalPrompt.isNotBlank()) {
                        onGenerate(finalPrompt)
                        onDismiss()
                    }
                },
                enabled = lyrics.isNotBlank() || musicStyle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Generate", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}

/**
 * Quick prompt chip
 */
@Composable
fun MusicPromptChip(
    text: String,
    onClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(text) },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF424242)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎵",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Use this prompt",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Lyria Setting Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyriaSettingDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            readOnly = true,
            value = options[selectedIndex],
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.menuAnchor(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onSelectedIndexChange(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Music Library Dialog
 */
@Composable
fun MusicLibraryDialog(
    library: List<GeneratedMusic>,
    onPlayMusic: (GeneratedMusic) -> Unit,
    onDeleteMusic: (String) -> Unit,
    onDownloadMusic: (GeneratedMusic) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A2A),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎵",
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Music Library",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                if (library.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎼",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No music yet!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generate your first track to start building your music library.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Music list
                    Text(
                        text = "${library.size} track${if (library.size != 1) "s" else ""} in library",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(library) { music ->
                            MusicTrackCard(
                                music = music,
                                onPlay = { onPlayMusic(music) },
                                onDelete = { onDeleteMusic(music.id) },
                                onDownload = { onDownloadMusic(music) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White, fontSize = 16.sp)
            }
        }
    )
}

/**
 * Individual music track card in library
 */
@Composable
fun MusicTrackCard(
    music: GeneratedMusic,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3A3A3A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = music.getShortPrompt(40),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ ${music.getFormattedDuration()}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Text(
                            text = music.getFormattedCost(),
                            fontSize = 11.sp,
                            color = if (music.isFreeTier) Color(0xFF4CAF50) else Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { showOptions = !showOptions },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }

            // Action buttons
            if (showOptions) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(onClick = onPlay) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Play", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Delete", fontSize = 10.sp, color = Color.White)
                        }
                    }
                }
            } else {
                // Quick play button when collapsed
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onPlay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Play Track", fontSize = 13.sp)
                }
            }
        }
    }
}

/**
 * Floating Music Library Button (shows when Music Composer is active)
 */
@Composable
fun MusicLibraryFAB(
    onClick: () -> Unit,
    trackCount: Int,
    modifier: Modifier = Modifier
) {
    if (!FeatureFlags.MusicComposerConfig.SHOW_MUSIC_LIBRARY) {
        return
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = Color(0xFFE91E63),
        contentColor = Color.White
    ) {
        Box {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = "Music Library"
            )

            if (trackCount > 0) {
                Badge(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = trackCount.toString(),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
