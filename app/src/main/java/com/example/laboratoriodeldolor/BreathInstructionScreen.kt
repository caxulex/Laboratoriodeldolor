package com.example.laboratoriodeldolor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun BreathInstructionScreen(id: String, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val (title, body) = when (id.lowercase()) {
        "enamorado" -> stringResource(id = R.string.breath_enamorado_title) to stringResource(id = R.string.breath_enamorado_body)
        "chilindrina" -> stringResource(id = R.string.breath_chilindrina_title) to stringResource(id = R.string.breath_chilindrina_body)
        "cuadrado" -> stringResource(id = R.string.breath_cuadrado_title) to stringResource(id = R.string.breath_cuadrado_body)
        else -> stringResource(id = R.string.breath_title) to stringResource(id = R.string.recent_exercises) // simple fallback
    }

    // Timer state for 4-4-4-4 (only visible for "cuadrado"). Phase seconds are configurable (3–6).
    var running by remember { mutableStateOf(false) }
    var phaseIndex by remember { mutableStateOf(0) } // 0: Inhala, 1: Sostén, 2: Exhala, 3: Sostén
    var secondsLeft by remember { mutableStateOf(4) }
    var phaseSeconds by remember { mutableStateOf(4) }
    val phases = listOf(
        stringResource(id = R.string.phase_inhale),
        stringResource(id = R.string.phase_hold),
        stringResource(id = R.string.phase_exhale),
        stringResource(id = R.string.phase_hold)
    )

    // Gentle haptic each phase change (Compose haptics, no permission required)
    LaunchedEffect(running, phaseIndex, id) {
        if (id.lowercase() == "cuadrado" && running) {
            try {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (_: Throwable) {
                // no-op
            }
        }
    }

    // Load preferred phase seconds from repository when entering the screen
    LaunchedEffect(id) {
        if (id.lowercase() == "cuadrado") {
            val app = (ctx.applicationContext as? MoodApplication)
            val repo = app?.preferencesRepository
            try {
                val s = repo?.boxPhaseSecondsFlow?.firstOrNull() ?: 4
                phaseSeconds = s.coerceIn(3, 6)
                secondsLeft = phaseSeconds
            } catch (_: Throwable) {
                phaseSeconds = 4
                secondsLeft = phaseSeconds
            }
        }
    }

    // Tick loop
    LaunchedEffect(running, id) {
        if (id.lowercase() != "cuadrado") return@LaunchedEffect
        while (true) {
            if (!running) { delay(100L); continue }
            delay(1000L)
            secondsLeft -= 1
            if (secondsLeft <= 0) {
                phaseIndex = (phaseIndex + 1) % 4
                secondsLeft = phaseSeconds
            }
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
                    Canvas(modifier = Modifier
                        .padding(8.dp)
                        .height(160.dp)) {
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
                            0 -> drawLine(color = activeColorCanvas, start = Offset(left, top), end = Offset(right, top), strokeWidth = stroke)
                            1 -> drawLine(color = activeColorCanvas, start = Offset(right, top), end = Offset(right, bottom), strokeWidth = stroke)
                            2 -> drawLine(color = activeColorCanvas, start = Offset(right, bottom), end = Offset(left, bottom), strokeWidth = stroke)
                            3 -> drawLine(color = activeColorCanvas, start = Offset(left, bottom), end = Offset(left, top), strokeWidth = stroke)
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
                            app?.let { application ->
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
                Button(onClick = onBack) { Text(text = stringResource(id = R.string.back_button)) }
            }
        }
    }
}

@Composable
private fun RowControls(running: Boolean, onStartPause: () -> Unit, onReset: () -> Unit) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onStartPause) {
            Text(text = if (running) stringResource(id = R.string.pause_timer) else stringResource(id = R.string.start_timer))
        }
        Button(onClick = onReset) {
            Text(text = stringResource(id = R.string.reset_timer))
        }
    }
}

@Composable
private fun DurationControls(seconds: Int, onChange: (Int) -> Unit) {
    Column {
        Text(text = stringResource(id = R.string.box_breath_phase_duration_title), style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onChange(seconds - 1) }, enabled = seconds > 3) { Text(text = "-1") }
            Text(text = stringResource(id = R.string.seconds_abbrev, seconds), style = MaterialTheme.typography.titleMedium)
            Button(onClick = { onChange(seconds + 1) }, enabled = seconds < 6) { Text(text = "+1") }
        }
    }
}

