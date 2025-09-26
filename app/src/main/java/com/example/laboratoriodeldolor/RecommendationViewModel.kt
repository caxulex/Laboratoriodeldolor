package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

/**
 * RecommendationViewModel
 * - Inspects recent MoodEntry and PainPoint records to produce a single Recommendation.
 * - Logic is rule-based and easily extendable.
 *
 * Important: this ViewModel never touches Android Context or resolves strings.
 * It returns resource IDs inside Recommendation and the UI should call stringResource() when rendering.
 */
class RecommendationViewModel(
    private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository,
    private val painPointRepository: com.example.laboratoriodeldolor.repository.PainPointRepository,
    private val routineRepository: com.example.laboratoriodeldolor.repository.RoutineRepository,
    private val routineStepRepository: com.example.laboratoriodeldolor.repository.RoutineStepRepository,
    private val techniqueRepository: com.example.laboratoriodeldolor.repository.TechniqueRepository
) : ViewModel() {

    private val _recommendation = MutableStateFlow<Recommendation?>(null)
    val recommendation: StateFlow<Recommendation?> = _recommendation

    // Derived: region keys suggested from pain points (Techniques categories)
    private val _regionKeys = MutableStateFlow<List<String>>(emptyList())
    val regionKeys: StateFlow<List<String>> = _regionKeys

    // Derived: techniques recommended (de-duplicated) from routines of those regions
    private val _techniques = MutableStateFlow<List<Technique>>(emptyList())
    val techniques: StateFlow<List<Technique>> = _techniques

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val recentMoods: List<MoodEntry>
            val recentPains: List<PainPoint>
            withContext(Dispatchers.IO) {
                recentMoods = moodRepository.getRecentMoodEntries(14)
                recentPains = painPointRepository.getAllPainPointsSnapshot()
            }

            val rec = withContext(Dispatchers.Default) { computeRecommendation(recentMoods, recentPains) }
            _recommendation.value = rec

            // Also compute region recommendations and techniques
            val regionKeys = withContext(Dispatchers.Default) { analyzePainPointsToRegionKeys(recentPains, maxRegions = 3) }
            _regionKeys.value = regionKeys

            // Load routines for those regions and collect referenced techniques (by id)
            val techniques = withContext(Dispatchers.IO) {
                // Fetch all routines once, filter by bodyRegion
                val routinesSnapshot = routineRepository.getAllRoutines().firstOrNull().orEmpty()
                val routineIds = routinesSnapshot.filter { rk -> regionKeys.contains(rk.bodyRegion) }.map { it.id }
                val techniqueIds = mutableSetOf<Long>()
                for (rid in routineIds) {
                    val steps = routineStepRepository.getStepsForRoutine(rid).firstOrNull().orEmpty()
                    for (s in steps) {
                        val tid = s.techniqueId
                        if (tid != null) techniqueIds.add(tid)
                    }
                }
                // Resolve Technique records for ids, keep insertion order
                val allTech = techniqueRepository.getAllTechniques().firstOrNull().orEmpty()
                allTech.filter { techniqueIds.contains(it.id) }
            }
            _techniques.value = techniques
        }
    }

    private suspend fun computeRecommendation(moods: List<MoodEntry>, pains: List<PainPoint>): Recommendation? {
        if (moods.isEmpty() && pains.isEmpty()) return null

        val ctx = RecommendationContext(moods = moods, pains = pains)

        // Rules evaluated in order
        val rules = listOf(
            RecommendationRule(
                id = "workshop_a",
                matches = { c ->
                    val recentMoodScores = c.moods.takeLast(7).mapNotNull { it.moodScore }
                    recentMoodScores.isNotEmpty() && longestConsecutiveStreakScores(recentMoodScores, threshold = 2) >= 3
                },
                build = { _ ->
                    Recommendation(
                        id = "workshop_a",
                        titleResId = R.string.breath_title,
                        descriptionResId = R.string.recommendation_no_data_subtitle,
                        iconResName = null
                    )
                }
            ),
            RecommendationRule(
                id = "upper_front_or_back",
                matches = { c ->
                    c.pains.any { pp ->
                        val k = mapPainPointToLocationKey(pp)
                        k == PainLocationKey.FRONT_UPPER || k == PainLocationKey.BACK_UPPER
                    }
                },
                build = { c ->
                    Recommendation(
                        id = "upper_front_or_back",
                        titleResId = R.string.upper_body_routine_title,
                        descriptionResId = R.string.recommendation_no_data_subtitle,
                        iconResName = null
                    )
                }
            ),
            RecommendationRule(
                id = "middle",
                matches = { c -> c.pains.any { pp ->
                    val k = mapPainPointToLocationKey(pp)
                    k == PainLocationKey.FRONT_MIDDLE || k == PainLocationKey.BACK_MIDDLE
                } },
                build = { c ->
                    Recommendation(
                        id = "middle",
                        titleResId = R.string.middle_body_routine_title,
                        descriptionResId = R.string.recommendation_no_data_subtitle,
                        iconResName = null
                    )
                }
            ),
            RecommendationRule(
                id = "lower",
                matches = { c -> c.pains.any { pp ->
                    val k = mapPainPointToLocationKey(pp)
                    k == PainLocationKey.FRONT_LOWER || k == PainLocationKey.BACK_LOWER
                } },
                build = { c ->
                    Recommendation(
                        id = "lower",
                        titleResId = R.string.lower_body_routine_title,
                        descriptionResId = R.string.recommendation_no_data_subtitle,
                        iconResName = null
                    )
                }
            ),
            RecommendationRule(
                id = "breathing",
                matches = { c ->
                    val moodScoresForAvg = c.moods.mapNotNull { it.moodScore }
                    moodScoresForAvg.isNotEmpty() && moodScoresForAvg.average() <= 2.5
                },
                build = { _ ->
                    Recommendation(
                        id = "breathing",
                        titleResId = R.string.breath_title,
                        descriptionResId = R.string.recommendation_no_data_subtitle,
                        iconResName = null
                    )
                }
            )
        )

        for (r in rules) {
            if (r.matches(ctx)) return r.build(ctx)
        }

        return Recommendation(
            id = "general",
            titleResId = R.string.recommendation_title,
            descriptionResId = R.string.recommendation_no_data_subtitle,
            iconResName = null
        )
    }

    private data class RecommendationRule(
        val id: String,
        val matches: (RecommendationContext) -> Boolean,
        val build: (RecommendationContext) -> Recommendation
    )

    private data class RecommendationContext(
        val moods: List<MoodEntry>,
        val pains: List<PainPoint>
    )

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

}

