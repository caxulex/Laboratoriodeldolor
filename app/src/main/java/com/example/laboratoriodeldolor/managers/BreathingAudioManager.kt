package com.example.laboratoriodeldolor.managers

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BreathingAudioManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null
    private var isInitialized = false
    private var currentVolume = 0.7f
    
    // Audio settings
    companion object {
        const val TONE_DURATION_MS = 200
        const val INHALE_TONE = ToneGenerator.TONE_PROP_BEEP
        const val EXHALE_TONE = ToneGenerator.TONE_PROP_BEEP2
        const val HOLD_TONE = ToneGenerator.TONE_DTMF_0
        const val PHASE_TRANSITION_TONE = ToneGenerator.TONE_DTMF_1
    }
    
    fun initialize(volume: Float = 0.7f) {
        if (isInitialized) return
        
        try {
            currentVolume = volume.coerceIn(0f, 1f)
            
            // Initialize ToneGenerator for simple beeps
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, (currentVolume * 100).toInt())
            
            // Initialize MediaPlayer for custom sounds if needed
            mediaPlayer = MediaPlayer().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                    )
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                setVolume(currentVolume, currentVolume)
            }
            
            isInitialized = true
        } catch (e: Exception) {
            // Handle initialization errors gracefully
            isInitialized = false
        }
    }
    
    fun updateVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        
        try {
            // Update ToneGenerator volume by recreating it
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, (currentVolume * 100).toInt())
            
            // Update MediaPlayer volume
            mediaPlayer?.setVolume(currentVolume, currentVolume)
        } catch (e: Exception) {
            // Handle volume update errors gracefully
        }
    }
    
    fun playInhaleSound() {
        if (!isInitialized) return
        
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(INHALE_TONE, TONE_DURATION_MS)
            } catch (e: Exception) {
                // Handle playback errors gracefully
            }
        }
    }
    
    fun playExhaleSound() {
        if (!isInitialized) return
        
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(EXHALE_TONE, TONE_DURATION_MS)
            } catch (e: Exception) {
                // Handle playback errors gracefully
            }
        }
    }
    
    fun playHoldSound() {
        if (!isInitialized) return
        
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(HOLD_TONE, TONE_DURATION_MS)
            } catch (e: Exception) {
                // Handle playback errors gracefully
            }
        }
    }
    
    fun playPhaseTransitionSound() {
        if (!isInitialized) return
        
        coroutineScope.launch {
            try {
                toneGenerator?.startTone(PHASE_TRANSITION_TONE, TONE_DURATION_MS * 2)
            } catch (e: Exception) {
                // Handle playback errors gracefully
            }
        }
    }
    
    fun playCustomBeepPattern(pattern: BeepPattern) {
        if (!isInitialized) return
        
        coroutineScope.launch {
            try {
                for ((tone, duration) in pattern.sequence) {
                    toneGenerator?.startTone(tone, duration)
                    delay(duration.toLong() + 50) // Small gap between tones
                }
            } catch (e: Exception) {
                // Handle playback errors gracefully
            }
        }
    }
    
    fun release() {
        try {
            mediaPlayer?.release()
            toneGenerator?.release()
        } catch (e: Exception) {
            // Handle release errors gracefully
        }
        
        mediaPlayer = null
        toneGenerator = null
        isInitialized = false
    }
    
    // Data class for custom beep patterns
    data class BeepPattern(
        val sequence: List<Pair<Int, Int>> // Pairs of (tone, duration_ms)
    ) {
        companion object {
            val GENTLE_TRANSITION = BeepPattern(
                listOf(
                    Pair(ToneGenerator.TONE_DTMF_1, 100),
                    Pair(ToneGenerator.TONE_DTMF_2, 100)
                )
            )
            
            val PHASE_COMPLETE = BeepPattern(
                listOf(
                    Pair(ToneGenerator.TONE_PROP_BEEP, 150),
                    Pair(ToneGenerator.TONE_PROP_BEEP, 150),
                    Pair(ToneGenerator.TONE_PROP_BEEP, 150)
                )
            )
            
            val SESSION_START = BeepPattern(
                listOf(
                    Pair(ToneGenerator.TONE_DTMF_1, 300)
                )
            )
            
            val SESSION_END = BeepPattern(
                listOf(
                    Pair(ToneGenerator.TONE_DTMF_9, 500)
                )
            )
        }
    }
}