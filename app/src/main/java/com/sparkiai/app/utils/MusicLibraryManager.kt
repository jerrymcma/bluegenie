package com.sparkiai.app.utils

import android.content.Context
import android.util.Log
import com.sparkiai.app.config.FeatureFlags
import com.sparkiai.app.model.GeneratedMusic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Manages the library of generated music tracks
 * - Saves audio files
 * - Tracks metadata
 * - Provides library access
 */
class MusicLibraryManager(private val context: Context) {

    companion object {
        private const val MUSIC_DIR = "generated_music"
        private const val LIBRARY_FILE = "music_library.json"
        private const val TAG = "MusicLibraryManager"
    }

    private val musicDir: File by lazy {
        File(context.filesDir, MUSIC_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private val libraryFile: File by lazy {
        File(context.filesDir, LIBRARY_FILE)
    }

    /**
     * Save generated music to library
     */
    suspend fun saveMusic(
        audioData: ByteArray,
        prompt: String,
        mimeType: String,
        durationSeconds: Int,
        isFreeTier: Boolean,
        costCents: Int
    ): GeneratedMusic = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val extension = when (mimeType) {
            "audio/wav" -> "wav"
            "audio/mp3", "audio/mpeg" -> "mp3"
            else -> "audio"
        }
        val fileName = "music_$id.$extension"
        val file = File(musicDir, fileName)

        // Save audio file
        file.writeBytes(audioData)

        Log.d(TAG, "💾 Saved music file: $fileName (${audioData.size} bytes)")

        // Create metadata
        val music = GeneratedMusic(
            id = id,
            prompt = prompt,
            filePath = file.absolutePath,
            mimeType = mimeType,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis(),
            isFreeTier = isFreeTier,
            costCents = costCents
        )

        // Add to library
        addToLibrary(music)

        // Enforce max library size
        enforceLibraryLimit()

        return@withContext music
    }

    /**
     * Add music to library metadata
     */
    private fun addToLibrary(music: GeneratedMusic) {
        val library = loadLibrary().toMutableList()
        library.add(0, music)  // Add to beginning (most recent first)
        saveLibrary(library)
    }

    /**
     * Load all music from library
     */
    fun loadLibrary(): List<GeneratedMusic> {
        if (!libraryFile.exists()) {
            return emptyList()
        }

        return try {
            val json = libraryFile.readText()
            val jsonArray = JSONArray(json)
            val library = mutableListOf<GeneratedMusic>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val music = GeneratedMusic(
                    id = obj.getString("id"),
                    prompt = obj.getString("prompt"),
                    filePath = obj.getString("filePath"),
                    mimeType = obj.getString("mimeType"),
                    durationSeconds = obj.getInt("durationSeconds"),
                    timestamp = obj.getLong("timestamp"),
                    isFreeTier = obj.getBoolean("isFreeTier"),
                    costCents = obj.optInt("costCents", 0)
                )

                // Only add if file still exists
                if (File(music.filePath).exists()) {
                    library.add(music)
                }
            }

            library
        } catch (e: Exception) {
            Log.e(TAG, "Error loading library: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Save library metadata
     */
    private fun saveLibrary(library: List<GeneratedMusic>) {
        try {
            val jsonArray = JSONArray()
            library.forEach { music ->
                val obj = JSONObject().apply {
                    put("id", music.id)
                    put("prompt", music.prompt)
                    put("filePath", music.filePath)
                    put("mimeType", music.mimeType)
                    put("durationSeconds", music.durationSeconds)
                    put("timestamp", music.timestamp)
                    put("isFreeTier", music.isFreeTier)
                    put("costCents", music.costCents)
                }
                jsonArray.put(obj)
            }

            libraryFile.writeText(jsonArray.toString())
            Log.d(TAG, "📚 Library saved: ${library.size} tracks")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving library: ${e.message}", e)
        }
    }

    /**
     * Get music by ID
     */
    fun getMusicById(id: String): GeneratedMusic? {
        return loadLibrary().find { it.id == id }
    }

    /**
     * Delete music from library
     */
    suspend fun deleteMusic(id: String): Boolean = withContext(Dispatchers.IO) {
        val music = getMusicById(id) ?: return@withContext false

        // Delete file
        val file = File(music.filePath)
        val fileDeleted = if (file.exists()) {
            file.delete()
        } else {
            true
        }

        // Remove from library
        if (fileDeleted) {
            val library = loadLibrary().toMutableList()
            library.removeAll { it.id == id }
            saveLibrary(library)
            Log.d(TAG, "🗑️ Deleted music: $id")
        }

        return@withContext fileDeleted
    }

    /**
     * Clear entire library
     */
    suspend fun clearLibrary(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete all music files
            musicDir.listFiles()?.forEach { it.delete() }

            // Clear library metadata
            if (libraryFile.exists()) {
                libraryFile.delete()
            }

            Log.d(TAG, "🗑️ Library cleared")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing library: ${e.message}", e)
            false
        }
    }

    /**
     * Enforce max library size
     * Removes oldest tracks if exceeding limit
     */
    private fun enforceLibraryLimit() {
        val maxSize = FeatureFlags.MusicComposerConfig.MAX_LIBRARY_SONGS
        val library = loadLibrary()

        if (library.size > maxSize) {
            val toRemove = library.drop(maxSize)
            toRemove.forEach { music ->
                File(music.filePath).delete()
            }

            val newLibrary = library.take(maxSize)
            saveLibrary(newLibrary)

            Log.d(TAG, "📚 Library trimmed: ${toRemove.size} old tracks removed")
        }
    }

    /**
     * Get library statistics
     */
    fun getLibraryStats(): LibraryStats {
        val library = loadLibrary()
        val totalSize = library.sumOf {
            File(it.filePath).let { file ->
                if (file.exists()) file.length() else 0L
            }
        }
        val totalDuration = library.sumOf { it.durationSeconds }
        val freeTierCount = library.count { it.isFreeTier }
        val paidCount = library.size - freeTierCount

        return LibraryStats(
            totalTracks = library.size,
            totalSizeBytes = totalSize,
            totalDurationSeconds = totalDuration,
            freeTierTracks = freeTierCount,
            paidTracks = paidCount
        )
    }

    /**
     * Get audio file for music
     */
    fun getAudioFile(id: String): File? {
        val music = getMusicById(id) ?: return null
        val file = File(music.filePath)
        return if (file.exists()) file else null
    }
}

/**
 * Library statistics
 */
data class LibraryStats(
    val totalTracks: Int,
    val totalSizeBytes: Long,
    val totalDurationSeconds: Int,
    val freeTierTracks: Int,
    val paidTracks: Int
) {
    fun getTotalSizeMB(): String {
        val mb = totalSizeBytes / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
    }

    fun getTotalDurationFormatted(): String {
        val minutes = totalDurationSeconds / 60
        val seconds = totalDurationSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}
