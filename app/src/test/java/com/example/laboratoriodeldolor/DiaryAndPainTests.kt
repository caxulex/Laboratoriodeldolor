package com.example.laboratoriodeldolor

import androidx.compose.ui.geometry.Offset
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
class DiaryAndPainTests {

    @Test
    fun diary_saveEntry_callsDaoInsert() = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            var inserted: MoodEntry? = null
        val spyDao = object : MoodDao {
            override suspend fun insert(moodEntry: MoodEntry) { inserted = moodEntry }
            override fun getAllEntries() = kotlinx.coroutines.flow.flowOf(emptyList<MoodEntry>())
            override suspend fun getRecent(limit: Int) = emptyList<MoodEntry>()
        }

        val vm = DiaryViewModel(spyDao)
        vm.saveEntry("😊", "nota de prueba")

        // saveEntry launches a coroutine; wait briefly by running a blocking loop
        runBlocking { kotlinx.coroutines.delay(200) }

            assertNotNull(inserted)
            assertEquals("😊", inserted?.emoji)
            assertEquals("nota de prueba", inserted?.note)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    @Test
    fun detectBodyAreas_detectsUpperMiddleLower() {
        val pts = listOf(Offset(0.5f, 0.1f), Offset(0.5f, 0.4f), Offset(0.5f, 0.9f))
        val areas = detectBodyAreas(pts)
        assertTrue(areas.contains(BodyArea.UPPER))
        assertTrue(areas.contains(BodyArea.MIDDLE))
        assertTrue(areas.contains(BodyArea.LOWER))
    }

    @Test
    fun mapPainPointToLocationKey_frontUpper_and_backLower() {
        val frontUpper = PainPoint(x = 0.5f, y = 0.1f, view = "front")
        val backLower = PainPoint(x = 0.5f, y = 0.9f, view = "back")
        assertEquals(PainLocationKey.FRONT_UPPER, mapPainPointToLocationKey(frontUpper))
        assertEquals(PainLocationKey.BACK_LOWER, mapPainPointToLocationKey(backLower))
    }
}
