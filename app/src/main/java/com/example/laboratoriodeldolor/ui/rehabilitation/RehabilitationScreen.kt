package com.example.laboratoriodeldolor.ui.rehabilitation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laboratoriodeldolor.MoodApplication
import com.example.laboratoriodeldolor.R
import com.example.laboratoriodeldolor.data.rehabilitation.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RehabilitationScreen(
    onNavigateToCategory: (String) -> Unit = {},
    onNavigateToExercise: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as MoodApplication
    val viewModel: RehabilitationViewModel = viewModel(
        factory = RehabilitationViewModelFactory(app.rehabilitationRepository)
    )
    
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val categoryProgress by viewModel.categoryProgress.collectAsState()
    val recentSessions by viewModel.recentSessions.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getRecommendedExercises()
    }
    
    com.example.laboratoriodeldolor.ui.AppScaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rehabilitación",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Stats Overview Card
            item {
                StatsOverviewCard(
                    stats = uiState.stats,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Categories Section
            item {
                Text(
                    text = "Categorías de Ejercicios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(categories) { category ->
                val progress = categoryProgress.find { it.categoryId == category.id }
                CategoryCard(
                    category = category,
                    totalSessions = progress?.totalSessions ?: 0,
                    completedToday = progress?.completedToday ?: false,
                    onClick = { onNavigateToCategory(category.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Recent Sessions Section
            if (recentSessions.isNotEmpty()) {
                item {
                    Text(
                        text = "Sesiones Recientes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                items(recentSessions.take(5)) { session ->
                    RecentSessionCard(
                        session = session,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Error/Success Messages
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
    
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or handle success
            viewModel.clearSuccess()
        }
    }
}

@Composable
fun StatsOverviewCard(
    stats: RehabilitationStats?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Progreso General",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "Sesiones Totales",
                    value = "${stats?.totalSessions ?: 0}"
                )
                StatItem(
                    label = "Racha Actual",
                    value = "${stats?.currentStreak ?: 0} días"
                )
                StatItem(
                    label = "Promedio Semanal",
                    value = String.format("%.1f", stats?.weeklyAverage ?: 0f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: RehabilitationCategory,
    totalSessions: Int,
    completedToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (completedToday) 
                MaterialTheme.colorScheme.secondaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon (placeholder - you can add actual icons)
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(android.graphics.Color.parseColor(category.colorHex))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (category.id) {
                            "foot_ankle" -> Icons.Default.DirectionsWalk
                            "eye_convergence" -> Icons.Default.RemoveRedEye
                            else -> Icons.Default.FitnessCenter
                        },
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                if (totalSessions > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalSessions sesiones completadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (completedToday) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completado hoy",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun RecentSessionCard(
    session: SessionSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = session.exerciseTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${session.categoryName} • ${session.durationSeconds / 60} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            session.painLevel?.let { pain ->
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "Dolor: $pain",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    }
}