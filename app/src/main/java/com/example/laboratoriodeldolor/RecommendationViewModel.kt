package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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

        // Prepare context useful for rules
        val context = RecommendationContext(moods = moods, pains = pains)

        // Define data-driven rules (order matters: first matching rule wins)
        val rules: List<RecommendationRule> = listOf(
            // Emotional workshop if 3+ low scores in recent 7 entries
            RecommendationRule(
                id = "workshop_a",
                matches = { ctx ->
                    val recentMoodScores = ctx.moods.takeLast(7).mapNotNull { it.moodScore }
                    recentMoodScores.isNotEmpty() && longestConsecutiveStreakScores(recentMoodScores, threshold = 2) >= 3
                },
                build = { Recommendation(
                    id = "workshop_a",
                    title = "Taller: Manejo Emocional",
                    description = "Has mostrado estados tristes consecutivos. Este taller corto ofrece técnicas de regulación emocional y prácticas diarias.",
                    iconResName = null
                ) }
            ),
            // Upper-front or upper-back pain (use descriptive mapped locations)
            RecommendationRule(
                id = "upper_front_or_back",
                matches = { ctx ->
                    ctx.pains.any { pp ->
                        val k = mapPainPointToLocationKey(pp)
                        k == PainLocationKey.FRONT_UPPER || k == PainLocationKey.BACK_UPPER
                    }
                },
                build = { Recommendation(
                    id = "upper_front_or_back",
                    title = "Rutina - Parte Superior",
                    description = "Dolor en la zona superior detectado. Se recomiendan ejercicios para cuello, hombros y pectorales/espalda según corresponda.",
                    iconResName = null
                ) }
            ),
            // Middle band
            RecommendationRule(
                id = "middle",
                matches = { ctx -> ctx.pains.any { pp ->
                    val k = mapPainPointToLocationKey(pp)
                    k == PainLocationKey.FRONT_MIDDLE || k == PainLocationKey.BACK_MIDDLE || k == PainLocationKey.FRONT_MIDDLE
                } },
                build = { Recommendation(
                    id = "middle",
                    title = "Rutina - Zona Media",
                    description = "Dolor en la zona media detectado. Ejercicios para movilidad del tronco y columna.",
                    iconResName = null
                ) }
            ),
            // Lower band
            RecommendationRule(
                id = "lower",
                matches = { ctx -> ctx.pains.any { pp ->
                    val k = mapPainPointToLocationKey(pp)
                    k == PainLocationKey.FRONT_LOWER || k == PainLocationKey.BACK_LOWER
                } },
                build = { Recommendation(
                    id = "lower",
                    title = "Rutina - Parte Inferior",
                    description = "Dolor en la parte inferior detectado. Ejercicios para piernas y glúteos.",
                    iconResName = null
                ) }
            ),
            // Breathing if avg mood low
            RecommendationRule(
                id = "breathing",
                matches = { ctx ->
                    val moodScoresForAvg = ctx.moods.mapNotNull { it.moodScore }
                    moodScoresForAvg.isNotEmpty() && moodScoresForAvg.average() <= 2.5
                },
                build = { Recommendation(
                    id = "breathing",
                    title = "Ejercicio de respiración",
                    description = "Prueba 5 minutos de respiración consciente para calmar el sistema nervioso.",
                    iconResName = null
                ) }
            )
        )

        // Evaluate rules in order
        for (r in rules) {
            if (r.matches(context)) return r.build()
        }

        // Default fallback
        return Recommendation(
            id = "general",
            title = "Sugerencia general",
            description = "Mantén tu hábito: pequeñas sesiones de autocuidado pueden marcar la diferencia.",
            iconResName = null
        )
    }

    // --- Data-driven rule helpers ---
    private data class RecommendationRule(
        val id: String,
        val matches: (RecommendationContext) -> Boolean,
        val build: () -> Recommendation
    )

    private data class RecommendationContext(
        val moods: List<MoodEntry>,
        val pains: List<PainPoint>
    ) {
        fun affectedAreas(): Set<AreaKey> {
            val pts = pains
            val byView = pts.groupBy { it.view }
            val affected = mutableSetOf<AreaKey>()
            for ((viewName, list) in byView) {
                for (p in list) {
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
                    affected.add(AreaKey(viewName, band, side))
                }
            }
            return affected
        }
    }

    private data class AreaKey(val view: String, val band: String, val side: String)

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
