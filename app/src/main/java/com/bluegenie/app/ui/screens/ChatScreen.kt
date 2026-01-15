@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_UNARY_OPERATOR")
package com.bluegenie.app.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.viewinterop.AndroidView
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import android.os.Build
import com.bluegenie.app.R
import com.bluegenie.app.ui.components.MessageBubble
import com.bluegenie.app.ui.components.PersonalitySelectorDialog
import com.bluegenie.app.ui.components.SignInModal
import com.bluegenie.app.ui.components.PremiumUpgradeModal
import com.bluegenie.app.ui.components.GenerateMusicButton
import com.bluegenie.app.ui.components.MusicGenerationDialog
import com.bluegenie.app.ui.components.MusicLibraryDialog
import com.bluegenie.app.ui.components.MusicUsageStatsCard
import com.bluegenie.app.ui.theme.*
import com.bluegenie.app.viewmodel.ChatViewModel
import com.bluegenie.app.model.MessageType
import com.bluegenie.app.utils.VoiceManager
import com.bluegenie.app.utils.GoogleSignInManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
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
    val googleSignInManager = remember { GoogleSignInManager(context) }

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
    val subscription by viewModel.subscription.collectAsState()
    val showSignInModal by viewModel.showSignInModal.collectAsState()
    val showUpgradeModal by viewModel.showUpgradeModal.collectAsState()

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        val account = googleSignInManager.handleSignInResult(task)
        val idToken = account?.idToken
        if (idToken != null) {
            viewModel.signInWithGoogle(idToken)
        } else {
            Toast.makeText(
                context,
                "Unable to sign in. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

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

    val sparkSnippet = "✨ Hey Blue Genie ✨🔮, let's summon a Genius Genie Idea ✨ with your crystal ball! ✨🔮"

    var lastLibraryCount by remember { mutableIntStateOf(0) }
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

    fun openMagicMusic(openGenerator: Boolean = false) {
        val musicPersonality = availablePersonalities.firstOrNull {
            it.id == "music_composer" || it.name.contains("Magic Music", ignoreCase = true)
        }
        musicPersonality?.let {
            viewModel.changePersonality(it)
            if (openGenerator) {
                showMusicGenerationDialog = true
            }
            coroutineScope.launch {
                delay(150)
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        } ?: Toast.makeText(context, "Magic Music not available yet", Toast.LENGTH_SHORT).show()
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
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - current personality heading (names)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentPersonality.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    AnimatedSparklesHeader(modifier = Modifier.size(32.dp))
                    if (isSpeaking) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speaking",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { showPersonalitySelector = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Text(
                            text = "Ai Models",
                            fontSize = 15.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        FlickeringSparkles(modifier = Modifier.size(28.dp))
                    }
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
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 0.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    WelcomeMessage(currentPersonality.greeting)
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
                                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Saved to Favorites", Toast.LENGTH_SHORT).show()
                            }
                            viewModel.toggleFavorite(messageId)
                        },
                        onLogoutClick = {
                            googleSignInManager.signOut {
                                viewModel.signOut()
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            if (isLoading) {
                item {
                    TypingIndicator(modelName = currentPersonality.name)
                }
            }
        }

        // Music Generation Button and Stats (only for Music Composer)
        if (isMusicComposerActive && isMusicGenerationAvailable) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Usage stats card - show subscription info
                MusicUsageStatsCard(
                    subscription = subscription,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )

                // Music Library button moved here
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
                        .fillMaxWidth(),
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
                        modifier = Modifier
                            .fillMaxWidth(0.78f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (highlightMusicLibrary) Color(0xFF1C53C2) else PrimaryBlue
                        ),
                        shape = RoundedCornerShape(16.dp),
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Generate Music Button
                GenerateMusicButton(
                    onClick = {
                        if (!viewModel.isUserSignedIn()) {
                            viewModel.showSignIn()
                        } else {
                            showMusicGenerationDialog = true
                        }
                    },
                    enabled = viewModel.canGenerateMusic(),
                    isGenerating = isMusicGenerating,
                    modifier = Modifier
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
                            .fillMaxHeight())
                    {
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
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
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

                selectedFileUri?.let { 
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
                val textFieldInteractionSource = remember { MutableInteractionSource() }
                val isTextFieldFocused by textFieldInteractionSource.collectIsFocusedAsState()
                val infiniteTransition = rememberInfiniteTransition(label = "textFieldShimmer")
                val shimmerProgress by infiniteTransition.animateFloat(
                    initialValue = -0.2f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 1500
                            -0.2f at 0 using LinearEasing
                            1.2f at 1000 using LinearEasing
                            1.2f at 1500 using LinearEasing
                        },
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmerProgress"
                )
                
                val shimmerBrush = remember(shimmerProgress) {
                    val baseColor = PrimaryBlueLighter.copy(alpha = 0.7f)
                    val highlightColor = Color.White.copy(alpha = 0.8f)
                    
                    val colorStops = mutableListOf<Pair<Float, Color>>()
                    colorStops.add(0.0f to baseColor)
                    
                    if (shimmerProgress > 0f && shimmerProgress < 1f) {
                        val startHighlight = (shimmerProgress - 0.1f).coerceIn(0f, 1f)
                        if (startHighlight > 0f) colorStops.add(startHighlight to baseColor)
                        colorStops.add(shimmerProgress to highlightColor)
                        val endHighlight = (shimmerProgress + 0.1f).coerceIn(0f, 1f)
                        if (endHighlight < 1f) colorStops.add(endHighlight to baseColor)
                    }
                    
                    colorStops.add(1.0f to baseColor)
                    colorStops.sortBy { it.first }
                    Brush.linearGradient(*colorStops.toTypedArray())
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isTextFieldFocused) {
                                Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(4.dp))
                            } else {
                                Modifier.border(2.dp, shimmerBrush, RoundedCornerShape(4.dp))
                            }
                        )
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Say hello to ${currentPersonality.name}, ask anything...") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Default
                        ),
                        keyboardActions = KeyboardActions.Default,
                        minLines = 2,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = PrimaryBlue,
                            unfocusedTextColor = PrimaryBlue
                        ),
                        interactionSource = textFieldInteractionSource
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Icons Row - evenly spaced across bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left group - three buttons pushed to the left
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Magic Music Spark shortcut
                        IconButton(
                            onClick = { openMagicMusic(openGenerator = false) },
                            modifier = Modifier.size(48.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .shadow(8.dp, shape = CircleShape)
                                    .background(color = PrimaryBlue, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Magic Music",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
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
                                    .shadow(8.dp, shape = CircleShape)
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

                    // Middle - Genie's Lamp Idea button
                    IconButton(
                        onClick = { insertSparkSnippet() },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(8.dp, shape = CircleShape)
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

                    // Right - Send button
                    FloatingActionButton(
                        onClick = {
                            Log.d("ChatScreenSend", "Send button clicked. isLoading: $isLoading, messageText: '$messageText', imageUri: $selectedImageUri")
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
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message"
                        )
                    }
                }
            }
        }

        // Footer with links
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            color = BackgroundLight
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://bluegeniemagic.com/privacy".toUri())
                        context.startActivity(intent)
                    },
                    contentPadding = PaddingValues(4.dp, 0.dp)
                ) {
                    Text(
                        text = "Privacy",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }
                Text(
                    text = " • ",
                    fontSize = 10.sp,
                    color = TextOnAIMessage
                )
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://bluegeniemagic.com/terms".toUri())
                        context.startActivity(intent)
                    },
                    contentPadding = PaddingValues(4.dp, 0.dp)
                ) {
                    Text(
                        text = "Terms",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }
                Text(
                    text = " • ",
                    fontSize = 10.sp,
                    color = TextOnAIMessage
                )
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, "https://bluegeniemagic.com".toUri())
                        context.startActivity(intent)
                    },
                    contentPadding = PaddingValues(4.dp, 0.dp)
                ) {
                    Text(
                        text = "Web App",
                        fontSize = 12.sp,
                        color = PrimaryBlue,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
    }

    // Personality Selector Dialog
    if (showPersonalitySelector) {
        PersonalitySelectorDialog(
            personalities = availablePersonalities,
            currentPersonality = currentPersonality,
            subscription = subscription,
            onShowUpgrade = {
                viewModel.setShowUpgradeModal(true)
                showPersonalitySelector = false
            },
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
            containerColor = Color(0xFF03467D),
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Add to Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
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
                            label = "Images",
                            icon = Icons.Default.PhotoLibrary,
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFFE65100), Color(0xFFFF9800))
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
                                colors = listOf(Color(0xFFD45200), Color(0xFFD45200))
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider(color = PrimaryBlue.copy(alpha = 0.2f))
                    Text(
                        text = "Library",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AttachmentOptionButton(
                            label = "Magic Music",
                            icon = Icons.Default.LibraryMusic,
                            onClick = {
                                showAttachmentOptions = false
                                openMagicMusic(openGenerator = false)
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, PrimaryBlue)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        AttachmentOptionButton(
                            label = "Favorites",
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            onClick = {
                                showAttachmentOptions = false
                                showFavoritesDialog = true
                            },
                            gradient = Brush.linearGradient(
                                colors = listOf(Color(0xFF1565C0), Color(0xFF1565C0))
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
            currentlyPlayingMusic = currentlyPlayingMusic,
            isMusicPlaying = isMusicPlaying,
            onPlayMusic = { music ->
                viewModel.playMusic(music)
                Toast.makeText(
                    context,
                    "🎵 Playing: ${music.getShortPrompt()}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onToggleMusicPlayPause = { viewModel.toggleMusicPlayPause() },
            onStopMusic = { viewModel.stopMusic() },
            onDeleteMusic = { musicId ->
                viewModel.deleteMusic(musicId)
                Toast.makeText(context, "🗑️ Music deleted", Toast.LENGTH_SHORT).show()
            },
            onDownloadMusic = { _ ->
                // TODO: Implement music download to device
                Toast.makeText(
                    context,
                    "⬇️ Download feature coming soon!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onShareMusic = { music ->
                try {
                    val file = File(music.filePath)
                    if (file.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = music.mimeType
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_SUBJECT, "Check out this song created by Blue Genie!")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Created with Blue Genie - Music Composer"
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Music"))
                    } else {
                        Toast.makeText(
                            context,
                            "❌ Music file not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "❌ Error sharing music: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                        text = "✨🔮 Favorites 🧞",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (favoriteMessages.isEmpty()) {
                        Text(
                            text = "Your saved Blue Genie messages will appear here.\n\nTo save a message, tap the 3-dot button at the bottom of a Blue Genie message.",
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

    SignInModal(
        isOpen = showSignInModal,
        onSignIn = {
            viewModel.setShowSignInModal(false)
            signInLauncher.launch(googleSignInManager.getSignInIntent())
        },
        onDismiss = { viewModel.setShowSignInModal(false) }
    )

    PremiumUpgradeModal(
        isOpen = showUpgradeModal,
        onUpgrade = { viewModel.startPremiumCheckout() },
        onDismiss = { viewModel.setShowUpgradeModal(false) },
        isRenewal = subscription.needsRenewal
    )
}

@Composable
fun AnimatedSparklesHeader(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = remember {
        coil.ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(R.drawable.rotating_sparkles)
            .build(),
        contentDescription = "Animated Sparkles",
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun FlickeringSparkles(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.sparkles_icon),
        contentDescription = "Sparkles",
        modifier = modifier
            .graphicsLayer(
                alpha = 1f
            ),
        contentScale = ContentScale.Fit
    )
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
    greeting: String = "Hey there! I'm your AI assistant. What's on your mind?\n\nYou can ask me anything! I can help you with text, voice input, images, files, and even generate ideas for inspiration. Tap the 'Ai Models' icon to meet all of the genies...we're glad you're here!"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
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
                    color = TextOnAIMessage)
                Spacer(modifier = Modifier.width(6.dp))
                PulsatingCrystalBallIcon()
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
private fun PulsatingCrystalBallIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome-crystal-ball")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crystal-ball-scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crystal-ball-alpha"
    )

    Image(
        painter = painterResource(id = R.drawable.crystal_ball_icon),
        contentDescription = "Crystal Ball",
        modifier = Modifier
            .size(28.dp)
            .scale(scale)
            .graphicsLayer(alpha = alpha),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun PulsatingSparklesEmoji(
    fontSize: TextUnit = 24.sp,
    tint: Color = TextOnAIMessage
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome-sparkle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle-scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle-alpha"
    )

    Text(
        text = "✨",
        fontSize = fontSize,
        modifier = Modifier.scale(scale),
        color = tint.copy(alpha = alpha)
    )
}

@Composable
fun TypingIndicator(modelName: String = "Blue Genie") {

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
                PulsatingSparklesEmoji(fontSize = 28.sp, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$modelName is thinking",
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
            .height(64.dp)
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
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val iconModifier = if (iconBorderColor != null) {
                    Modifier
                        .size(30.dp)
                        .border(BorderStroke(2.dp, iconBorderColor), CircleShape)
                        .padding(4.dp)
                } else {
                    Modifier.size(30.dp)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = iconModifier
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
