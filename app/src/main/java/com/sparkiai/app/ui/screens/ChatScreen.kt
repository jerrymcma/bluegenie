package com.sparkiai.app.ui.screens

import android.Manifest
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sparkiai.app.R
import com.sparkiai.app.ui.components.MessageBubble
import com.sparkiai.app.ui.components.PersonalitySelectorDialog
import com.sparkiai.app.ui.components.GenerateMusicButton
import com.sparkiai.app.ui.components.MusicGenerationDialog
import com.sparkiai.app.ui.components.MusicLibraryDialog
import com.sparkiai.app.ui.components.MusicUsageStatsCard
import com.sparkiai.app.ui.components.MusicLibraryFAB
import com.sparkiai.app.ui.theme.*
import com.sparkiai.app.config.FeatureFlags
import com.sparkiai.app.viewmodel.ChatViewModel
import com.sparkiai.app.model.Message
import com.sparkiai.app.model.MessageType
import com.sparkiai.app.utils.VoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceManager(context) }

    // Initialize ViewModel with context for memory management
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val recognizedText by voiceManager.recognizedText.collectAsState()
    val currentPersonality by viewModel.currentPersonality.collectAsState()
    val availablePersonalities by viewModel.availablePersonalities.collectAsState()

    // Music generation state
    val isMusicGenerating by viewModel.isMusicGenerating.collectAsState()
    val musicUsageStats by viewModel.musicUsageStats.collectAsState()
    val musicLibrary by viewModel.generatedMusicLibrary.collectAsState()
    val currentlyPlayingMusic by viewModel.currentlyPlayingMusic.collectAsState()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsState()

    // Recompute these based on personality changes
    val isMusicComposerActive = remember(currentPersonality) {
        val isActive = viewModel.isMusicComposerActive()
        Log.d(
            "MusicDebug",
            "🎵 isMusicComposerActive: $isActive (personality: ${currentPersonality.name})"
        )
        isActive
    }
    val isMusicGenerationAvailable = remember(currentPersonality) {
        val isAvailable = viewModel.isMusicGenerationAvailable()
        Log.d("MusicDebug", "🎵 isMusicGenerationAvailable: $isAvailable")
        isAvailable
    }

    LaunchedEffect(isMusicComposerActive, isMusicGenerationAvailable) {
        Log.d(
            "MusicDebug",
            "🎵 Music state: active=$isMusicComposerActive, available=$isMusicGenerationAvailable")
    }

    var messageText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showPersonalitySelector by remember { mutableStateOf(false) }
    var showStartFreshDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }

    // Music generation dialogs
    var showMusicGenerationDialog by remember { mutableStateOf(false) }
    var showMusicLibraryDialog by remember { mutableStateOf(false) }

    val favoriteMessages = remember(messages) { messages.filter { it.isBookmarked } }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val savedDateFormatter = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Chicago")
        }
    }

    // Create a temporary file for camera capture
    val photoFile = remember {
        File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
    }
    val photoUri = remember {
        FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        selectedFileUri = null
        selectedFileName = null
        showAttachmentOptions = false
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoUri
            selectedFileUri = null
            selectedFileName = null
        }
        showAttachmentOptions = false
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = resolveDisplayName(context, uri)
            selectedImageUri = null
        }
        showAttachmentOptions = false
    }

    // Mic/audio permission launcher
    var micPermissionRequested by remember { mutableStateOf(false) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            micPermissionRequested = false
            if (granted) {
                voiceManager.startListening()
            }
        }
    )

    // Handle recognized speech
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotEmpty()) {
            messageText = recognizedText
            voiceManager.clearRecognizedText()
        }
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // Clean up voice manager
    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    val sparkSnippet = " Inspire me with a Spark Idea!"

    var lastLibraryCount by remember { mutableStateOf(0) }
    var highlightMusicLibrary by remember { mutableStateOf(false) }

    LaunchedEffect(musicLibrary.size) {
        if (musicLibrary.size > lastLibraryCount) {
            highlightMusicLibrary = true
            lastLibraryCount = musicLibrary.size
            // Keep the library button lit up for a few seconds so users notice it
            delay(5000)
            highlightMusicLibrary = false
        } else {
            lastLibraryCount = musicLibrary.size
        }
    }

    fun insertSparkSnippet(closeSheet: Boolean = false) {
        messageText = if (messageText.isBlank()) {
            sparkSnippet
        } else {
            val separator = if (messageText.endsWith("\n")) "" else "\n"
            "$messageText$separator$sparkSnippet"
        }
        if (closeSheet) {
            showAttachmentOptions = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Custom App Bar Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBlue,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - current personality heading
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = currentPersonality.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    if (isSpeaking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaking",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Right side - Music Library + Personalities Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = { showPersonalitySelector = true },
                        modifier = Modifier.padding(end = 2.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personalities",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "✨",
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Prominent top-center Music Library button when music features are available
        if (isMusicComposerActive && isMusicGenerationAvailable) {
            val hasMusic = musicLibrary.isNotEmpty()
            val sparkleTransition = rememberInfiniteTransition(label = "library-sparkles")
            val sparkleScale by sparkleTransition.animateFloat(
                initialValue = if (highlightMusicLibrary) 0.95f else 0.9f,
                targetValue = if (highlightMusicLibrary) 1.2f else 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sparkle-scale"
            )
            val sparkleAlpha by sparkleTransition.animateFloat(
                initialValue = if (highlightMusicLibrary) 0.6f else 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sparkle-alpha"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Sparkles around (slightly above) the button when music exists
                if (hasMusic) {
                    Box(
                        modifier = Modifier
                            .matchParentSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .padding(top = 2.dp),
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

                Button(
                    onClick = { showMusicLibraryDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (highlightMusicLibrary) Color(0xFF1C53C2) else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(22.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    val libraryIconTint =
                        if (highlightMusicLibrary) Color(0xFFFFD700) else Color.White
                    Icon(
                        imageVector = Icons.Default.LibraryMusic,
                        contentDescription = "Music Library",
                        tint = libraryIconTint,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasMusic) {
                            if (highlightMusicLibrary) "Music Library (${musicLibrary.size}) ✨ NEW ✨" else "Music Library (${musicLibrary.size})"
                        } else "Music Library",
                        color = libraryIconTint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    WelcomeMessage(currentPersonality.name, currentPersonality.greeting)
                }
            } else {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        onSpeakClick = { text ->
                            voiceManager.speak(text)
                        },
                        onFavoriteClick = { messageId ->
                            if (message.isBookmarked) {
                                Toast.makeText(context, "Removed from Favorite Sparks", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Saved to Favorite Sparks", Toast.LENGTH_SHORT).show()
                            }
                            viewModel.toggleFavorite(messageId)
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Music Generation Button and Stats (only for Music Composer)
        if (isMusicComposerActive && isMusicGenerationAvailable) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Usage stats card
                musicUsageStats?.let { stats ->
                    MusicUsageStatsCard(
                        stats = stats,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                // Generate Music Button
                GenerateMusicButton(
                    onClick = { showMusicGenerationDialog = true },
                    enabled = viewModel.canGenerateMusic(),
                    isGenerating = isMusicGenerating,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        // Mini player bar when music is playing or paused
        currentlyPlayingMusic?.let { playing ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .height(56.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = playing.getShortPrompt(50),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleMusicPlayPause() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isMusicPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.stopMusic() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Message Input with Voice and Image Controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .imePadding(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Selected Image Preview
                selectedImageUri?.let { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .build(),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Image selected",
                            modifier = Modifier.weight(1f),
                            color = PrimaryBlue,
                            fontSize = 14.sp
                        )

                        IconButton(
                            onClick = {
                                selectedImageUri = null
                                selectedFileUri = null
                                selectedFileName = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove image",
                                tint = Color.Red
                            )
                        }
                    }
                }

                selectedFileUri?.let { uri ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Attachment,
                            contentDescription = "Selected file",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = selectedFileName ?: "File selected",
                            modifier = Modifier.weight(1f),
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )

                        IconButton(
                            onClick = {
                                selectedFileUri = null
                                selectedFileName = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove file",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }

                // Voice Status Indicator
                if (isListening) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Listening",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Listening...",
                            color = PrimaryBlue,
                            fontSize = 12.sp
                        )
                    }
                }

                // Text Field - Full Width
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Say hello to Sparki, ask anything...") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    keyboardActions = KeyboardActions.Default,
                    minLines = 2,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = PrimaryBlue,
                        unfocusedTextColor = PrimaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Icons Row - evenly spaced across bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left group - three buttons pushed to the left
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Favorites button as simple blue folder icon
                        IconButton(
                            onClick = { showFavoritesDialog = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Favorite Sparks",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Plus button (Add attachment)
                        IconButton(
                            onClick = { showAttachmentOptions = true },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(color = PrimaryBlue, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add attachment",
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Mic button
                        IconButton(
                            onClick = {
                                if (isListening) {
                                    voiceManager.stopListening()
                                } else {
                                    val permissionGranted =
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (permissionGranted) {
                                        voiceManager.startListening()
                                    } else if (!micPermissionRequested) {
                                        micPermissionRequested = true
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isListening) "Stop listening" else "Start voice input",
                                tint = if (isListening) Color.Red else PrimaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Middle - Send button
                    FloatingActionButton(
                        onClick = {
                            if ((messageText.isNotBlank() || selectedImageUri != null || selectedFileUri != null) && !isLoading) {
                                sendMessage(
                                    viewModel,
                                    messageText,
                                    selectedImageUri,
                                    selectedFileUri,
                                    selectedFileName
                                )
                                messageText = ""
                                selectedImageUri = null
                                selectedFileUri = null
                                selectedFileName = null
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send message"
                        )
                    }

                    // Right - Spark Idea button
                    IconButton(
                        onClick = { insertSparkSnippet() },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color = PrimaryBlue, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Spark idea",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Personality Selector Dialog
    if (showPersonalitySelector) {
        PersonalitySelectorDialog(
            personalities = availablePersonalities,
            currentPersonality = currentPersonality,
            onPersonalitySelected = { personality ->
                viewModel.changePersonality(personality)
            },
            onDismiss = { showPersonalitySelector = false }
        )
    }

    // Start Fresh Dialog
    if (showStartFreshDialog) {
        AlertDialog(
            onDismissRequest = { showStartFreshDialog = false },
            title = { Text("Start Fresh") },
            text = { Text("Start over? AI will forget this chat.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStartFreshDialog = false
                        viewModel.startFresh()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartFreshDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Attachment Options Dialog
    if (showAttachmentOptions) {
        AlertDialog(
            onDismissRequest = { showAttachmentOptions = false },
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Add to Chat",
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = { showAttachmentOptions = false },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close attachment dialog",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AttachmentOptionButton(
                            label = "File",
                            icon = Icons.Outlined.Folder,
                            onClick = {
                                filePickerLauncher.launch(arrayOf("*/*"))
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        AttachmentOptionButton(
                            label = "Camera",
                            icon = Icons.Default.CameraAlt,
                            onClick = {
                                cameraLauncher.launch(photoUri)
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFF00BCD4), Color(0xFF1DE9B6))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AttachmentOptionButton(
                            label = "Gallery",
                            icon = Icons.Default.PhotoLibrary,
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        AttachmentOptionButton(
                            label = "Generate Music",
                            icon = Icons.Default.LibraryMusic,
                            onClick = {
                                // Always link this action to Magic Music Spark personality
                                val magicMusicSpark =
                                    availablePersonalities.firstOrNull { it.id == "music_composer" }
                                if (magicMusicSpark != null) {
                                    viewModel.changePersonality(magicMusicSpark)
                                }
                                showAttachmentOptions = false
                                // Music dialog will appear if generation is available for the personality
                                showMusicGenerationDialog = true
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFF1C53C2), Color(0xFF64B5F6))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Music Generation Dialog
    if (showMusicGenerationDialog && isMusicComposerActive) {
        MusicGenerationDialog(
            onGenerate = { prompt ->
                viewModel.generateMusic(prompt)
            },
            onDismiss = { showMusicGenerationDialog = false },
            stats = musicUsageStats
        )
    }

    // Music Library Dialog
    if (showMusicLibraryDialog) {
        MusicLibraryDialog(
            library = musicLibrary,
            onPlayMusic = { music ->
                viewModel.playMusic(music)
                Toast.makeText(
                    context,
                    "🎵 Playing: ${music.getShortPrompt()}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteMusic = { musicId ->
                viewModel.deleteMusic(musicId)
                Toast.makeText(context, "🗑️ Music deleted", Toast.LENGTH_SHORT).show()
            },
            onDownloadMusic = { music ->
                // TODO: Implement music download to device
                Toast.makeText(
                    context,
                    "⬇️ Download feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showMusicLibraryDialog = false }
        )
    }

    if (showFavoritesDialog) {
        BasicAlertDialog(onDismissRequest = { showFavoritesDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 6.dp,
                color = Color(0xFF1C53C2)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                        .widthIn(min = 0.dp, max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "✨ Favorite Sparks ✨",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (favoriteMessages.isEmpty()) {
                        Text(
                            text = "Your saved Sparki messages will appear here.\n\nTo save a Spark, tap the 3-dot button at the bottom of a Sparki message.",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            favoriteMessages.forEach { favorite ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = favorite.content,
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 3,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val savedTimestampText = remember(favorite.timestamp) {
                                            savedDateFormatter.format(Date(favorite.timestamp))
                                        }
                                        Text(
                                            text = "Saved $savedTimestampText",
                                            color = PrimaryBlue.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { showFavoritesDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                            modifier = Modifier.align(Alignment.BottomEnd),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text(text = "Close", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null, null)
    try {
        if (cursor != null && cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
            return null
        }
    } finally {
        cursor?.close()
    }
    return null
}

private fun sendMessage(
    viewModel: ChatViewModel,
    text: String,
    imageUri: Uri?,
    fileUri: Uri?,
    fileName: String?
) {
    when {
        imageUri != null && text.isNotBlank() -> {
            // Text with image
            viewModel.sendMessage(
                content = text,
                imageUri = imageUri.toString(),
                messageType = MessageType.TEXT_WITH_IMAGE
            )
        }
        imageUri != null -> {
            // Image only
            viewModel.sendMessage(
                content = "📷 Image shared",
                imageUri = imageUri.toString(),
                fileUri = null,
                fileName = null,
                messageType = MessageType.IMAGE
            )
        }
        fileUri != null && text.isNotBlank() -> {
            // Text with file
            viewModel.sendMessage(
                content = text,
                fileUri = fileUri.toString(),
                fileName = fileName,
                messageType = MessageType.TEXT_WITH_FILE
            )
        }
        fileUri != null -> {
            // File only
            viewModel.sendMessage(
                content = "📁 File shared",
                fileUri = fileUri.toString(),
                fileName = fileName,
                messageType = MessageType.FILE
            )
        }
        text.isNotBlank() -> viewModel.sendMessage(text)
    }
}

@Composable
fun WelcomeMessage(
    personalityName: String = "SparkiFire AI",
    greeting: String = "Hey there! I'm Sparki, your personal assistant. What's on your mind? You can type, use voice input, share images and files, or try a 'Spark idea' for inspiration. Tap the 'Personalities' icon to meet all of the Sparkies...we're glad you're here!"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = AIMessageBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Welcome!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnAIMessage
                )
                Spacer(modifier = Modifier.width(6.dp))
                PulsatingFireEmoji()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = greeting,
                fontSize = 16.sp,
                color = TextOnAIMessage
            )
        }
    }
}

@Composable
private fun PulsatingFireEmoji(
    fontSize: TextUnit = 24.sp,
    tint: Color = TextOnAIMessage
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome-fire")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire-scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fire-alpha"
    )

    Text(
        text = "🔥",
        fontSize = 24.sp,
        modifier = Modifier.scale(scale),
        color = TextOnAIMessage.copy(alpha = alpha)
    )
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle-pulse")
    val flameSequenceProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing)
        ),
        label = "flame-sequence"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AIMessageBackground),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PulsatingFireEmoji(fontSize = 28.sp, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sparki is thinking",
                    color = TextOnAIMessage,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun AttachmentOptionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    gradient: Brush,
    modifier: Modifier = Modifier,
    iconBorderColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        label = "attachment-press-$label"
    )
    val pulse = rememberInfiniteTransition(label = "attachment-pulse-$label").animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "attachment-pulse-scale-$label"
    ).value
    val combinedScale = pressScale * pulse

    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(72.dp)
            .scale(combinedScale)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp), clip = false),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(18.dp))
                .background(gradient)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val iconModifier = if (iconBorderColor != null) {
                    Modifier
                        .size(34.dp)
                        .border(BorderStroke(2.dp, iconBorderColor), CircleShape)
                        .padding(4.dp)
                } else {
                    Modifier.size(34.dp)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = iconModifier
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}