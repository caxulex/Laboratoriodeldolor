package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * RecommendationViewModel
 * - Inspects recent MoodEntry and PainPoint records to produce a single Recommendation.
 * - Logic is rule-based and easily extendable (add more rules in computeRecommendation).
 */
class RecommendationViewModel(
    private val moodDao: MoodDao,
    private val painDao: PainPointDao
) : ViewModel() {

    private val _recommendation = MutableStateFlow<Recommendation?>(null)
    val recommendation: StateFlow<Recommendation?> = _recommendation

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val recentMoods = moodDao.getRecent(14) // last 14 entries/days
            val recentPains = painDao.getAllSnapshot() // latest snapshot

            // Compute recommendation based on current data
            val rec = computeRecommendation(recentMoods, recentPains)
            _recommendation.value = rec
        }
    }

    private suspend fun computeRecommendation(moods: List<MoodEntry>, pains: List<PainPoint>): Recommendation? {
        if (moods.isEmpty() && pains.isEmpty()) return null

        // Mood-based recommendations remain as before
        val recentMoodScores = moods.takeLast(7).mapNotNull { it.moodScore }
        if (recentMoodScores.isNotEmpty()) {
            val sadStreak = longestConsecutiveStreakScores(recentMoodScores, threshold = 2)
            if (sadStreak >= 3) {
                return Recommendation(
                    id = "workshop_a",
                    title = "Taller: Manejo Emocional",
                    description = "Has mostrado estados tristes consecutivos. Este taller corto ofrece técnicas de regulación emocional y prácticas diarias.",
                    iconResName = null
                )
            }
        }

        // Analyze pain points by view and y-coordinate to determine affected body regions and side
        if (pains.isNotEmpty()) {
            // Group by view (front/back)
            val byView = pains.groupBy { it.view }

            // Simple heuristics:
            // - y in [0, 0.33) => upper, [0.33,0.66) => middle, [0.66,1] => lower
            // - x < 0.5 => left, x > 0.5 => right, x approx 0.5 => center

            data class AreaKey(val view: String, val band: String, val side: String)

            val affectedAreas = mutableSetOf<AreaKey>()

            for ((viewName, pts) in byView) {
                for (p in pts) {
                    val band = when {
                        p.y < 0.33f -> "upper"
                        p.y < 0.66f -> "middle"
                        else -> "lower"
                    }
                    val side = when {
                        p.x < 0.45f -> "left"
                        p.x > 0.55f -> "right"
                        else -> "center"
                    }
                    affectedAreas.add(AreaKey(viewName, band, side))
                }
            }

            // Prioritize areas: if upper-front pain -> UpperBodyExercise
            if (affectedAreas.any { it.band == "upper" && it.view == "front" }) {
                return Recommendation(
                    id = "upper_front",
                    title = "Rutina - Parte Superior (Frente)",
                    description = "Dolor en la zona superior frontal detectado. Se recomiendan ejercicios para cuello, hombros y pectorales.",
                    iconResName = null
                )
            }

            // If upper-back pain
            if (affectedAreas.any { it.band == "upper" && it.view == "back" }) {
                return Recommendation(
                    id = "upper_back",
                    title = "Rutina - Parte Superior (Espalda)",
                    description = "Dolor en la parte superior de la espalda detectado. Se recomiendan estiramientos para trapecio y dorsal.",
                    iconResName = null
                )
            }

            // Middle-band recommendations
            if (affectedAreas.any { it.band == "middle" }) {
                return Recommendation(
                    id = "middle",
                    title = "Rutina - Zona Media",
                    description = "Dolor en la zona media detectado. Ejercicios para movilidad del tronco y columna.",
                    iconResName = null
                )
            }

            // Lower-band recommendations
            if (affectedAreas.any { it.band == "lower" }) {
                return Recommendation(
                    id = "lower",
                    title = "Rutina - Parte Inferior",
                    description = "Dolor en la parte inferior detectado. Ejercicios para piernas y glúteos.",
                    iconResName = null
                )
            }
        }

        // Fallback: breathing if mood low
        val moodScoresForAvg = moods.mapNotNull { it.moodScore }
        if (moodScoresForAvg.isNotEmpty()) {
            val avg = moodScoresForAvg.average()
            if (avg <= 2.5) {
                return Recommendation(
                    id = "breathing",
                    title = "Ejercicio de respiración",
                    description = "Prueba 5 minutos de respiración consciente para calmar el sistema nervioso.",
                    iconResName = null
                )
            }
        }

        return Recommendation(
            id = "general",
            title = "Sugerencia general",
            description = "Mantén tu hábito: pequeñas sesiones de autocuidado pueden marcar la diferencia.",
            iconResName = null
        )
    }

    private fun longestConsecutiveStreak(emojis: List<String>, target: Set<String>): Int {
        var best = 0
        var current = 0
        for (e in emojis.reversed()) {
            if (target.contains(e)) {
                current++
                best = maxOf(best, current)
            } else {
                current = 0
            }
        }
        return best
    }

    private fun longestConsecutiveStreakScores(scores: List<Int>, threshold: Int): Int {
        var best = 0
        var current = 0
        for (s in scores.reversed()) {
            if (s <= threshold) {
                current++
                best = maxOf(best, current)
            } else {
                current = 0
            }
        }
        return best
    }

    // Legacy mapping removed — use moodScore directly from MoodEntry
}
