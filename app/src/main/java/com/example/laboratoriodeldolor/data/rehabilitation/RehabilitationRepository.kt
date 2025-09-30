package com.example.laboratoriodeldolor.data.rehabilitation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class RehabilitationRepository(
    private val dao: RehabilitationDao
) {
    
    // Category operations
    fun getAllCategories(): Flow<List<RehabilitationCategory>> = dao.getAllCategories()
    
    suspend fun getCategoryById(categoryId: String): RehabilitationCategory? = 
        withContext(Dispatchers.IO) {
            dao.getCategoryById(categoryId)
        }
    
    suspend fun initializeDefaultCategories() = withContext(Dispatchers.IO) {
        val defaultCategories = listOf(
            RehabilitationCategory(
                id = "foot_ankle",
                name = "Pie y Tobillo",
                description = "Ejercicios específicos para fortalecer y mejorar la movilidad del pie y tobillo",
                iconResource = "ic_foot",
                colorHex = "#4CAF50",
                displayOrder = 1
            ),
            RehabilitationCategory(
                id = "eye_convergence",
                name = "Convergencia Ocular",
                description = "Ejercicios para mejorar la coordinación y convergencia de los ojos",
                iconResource = "ic_eye",
                colorHex = "#2196F3",
                displayOrder = 2
            )
        )
        dao.insertCategories(defaultCategories)
    }
    
    // Exercise operations
    fun getExercisesByCategory(categoryId: String): Flow<List<RehabilitationExercise>> = 
        dao.getExercisesByCategory(categoryId)
    
    suspend fun getExerciseById(exerciseId: String): RehabilitationExercise? = 
        withContext(Dispatchers.IO) {
            dao.getExerciseById(exerciseId)
        }
    
    fun getExercisesByDifficulty(maxDifficulty: Int): Flow<List<RehabilitationExercise>> = 
        dao.getExercisesByDifficulty(maxDifficulty)
    
    suspend fun initializeDefaultExercises() = withContext(Dispatchers.IO) {
        val footAnkleExercises = listOf(
            RehabilitationExercise(
                id = "ankle_circles",
                categoryId = "foot_ankle",
                title = "Círculos de Tobillo",
                description = "Ejercicio básico para mejorar la movilidad del tobillo",
                instructions = "Sentado o acostado, levanta el pie y realiza círculos lentos con el tobillo. Haz 10 círculos en cada dirección.",
                durationSeconds = 60,
                repetitions = 10,
                difficultyLevel = 1,
                displayOrder = 1
            ),
            RehabilitationExercise(
                id = "calf_raises",
                categoryId = "foot_ankle",
                title = "Elevación de Pantorrillas",
                description = "Fortalece los músculos de la pantorrilla y mejora la estabilidad",
                instructions = "De pie, eleva lentamente los talones del suelo, sostén por 2 segundos y baja lentamente.",
                durationSeconds = 90,
                repetitions = 15,
                difficultyLevel = 2,
                displayOrder = 2
            ),
            RehabilitationExercise(
                id = "toe_flexion",
                categoryId = "foot_ankle",
                title = "Flexión de Dedos",
                description = "Mejora la flexibilidad y fuerza de los dedos del pie",
                instructions = "Sentado, coloca una toalla en el suelo y usa los dedos del pie para agarrarla y soltarla.",
                durationSeconds = 120,
                repetitions = 20,
                difficultyLevel = 1,
                displayOrder = 3
            ),
            RehabilitationExercise(
                id = "balance_single_leg",
                categoryId = "foot_ankle",
                title = "Equilibrio en Una Pierna",
                description = "Mejora el equilibrio y la propriocepción",
                instructions = "De pie, levanta una pierna y mantén el equilibrio en la otra. Sostén por 30 segundos.",
                durationSeconds = 30,
                repetitions = 3,
                difficultyLevel = 3,
                displayOrder = 4
            )
        )
        
        val eyeConvergenceExercises = listOf(
            RehabilitationExercise(
                id = "pencil_pushups",
                categoryId = "eye_convergence",
                title = "Flexiones con Lápiz",
                description = "Ejercicio clásico para mejorar la convergencia ocular",
                instructions = "Sostén un lápiz a la distancia del brazo. Acércalo lentamente hacia la nariz manteniendo la imagen única.",
                durationSeconds = 60,
                repetitions = 10,
                difficultyLevel = 1,
                displayOrder = 1
            ),
            RehabilitationExercise(
                id = "dot_card",
                categoryId = "eye_convergence",
                title = "Tarjeta de Puntos",
                description = "Mejora la coordinación binocular usando una tarjeta con puntos",
                instructions = "Mira la tarjeta de puntos y enfoca en el punto central. Mueve la tarjeta lentamente hacia ti.",
                durationSeconds = 90,
                repetitions = 5,
                difficultyLevel = 2,
                displayOrder = 2
            ),
            RehabilitationExercise(
                id = "focus_shifts",
                categoryId = "eye_convergence",
                title = "Cambios de Enfoque",
                description = "Alterna el enfoque entre objetos cercanos y lejanos",
                instructions = "Coloca un dedo a 15cm de tu cara. Alterna el enfoque entre el dedo y un objeto lejano cada 3 segundos.",
                durationSeconds = 120,
                repetitions = 20,
                difficultyLevel = 2,
                displayOrder = 3
            ),
            RehabilitationExercise(
                id = "string_bead",
                categoryId = "eye_convergence",
                title = "Cuerda con Cuentas",
                description = "Ejercicio avanzado de convergencia usando una cuerda con cuentas",
                instructions = "Ata una cuerda con 3 cuentas a diferentes distancias. Enfoca en cada cuenta secuencialmente.",
                durationSeconds = 180,
                repetitions = 3,
                difficultyLevel = 4,
                displayOrder = 4
            )
        )
        
        dao.insertExercises(footAnkleExercises + eyeConvergenceExercises)
    }
    
    // Session operations
    suspend fun recordSession(session: RehabilitationSession): Long = 
        withContext(Dispatchers.IO) {
            val sessionId = dao.insertSession(session)
            
            // Update progress for the category
            val exercise = dao.getExerciseById(session.exerciseId)
            exercise?.let {
                updateProgressAfterSession(
                    categoryId = it.categoryId,
                    sessionDate = session.completedAt,
                    painLevel = session.painLevel?.toFloat() ?: 0f,
                    difficulty = session.difficultyRating?.toFloat() ?: it.difficultyLevel.toFloat()
                )
            }
            
            sessionId
        }
    
    suspend fun getRecentSessions(exerciseId: String, limit: Int = 10): List<RehabilitationSession> = 
        withContext(Dispatchers.IO) {
            dao.getRecentSessions(exerciseId, limit)
        }
    
    suspend fun getTodaySessionCount(): Int = 
        withContext(Dispatchers.IO) {
            dao.getTodaySessionCount()
        }
    
    fun getRecentSessionsWithDetails(): Flow<List<SessionSummary>> = 
        dao.getRecentSessionsWithDetails()
    
    // Progress operations
    suspend fun getProgressByCategory(categoryId: String): RehabilitationProgress? = 
        withContext(Dispatchers.IO) {
            dao.getProgressByCategory(categoryId)
        }
    
    fun getAllProgress(): Flow<List<RehabilitationProgress>> = dao.getAllProgress()
    
    fun getCategoryProgressOverview(): Flow<List<CategoryProgress>> = 
        dao.getCategoryProgressOverview()
    
    private suspend fun updateProgressAfterSession(
        categoryId: String,
        sessionDate: LocalDateTime,
        painLevel: Float,
        difficulty: Float
    ) {
        // Get existing progress or create new
        val existingProgress = dao.getProgressByCategory(categoryId)
        if (existingProgress == null) {
            val newProgress = RehabilitationProgress(
                categoryId = categoryId,
                currentLevel = 1,
                totalSessions = 1,
                lastSessionDate = sessionDate,
                averagePainLevel = painLevel,
                averageDifficulty = difficulty,
                streakDays = 1
            )
            dao.insertOrUpdateProgress(newProgress)
        } else {
            dao.updateProgressAfterSession(categoryId, sessionDate, painLevel, difficulty)
        }
    }
    
    // Preferences operations
    suspend fun getPreferences(): RehabilitationPreferences = 
        withContext(Dispatchers.IO) {
            dao.getPreferences() ?: RehabilitationPreferences()
        }
    
    suspend fun updatePreferences(preferences: RehabilitationPreferences) = 
        withContext(Dispatchers.IO) {
            dao.insertOrUpdatePreferences(preferences)
        }
    
    // Analytics
    suspend fun getOverallStats(): RehabilitationStats = 
        withContext(Dispatchers.IO) {
            dao.getOverallStats() ?: RehabilitationStats(
                totalSessions = 0,
                weeklyAverage = 0f,
                currentStreak = 0
            )
        }
    
    // Search
    suspend fun searchExercises(query: String): List<RehabilitationExercise> = 
        withContext(Dispatchers.IO) {
            dao.searchExercises(query)
        }
    
    // Data management
    suspend fun cleanupOldData(retentionDays: Int = 365) = 
        withContext(Dispatchers.IO) {
            val cutoffDate = LocalDateTime.now().minusDays(retentionDays.toLong())
            dao.deleteOldSessions(cutoffDate)
            dao.cleanupEmptyProgress()
        }
    
    // Recommendations
    suspend fun getRecommendedExercises(limit: Int = 5): List<RehabilitationExercise> = 
        withContext(Dispatchers.IO) {
            val preferences = getPreferences()
            val maxDifficulty = preferences.preferredDifficulty
            
            dao.getExercisesByDifficulty(maxDifficulty).firstOrNull()?.take(limit) ?: emptyList()
        }
    
    suspend fun getNextExerciseForCategory(categoryId: String): RehabilitationExercise? = 
        withContext(Dispatchers.IO) {
            val progress = dao.getProgressByCategory(categoryId)
            val exercises = dao.getExercisesByCategory(categoryId).firstOrNull() ?: return@withContext null
            
            // If no progress, return first exercise
            if (progress == null) return@withContext exercises.firstOrNull()
            
            // Find next exercise based on current level
            exercises.find { it.difficultyLevel == progress.currentLevel } 
                ?: exercises.firstOrNull { it.difficultyLevel > progress.currentLevel }
                ?: exercises.lastOrNull()
        }
}