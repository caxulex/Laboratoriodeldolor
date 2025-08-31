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

    @Test
    fun prefersUpperRoutine_whenUpperPainPresent() = runTest {
        val moodDao = object : MoodDao {
            override suspend fun insert(moodEntry: MoodEntry) {}
            override fun getAllEntries() = kotlinx.coroutines.flow.flowOf(emptyList<MoodEntry>())
            override suspend fun getRecent(limit: Int) = emptyList<MoodEntry>()
        }

        val painDao = object : PainPointDao {
            override fun getAll() = kotlinx.coroutines.flow.flowOf(listOf(PainPoint(x=0.5f,y=0.1f,view="front")))
            override suspend fun getAllSnapshot() = listOf(PainPoint(x=0.5f,y=0.1f,view="front"))
            override suspend fun insertAll(points: List<PainPoint>) {}
            override suspend fun deleteAll() {}
            override suspend fun deleteById(id: Long) {}
            override suspend fun getByLogId(logId: Long) = emptyList<PainPoint>()
        }

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            val vm = RecommendationViewModel(moodDao, painDao)
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
        }

        val painDao = fakePainPointDao()

        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            val vm = RecommendationViewModel(moodDao, painDao)
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
