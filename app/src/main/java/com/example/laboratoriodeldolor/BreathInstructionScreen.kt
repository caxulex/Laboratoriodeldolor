package com.example.laboratoriodeldolor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.laboratoriodeldolor.ui.AppScaffold
import com.example.laboratoriodeldolor.managers.BreathingAudioManager
import com.example.laboratoriodeldolor.managers.BreathingVibrationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect

@Composable
fun BreathInstructionScreen(id: String, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Initialize audio and vibration managers
    val audioManager = remember { BreathingAudioManager(ctx, scope) }
    val vibrationManager = remember { BreathingVibrationManager(ctx, scope) }
    
    val (title, body) = when (id.lowercase()) {
        "enamorado" -> stringResource(id = R.string.breath_enamorado_title) to stringResource(id = R.string.breath_enamorado_body)
        "chilindrina" -> stringResource(id = R.string.breath_chilindrina_title) to stringResource(id = R.string.breath_chilindrina_body)
        "cuadrado" -> stringResource(id = R.string.breath_cuadrado_title) to stringResource(id = R.string.breath_cuadrado_body)
        "retencion" -> stringResource(id = R.string.breath_retencion_title) to stringResource(id = R.string.breath_retencion_body)
        else -> stringResource(id = R.string.breath_title) to stringResource(id = R.string.recent_exercises) // simple fallback
    }

    // Timer state for both "cuadrado" (4-4-4-4) and "retencion" (6-8-10-2)
    var running by remember { mutableStateOf(false) }
    var phaseIndex by remember { mutableStateOf(0) }
    var secondsLeft by remember { mutableStateOf(4) }
    var phaseSeconds by remember { mutableStateOf(4) }
    
    // Audio and vibration preferences
    var audioEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var audioVolume by remember { mutableStateOf(0.7f) }
    var vibrationIntensity by remember { mutableStateOf(2) }
    
    val (phases, phaseDurations) = when (id.lowercase()) {
        "cuadrado" -> {
            val phases = listOf(
                stringResource(id = R.string.phase_inhale),
                stringResource(id = R.string.phase_hold),
                stringResource(id = R.string.phase_exhale),
                stringResource(id = R.string.phase_hold)
            )
            phases to listOf(4, 4, 4, 4)
        }
        "retencion" -> {
            val phases = listOf(
                stringResource(id = R.string.phase_inhale),
                stringResource(id = R.string.phase_hold),
                stringResource(id = R.string.phase_exhale),
                "Pausa"
            )
            phases to listOf(6, 8, 10, 2)
        }
        else -> {
            val phases = listOf(
                stringResource(id = R.string.phase_inhale),
                stringResource(id = R.string.phase_hold),
                stringResource(id = R.string.phase_exhale),
                stringResource(id = R.string.phase_hold)
            )
            phases to listOf(4, 4, 4, 4)
        }
    }

    // Initialize managers and load preferences
    LaunchedEffect(Unit) {
        val app = (ctx.applicationContext as? MoodApplication)
        val repo = app?.preferencesRepository
        try {
            audioEnabled = repo?.breathingAudioEnabledFlow?.firstOrNull() ?: true
            vibrationEnabled = repo?.breathingVibrationEnabledFlow?.firstOrNull() ?: true
            audioVolume = repo?.breathingAudioVolumeFlow?.firstOrNull() ?: 0.7f
            vibrationIntensity = repo?.breathingVibrationIntensityFlow?.firstOrNull() ?: 2
            
            audioManager.initialize(audioVolume)
            vibrationManager.updateIntensity(vibrationIntensity)
            
            if (id.lowercase() == "cuadrado") {
                val s = repo?.boxPhaseSecondsFlow?.firstOrNull() ?: 4
                phaseSeconds = s.coerceIn(3, 6)
                secondsLeft = phaseSeconds
            } else if (id.lowercase() == "retencion") {
                secondsLeft = phaseDurations[0]
            }
        } catch (_: Throwable) {
            phaseSeconds = 4
            secondsLeft = if (id.lowercase() == "retencion") 6 else phaseSeconds
        }
    }
    
    // Enhanced phase transition with audio and vibration
    LaunchedEffect(running, phaseIndex, id) {
        if ((id.lowercase() == "cuadrado" || id.lowercase() == "retencion") && running) {
            try {
                // Play phase-specific sounds
                if (audioEnabled) {
                    when (phaseIndex) {
                        0 -> audioManager.playInhaleSound()
                        1 -> audioManager.playHoldSound()
                        2 -> audioManager.playExhaleSound()
                        3 -> audioManager.playPhaseTransitionSound()
                    }
                }
                
                // Play phase-specific vibrations
                if (vibrationEnabled) {
                    when (phaseIndex) {
                        0 -> vibrationManager.vibrateForInhale()
                        1 -> vibrationManager.vibrateForHold()
                        2 -> vibrationManager.vibrateForExhale()
                        3 -> vibrationManager.vibrateForPhaseTransition()
                    }
                }
                
                // Fallback haptic feedback if vibration is disabled
                if (!vibrationEnabled) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            } catch (_: Throwable) {
                // Fallback to basic haptic
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    // Enhanced tick loop for multiple breathing exercises
    LaunchedEffect(running, id) {
        if (id.lowercase() != "cuadrado" && id.lowercase() != "retencion") return@LaunchedEffect
        while (true) {
            if (!running) { delay(100L); continue }
            delay(1000L)
            secondsLeft -= 1
            if (secondsLeft <= 0) {
                phaseIndex = (phaseIndex + 1) % phases.size
                secondsLeft = if (id.lowercase() == "retencion") {
                    phaseDurations[phaseIndex]
                } else {
                    phaseSeconds
                }
            }
        }
    }
    
    // Cleanup managers when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            audioManager.release()
            vibrationManager.stopVibration()
        }
    }

    AppScaffold { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding), color = Color.Transparent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = body, style = MaterialTheme.typography.bodyLarge)

                if (id.lowercase() == "cuadrado") {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = stringResource(id = R.string.box_breath_timer_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Pre-resolve colors from MaterialTheme (cannot call @Composable inside Canvas draw)
                    val baseColorCanvas = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    val activeColorCanvas = MaterialTheme.colorScheme.primary

                    // Simple animated square: highlights current side based on phase
                    val targetStroke = if (running) 16f + (phaseIndex % 2) * 2f else 12f
                    val animatedStroke by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = targetStroke,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 240),
                        label = "BreathStroke"
                    )

                    Canvas(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .sizeIn(maxWidth = 220.dp)
                        .align(Alignment.CenterHorizontally)) {
                        val w = size.width
                        val h = size.height
                        val pad = 20f
                        val left = pad
                        val top = pad
                        val right = w - pad
                        val bottom = h - pad
                        val stroke = 12f

                        // Draw all sides faintly
                        drawLine(color = baseColorCanvas, start = Offset(left, top), end = Offset(right, top), strokeWidth = stroke)
                        drawLine(color = baseColorCanvas, start = Offset(right, top), end = Offset(right, bottom), strokeWidth = stroke)
                        drawLine(color = baseColorCanvas, start = Offset(right, bottom), end = Offset(left, bottom), strokeWidth = stroke)
                        drawLine(color = baseColorCanvas, start = Offset(left, bottom), end = Offset(left, top), strokeWidth = stroke)

                        // Highlight current side
                        when (phaseIndex) {
                            0 -> drawLine(color = activeColorCanvas, start = Offset(left, top), end = Offset(right, top), strokeWidth = animatedStroke)
                            1 -> drawLine(color = activeColorCanvas, start = Offset(right, top), end = Offset(right, bottom), strokeWidth = animatedStroke)
                            2 -> drawLine(color = activeColorCanvas, start = Offset(right, bottom), end = Offset(left, bottom), strokeWidth = animatedStroke)
                            3 -> drawLine(color = activeColorCanvas, start = Offset(left, bottom), end = Offset(left, top), strokeWidth = animatedStroke)
                        }
                    }

                    // Phase + seconds remaining (announce politely for accessibility)
                    val phaseDesc = phases[phaseIndex]
                    Text(
                        text = phaseDesc,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics {
                            contentDescription = phaseDesc
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                    val secondsText = stringResource(id = R.string.seconds_abbrev, secondsLeft)
                    Text(
                        text = secondsText,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.semantics {
                            contentDescription = secondsText
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Duration controls 3–6 seconds, persisted
                    DurationControls(
                        seconds = phaseSeconds,
                        onChange = { newVal ->
                            val v = newVal.coerceIn(3, 6)
                            phaseSeconds = v
                            if (!running) secondsLeft = v else if (secondsLeft > v) secondsLeft = v
                            // Persist
                            val app = (ctx.applicationContext as? MoodApplication)
                            val repo = app?.preferencesRepository
                            app?.let { _ ->
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    try { repo?.setBoxPhaseSeconds(v) } catch (_: Throwable) {}
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Controls
                    RowControls(
                        running = running,
                        onStartPause = { running = !running },
                        onReset = { running = false; phaseIndex = 0; secondsLeft = phaseSeconds }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(text = stringResource(id = R.string.back_button), onClick = onBack)
            }
        }
    }
}

@Composable
private fun RowControls(running: Boolean, onStartPause: () -> Unit, onReset: () -> Unit) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(text = if (running) stringResource(id = R.string.pause_timer) else stringResource(id = R.string.start_timer), onClick = onStartPause)
        com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(text = stringResource(id = R.string.reset_timer), onClick = onReset)
    }
}

@Composable
private fun DurationControls(seconds: Int, onChange: (Int) -> Unit) {
    Column {
        Text(text = stringResource(id = R.string.box_breath_phase_duration_title), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(text = "-1", onClick = { onChange(seconds - 1) })
            Text(text = stringResource(id = R.string.seconds_abbrev, seconds), style = MaterialTheme.typography.titleMedium)
            com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(text = "+1", onClick = { onChange(seconds + 1) })
        }
    }
}

