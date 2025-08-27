package com.example.laboratoriodeldolor

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.OptIn
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationViewModelTest {

    @Test
    fun longestConsecutiveStreakScores_detectsStreaks() = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
            kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    try {
            val vm = RecommendationViewModel(fakeMoodDao(), fakePainPointDao())
            val method = RecommendationViewModel::class.java.getDeclaredMethod("longestConsecutiveStreakScores", List::class.java, Int::class.java)
            method.isAccessible = true
            val scores = listOf(3,2,2,1,4,2)
            val res = method.invoke(vm, scores, 2) as Int
            // Last-to-first (reversed logic) yields a streak of 3 (2,2,1)
            assertEquals(3, res)
        } finally {
                kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    @Test
    fun computeRecommendation_prefersUpperRoutine_whenUpperPainPresent() = runTest {
        val moodDao = object : MoodDao {
            override suspend fun insert(moodEntry: MoodEntry) {}
            override fun getAllEntries() = kotlinx.coroutines.flow.flowOf(emptyList<MoodEntry>())
            override suspend fun getRecent(limit: Int) = emptyList<MoodEntry>()
        }
        val painDao = object : PainPointDao {
            override fun getAll() = kotlinx.coroutines.flow.flowOf(emptyList<PainPoint>())
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
            // computeRecommendation is a suspend private method; call refresh() then check recommendation
            vm.refresh()
            // wait briefly for coroutine
            runBlocking { kotlinx.coroutines.delay(200) }
            val rec = vm.recommendation.value
            assertNotNull(rec)
            assertEquals("upper_front_or_back", rec?.id)
        } finally {
                kotlinx.coroutines.Dispatchers.resetMain()
        }
    }
}

fun fakePainPointDao(): PainPointDao = object : PainPointDao {
    override fun getAll() = kotlinx.coroutines.flow.flowOf(emptyList<PainPoint>())
    override suspend fun getAllSnapshot() = emptyList<PainPoint>()
    override suspend fun insertAll(points: List<PainPoint>) {}
    override suspend fun deleteAll() {}
    override suspend fun deleteById(id: Long) {}
    override suspend fun getByLogId(logId: Long) = emptyList<PainPoint>()
}

fun fakeMoodDao(): MoodDao = object : MoodDao {
    override suspend fun insert(moodEntry: MoodEntry) {}
    override fun getAllEntries() = kotlinx.coroutines.flow.flowOf(emptyList<MoodEntry>())
    override suspend fun getRecent(limit: Int) = emptyList<MoodEntry>()
}
