package com.sparkiai.app.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Handles music playback for generated tracks
 */
class MusicPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    companion object {
        private const val TAG = "MusicPlayer"
    }

    /**
     * Play a music file
     */
    fun play(file: File) {
        try {
            // Stop current playback if any
            stop()

            if (!file.exists()) {
                Log.e(TAG, "❌ Music file not found: ${file.absolutePath}")
                return
            }

            Log.d(TAG, "🎵 Playing music file: ${file.name}")

            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()

                // Set listeners
                setOnPreparedListener { mp ->
                    _duration.value = mp.duration
                    mp.start()
                    _isPlaying.value = true
                    Log.d(TAG, "✅ Music playback started (${mp.duration}ms)")
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    Log.d(TAG, "✅ Music playback completed")
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "❌ MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    true
                }
            }

            currentFile = file

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error playing music: ${e.message}", e)
            _isPlaying.value = false
        }
    }

    /**
     * Pause playback
     */
    fun pause() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.pause()
                    _currentPosition.value = mp.currentPosition
                    _isPlaying.value = false
                    Log.d(TAG, "⏸️ Music paused at ${mp.currentPosition}ms")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing music: ${e.message}", e)
        }
    }

    /**
     * Resume playback
     */
    fun resume() {
        try {
            mediaPlayer?.let { mp ->
                if (!mp.isPlaying) {
                    mp.start()
                    _isPlaying.value = true
                    Log.d(TAG, "▶️ Music resumed")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming music: ${e.message}", e)
        }
    }

    /**
     * Stop playback
     */
    fun stop() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
                Log.d(TAG, "⏹️ Music stopped")
            }
            mediaPlayer = null
            currentFile = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _duration.value = 0
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping music: ${e.message}", e)
        }
    }

    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            currentFile?.let { play(it) } ?: Log.w(TAG, "No file to resume")
        }
    }

    /**
     * Seek to position
     */
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
            _currentPosition.value = position
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking: ${e.message}", e)
        }
    }

    /**
     * Get current playback position
     */
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Get total duration
     */
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Check if currently playing
     */
    fun isCurrentlyPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Release resources
     */
    fun release() {
        stop()
        Log.d(TAG, "🔇 MusicPlayer released")
    }
}
