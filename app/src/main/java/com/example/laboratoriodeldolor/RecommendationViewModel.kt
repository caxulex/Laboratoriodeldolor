package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * RecommendationViewModel
 * - Inspects recent MoodEntry and PainPoint records to produce a single Recommendation.
 * - Logic is rule-based and easily extendable.
 *
 * Important: this ViewModel never touches Android Context or resolves strings.
 * It returns resource IDs inside Recommendation and the UI should call stringResource() when rendering.
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
            val (recentMoods, recentPains) = withContext(Dispatchers.IO) {
                val m = moodDao.getRecent(14)
                val p = painDao.getAllSnapshot()
                Pair(m, p)
            }

            val rec = withContext(Dispatchers.Default) { computeRecommendation(recentMoods, recentPains) }
            _recommendation.value = rec
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
                    val maxIntensity = c.pains
                        .filter { pp ->
                            val k = mapPainPointToLocationKey(pp)
                            k == PainLocationKey.FRONT_UPPER || k == PainLocationKey.BACK_UPPER
                        }
                        .maxOfOrNull { it.intensity } ?: 1

                    val descRes = R.string.recommendation_no_data_subtitle

                    Recommendation(
                        id = "upper_front_or_back",
                        titleResId = R.string.upper_body_routine_title,
                        descriptionResId = descRes,
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
                    val maxIntensity = c.pains
                        .filter { pp ->
                            val k = mapPainPointToLocationKey(pp)
                            k == PainLocationKey.FRONT_MIDDLE || k == PainLocationKey.BACK_MIDDLE
                        }
                        .maxOfOrNull { it.intensity } ?: 1

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
                    val maxIntensity = c.pains
                        .filter { pp ->
                            val k = mapPainPointToLocationKey(pp)
                            k == PainLocationKey.FRONT_LOWER || k == PainLocationKey.BACK_LOWER
                        }
                        .maxOfOrNull { it.intensity } ?: 1

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

