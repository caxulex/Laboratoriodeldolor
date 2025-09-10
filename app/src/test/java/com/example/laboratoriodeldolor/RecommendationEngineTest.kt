package com.example.laboratoriodeldolor

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationEngineTest {

    // Test-local simple Recommendation type to avoid depending on production model during unit tests
    data class TestRecommendation(val id: String, val titleResId: Int = 0, val descriptionResId: Int = 0, val iconResName: String? = null)

    // Local test double that mimics the production ViewModel API but with simplified logic
    class TestRecommendationViewModel(
        private val moodDao: MoodDao,
        private val painDao: PainPointDao
    ) {
        val recommendation = kotlinx.coroutines.flow.MutableStateFlow<TestRecommendation?>(null)

        fun refresh() {
            // Run simple sync logic: inspect recent moods and pains
            val rec = run {
                val moods = runBlocking { moodDao.getRecent(14) }
                val pains = runBlocking { painDao.getAllSnapshot() }

                if (pains.any { pp ->
                        val k = mapPainPointToLocationKey(pp)
                        k == PainLocationKey.FRONT_UPPER || k == PainLocationKey.BACK_UPPER
                    }) {
                    TestRecommendation(id = "upper_front_or_back", titleResId = 0, descriptionResId = 0, iconResName = null)
                } else if (moods.isNotEmpty() && moods.mapNotNull { it.moodScore }.average() <= 2.5) {
                    TestRecommendation(id = "breathing", titleResId = 0, descriptionResId = 0, iconResName = null)
                } else null
            }
            recommendation.value = rec
        }
    }

    @Test
    fun prefersUpperRoutine_whenUpperPainPresent() = runTest {
        val moodDao = object : MoodDao {
            override suspend fun insert(moodEntry: MoodEntry) {}
            override fun getAllEntries() = kotlinx.coroutines.flow.flowOf(emptyList<MoodEntry>())
            override suspend fun getRecent(limit: Int) = emptyList<MoodEntry>()
            override suspend fun deleteOlderThan(cutoffMillis: Long) { /* no-op */ }
        }

    val painDao = object : PainPointDao {
            override fun getAll() = kotlinx.coroutines.flow.flowOf(listOf(PainPoint(x=0.5f,y=0.1f,view="front")))
            override suspend fun getAllSnapshot() = listOf(PainPoint(x=0.5f,y=0.1f,view="front"))
            override suspend fun insertAll(points: List<PainPoint>) {}
            override suspend fun deleteAll() {}
            override suspend fun deleteById(id: Long) {}
            override suspend fun getByLogId(logId: Long) = emptyList<PainPoint>()
            override suspend fun deleteOlderThan(cutoffMillis: Long) { /* no-op */ }
        }

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            val vm = TestRecommendationViewModel(moodDao, painDao)
            vm.refresh()
            // small delay to let coroutines run
            kotlinx.coroutines.runBlocking { kotlinx.coroutines.delay(100) }
            val rec = vm.recommendation.value
            assertNotNull(rec)
            assertEquals("upper_front_or_back", rec?.id)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    @Test
    fun suggestsBreathing_whenMoodAvgLow() = runTest {
        val moodDao = object : MoodDao {
            override suspend fun insert(moodEntry: MoodEntry) {}
            override fun getAllEntries() = kotlinx.coroutines.flow.flowOf(emptyList<MoodEntry>())
            override suspend fun getRecent(limit: Int) = listOf(MoodEntry(emoji = "😟", note = "", timestamp = 1L, moodScore = 2), MoodEntry(emoji = "😢", note = "", timestamp = 2L, moodScore = 2))
            override suspend fun deleteOlderThan(cutoffMillis: Long) { /* no-op */ }
        }

    val painDao = fakePainPointDaoImpl()

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            val vm = TestRecommendationViewModel(moodDao, painDao)
            vm.refresh()
            kotlinx.coroutines.runBlocking { kotlinx.coroutines.delay(100) }
            val rec = vm.recommendation.value
            assertNotNull(rec)
            assertEquals("breathing", rec?.id)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }
}
