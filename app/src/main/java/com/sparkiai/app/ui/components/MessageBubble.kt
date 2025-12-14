package com.sparkiai.app.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sparkiai.app.R
import com.sparkiai.app.model.Message
import com.sparkiai.app.model.MessageType
import com.sparkiai.app.ui.theme.*
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.VolumeUp

@Composable
fun MessageBubble(
    message: Message,
    onSpeakClick: ((String) -> Unit)? = null,
    onFavoriteClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isFromUser) {
                        UserMessageBackground
                    } else {
                        AIMessageBackground
                    }
                ),
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (message.isFromUser) 18.dp else 4.dp,
                    bottomEnd = if (message.isFromUser) 4.dp else 18.dp
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Display image if present
                    if (message.imageUri != null &&
                        (message.messageType == MessageType.IMAGE || message.messageType == MessageType.TEXT_WITH_IMAGE)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(message.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Shared image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        if (message.messageType == MessageType.TEXT_WITH_IMAGE) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Display file if present
                    // Improved file preview for file-related messages
                    if (
                        message.messageType == MessageType.FILE
                        || message.messageType == MessageType.TEXT_WITH_FILE
                        || message.fileName != null
                        || message.fileUri != null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if ((message.content.isNotBlank() && message.messageType != MessageType.FILE)) 8.dp else 0.dp)
                                .clickable(enabled = message.fileUri != null) {
                                    message.fileUri?.let { uri ->
                                        shareText(context, uri)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "File attached",
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.fileName ?: "File attached",
                                    color = PrimaryBlue,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                // Path hidden for a cleaner look; file name is sufficient context
                            }
                        }
                    }

                    // Display text if present
                    if (message.messageType == MessageType.TEXT || message.messageType == MessageType.TEXT_WITH_IMAGE || message.messageType == MessageType.TEXT_WITH_FILE) {
                        Text(
                            text = message.content,
                            color = if (message.isFromUser) {
                                TextOnUserMessage
                            } else {
                                TextOnAIMessage
                            },
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            if (!message.isFromUser) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.messageType != MessageType.IMAGE) {
                        onSpeakClick?.let { speakClick ->
                            IconButton(
                                onClick = { speakClick(message.content) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak message",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    MessageMenuActions(
                        isUser = false,
                        isFavorited = message.isBookmarked,
                        onCopy = { copyToClipboard(context, message.content) },
                        onShare = { shareText(context, message.content) },
                        onFavorite = onFavoriteClick?.let { favoriteHandler ->
                            { favoriteHandler(message.id) }
                        },
                        tintColor = PrimaryBlue
                    )
                }
            }
        }
    }

    if (message.isFromUser) {
        MessageMenuActions(
            isUser = true,
            onCopy = { copyToClipboard(context, message.content) },
            onShare = { shareText(context, message.content) },
            tintColor = PrimaryBlue
        )
    }
}

@Composable
private fun MessageMenuActions(
    isUser: Boolean,
    isFavorited: Boolean = false,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onFavorite: (() -> Unit)? = null,
    tintColor: Color = PrimaryBlue
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(40.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_more_horiz_two_dots),
                contentDescription = if (isUser) "More options" else "Message options",
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Copy") },
                onClick = {
                    menuExpanded = false
                    onCopy()
                }
            )
            DropdownMenuItem(
                text = { Text("Share") },
                onClick = {
                    menuExpanded = false
                    onShare()
                }
            )
            onFavorite?.let {
                DropdownMenuItem(
                    text = { Text(if (isFavorited) "Remove Favorite" else "Favorite Spark") },
                    onClick = {
                        menuExpanded = false
                        it()
                    }
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Sparki Response", text)
    clipboardManager.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareText(context: Context, text: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Sparki's response")
    context.startActivity(shareIntent)
}