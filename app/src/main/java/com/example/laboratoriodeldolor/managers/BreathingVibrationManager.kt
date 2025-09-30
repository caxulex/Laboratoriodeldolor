package com.example.laboratoriodeldolor.managers

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BreathingVibrationManager(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private var vibrator: Vibrator? = null
    private var currentIntensity = 2 // 1=low, 2=medium, 3=high
    
    companion object {
        const val VIBRATION_DURATION_SHORT = 200L
        const val VIBRATION_DURATION_MEDIUM = 400L
        const val VIBRATION_DURATION_LONG = 600L
        
        // Vibration intensities (API 26+)
        const val INTENSITY_LOW = 100
        const val INTENSITY_MEDIUM = 150
        const val INTENSITY_HIGH = 255
    }
    
    init {
        initializeVibrator()
    }
    
    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    fun updateIntensity(intensity: Int) {
        currentIntensity = intensity.coerceIn(1, 3)
    }
    
    private fun getVibrationAmplitude(): Int {
        return when (currentIntensity) {
            1 -> INTENSITY_LOW
            2 -> INTENSITY_MEDIUM
            3 -> INTENSITY_HIGH
            else -> INTENSITY_MEDIUM
        }
    }
    
    fun vibrateForInhale() {
        performVibration(VibrationPattern.INHALE)
    }
    
    fun vibrateForExhale() {
        performVibration(VibrationPattern.EXHALE)
    }
    
    fun vibrateForHold() {
        performVibration(VibrationPattern.HOLD)
    }
    
    fun vibrateForPhaseTransition() {
        performVibration(VibrationPattern.PHASE_TRANSITION)
    }
    
    fun vibrateForSessionStart() {
        performVibration(VibrationPattern.SESSION_START)
    }
    
    fun vibrateForSessionEnd() {
        performVibration(VibrationPattern.SESSION_END)
    }
    
    private fun performVibration(pattern: VibrationPattern) {
        vibrator?.let { vib ->
            if (!vib.hasVibrator()) return
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = when (pattern) {
                        VibrationPattern.INHALE -> createPulseEffect(
                            longArrayOf(0, VIBRATION_DURATION_MEDIUM),
                            intArrayOf(0, getVibrationAmplitude())
                        )
                        VibrationPattern.EXHALE -> createPulseEffect(
                            longArrayOf(0, VIBRATION_DURATION_LONG),
                            intArrayOf(0, (getVibrationAmplitude() * 0.8).toInt())
                        )
                        VibrationPattern.HOLD -> createPulseEffect(
                            longArrayOf(0, VIBRATION_DURATION_SHORT, 100, VIBRATION_DURATION_SHORT),
                            intArrayOf(0, getVibrationAmplitude(), 0, getVibrationAmplitude())
                        )
                        VibrationPattern.PHASE_TRANSITION -> createPulseEffect(
                            longArrayOf(0, 100, 100, 100, 100, 200),
                            intArrayOf(0, getVibrationAmplitude(), 0, getVibrationAmplitude(), 0, getVibrationAmplitude())
                        )
                        VibrationPattern.SESSION_START -> createPulseEffect(
                            longArrayOf(0, 500),
                            intArrayOf(0, getVibrationAmplitude())
                        )
                        VibrationPattern.SESSION_END -> createPulseEffect(
                            longArrayOf(0, 200, 200, 200, 200, 400),
                            intArrayOf(0, getVibrationAmplitude(), 0, getVibrationAmplitude(), 0, getVibrationAmplitude())
                        )
                    }
                    vib.vibrate(effect)
                } else {
                    // Fallback for older API levels
                    @Suppress("DEPRECATION")
                    when (pattern) {
                        VibrationPattern.INHALE -> vib.vibrate(VIBRATION_DURATION_MEDIUM)
                        VibrationPattern.EXHALE -> vib.vibrate(VIBRATION_DURATION_LONG)
                        VibrationPattern.HOLD -> vib.vibrate(longArrayOf(0, VIBRATION_DURATION_SHORT, 100, VIBRATION_DURATION_SHORT), -1)
                        VibrationPattern.PHASE_TRANSITION -> vib.vibrate(longArrayOf(0, 100, 100, 100, 100, 200), -1)
                        VibrationPattern.SESSION_START -> vib.vibrate(500)
                        VibrationPattern.SESSION_END -> vib.vibrate(longArrayOf(0, 200, 200, 200, 200, 400), -1)
                    }
                }
            } catch (e: Exception) {
                // Handle vibration errors gracefully
            }
        }
    }
    
    private fun createPulseEffect(timings: LongArray, amplitudes: IntArray): VibrationEffect? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    fun performCustomVibration(timings: LongArray, amplitudes: IntArray? = null) {
        vibrator?.let { vib ->
            if (!vib.hasVibrator()) return
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && amplitudes != null) {
                    val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                    vib.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(timings, -1)
                }
            } catch (e: Exception) {
                // Handle vibration errors gracefully
            }
        }
    }
    
    fun startContinuousVibration() {
        coroutineScope.launch {
            try {
                performVibration(VibrationPattern.SESSION_START)
            } catch (e: Exception) {
                // Handle vibration errors gracefully
            }
        }
    }
    
    fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Handle cancellation errors gracefully
        }
    }
    
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() ?: false
    }
    
    enum class VibrationPattern {
        INHALE,
        EXHALE,
        HOLD,
        PHASE_TRANSITION,
        SESSION_START,
        SESSION_END
    }
}