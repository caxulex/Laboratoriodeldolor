package com.example.laboratoriodeldolor.ui.rehabilitation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laboratoriodeldolor.MoodApplication
import com.example.laboratoriodeldolor.data.rehabilitation.*
import kotlinx.coroutines.delay

@Composable
fun ExerciseSessionScreen(
    exerciseId: String,
    onNavigateBack: () -> Unit = {},
    onSessionComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as MoodApplication
    val viewModel: RehabilitationViewModel = viewModel(
        factory = RehabilitationViewModelFactory(app.rehabilitationRepository)
    )
    
    val exerciseState by viewModel.exerciseState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Load exercise when screen opens
    LaunchedEffect(exerciseId) {
        val exercise = app.rehabilitationRepository.getExerciseById(exerciseId)
        exercise?.let { viewModel.selectExercise(it) }
    }
    
    // Timer countdown effect
    LaunchedEffect(exerciseState.isRunning, exerciseState.timeRemaining) {
        if (exerciseState.isRunning && exerciseState.timeRemaining > 0) {
            delay(1000L)
            viewModel.updateTimer(exerciseState.timeRemaining - 1)
        }
    }
    
    // Handle completion dialog
    if (uiState.showCompletionDialog) {
        ExerciseCompletionDialog(
            onComplete = { painLevel, difficulty, notes ->
                viewModel.recordSession(painLevel, difficulty, notes)
                onSessionComplete()
            },
            onDismiss = {
                viewModel.dismissCompletionDialog()
                onSessionComplete()
            }
        )
    }
    
    com.example.laboratoriodeldolor.ui.AppScaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseState.exercise?.title ?: "Ejercicio",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Paso ${exerciseState.currentStep} de ${exerciseState.totalSteps}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main content
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Timer circle
                TimerCircle(
                    timeRemaining = exerciseState.timeRemaining,
                    totalTime = exerciseState.exercise?.durationSeconds ?: 60,
                    isRunning = exerciseState.isRunning
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Instructions
                exerciseState.exercise?.let { exercise ->
                    InstructionsCard(
                        instructions = exercise.instructions,
                        currentStep = exerciseState.currentStep,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Progress indicator
                LinearProgressIndicator(
                    progress = { (exerciseState.currentStep.toFloat() / exerciseState.totalSteps) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            
            // Control buttons
            ExerciseControls(
                isRunning = exerciseState.isRunning,
                isCompleted = exerciseState.isCompleted,
                onStart = { viewModel.startExercise() },
                onPause = { viewModel.pauseExercise() },
                onResume = { viewModel.resumeExercise() },
                onNext = { viewModel.nextStep() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimerCircle(
    timeRemaining: Int,
    totalTime: Int,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTime > 0) {
        (totalTime - timeRemaining).toFloat() / totalTime.toFloat()
    } else 0f
    
    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            strokeWidth = 8.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(timeRemaining),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = if (isRunning) "En progreso" else "Pausado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun InstructionsCard(
    instructions: String,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Instrucciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun ExerciseControls(
    isRunning: Boolean,
    isCompleted: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (!isRunning && !isCompleted) {
            Button(
                onClick = if (isRunning) onResume else onStart,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar")
            }
        } else if (isRunning) {
            Button(
                onClick = onPause,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pausar")
            }
        }
        
        if (isRunning || isCompleted) {
            Spacer(modifier = Modifier.width(16.dp))
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.SkipNext,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isCompleted) "Completar" else "Siguiente")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCompletionDialog(
    onComplete: (Int?, Int?, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var painLevel by remember { mutableIntStateOf(0) }
    var difficultyRating by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("¡Ejercicio Completado!")
        },
        text = {
            Column {
                Text("¿Cómo fue tu experiencia?")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pain level
                Text(
                    text = "Nivel de dolor (0-10): $painLevel",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = painLevel.toFloat(),
                    onValueChange = { painLevel = it.toInt() },
                    valueRange = 0f..10f,
                    steps = 9
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Difficulty rating
                Text(
                    text = "Dificultad percibida (1-5): $difficultyRating",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = difficultyRating.toFloat(),
                    onValueChange = { difficultyRating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onComplete(
                        if (painLevel > 0) painLevel else null,
                        difficultyRating,
                        if (notes.isNotBlank()) notes else null
                    )
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Omitir")
            }
        }
    )
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}