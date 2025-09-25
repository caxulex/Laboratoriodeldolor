package com.example.laboratoriodeldolor

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.OptIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PainTrackerViewModelTest {

    @Test
    fun addAndClearPoints_updatesStateFlow() = runTest {
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(dispatcher)
        try {
            val vm = PainTrackerViewModel(null, null)

            // start empty (front view is default)
            assertEquals(0, vm.frontPainPoints.value.size)

            vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.5f, 0.5f))
            vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.1f, 0.2f))

            assertEquals(2, vm.frontPainPoints.value.size)

            vm.clearPainPoints()
            assertEquals(0, vm.frontPainPoints.value.size)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    @Test
    fun savePainPoints_returnsFalseWhenNoRepository() = runTest {
        val vm = PainTrackerViewModel(null, null)
    val result = vm.savePainPoints()
    // savePainPoints returns String?; when no repository it's null
    assertNull(result)
    }

    @Test
    fun savePainPoints_returnsTrueWhenRepositoryProvided() = runTest {
        // Provide a fake repository that records calls but does not require Room
        val fakeRepository = object : com.example.laboratoriodeldolor.repository.PainPointRepository {
            var saved: List<PainPoint>? = null
            override fun getAllPainPoints() = kotlinx.coroutines.flow.flowOf(emptyList<PainPoint>())
            override suspend fun getAllPainPointsSnapshot(): List<PainPoint> = emptyList()
            override suspend fun insertPainPoints(painPoints: List<PainPoint>) {
                saved = painPoints
            }
            override suspend fun clearAllPainPoints() {
                // no-op
            }
            override suspend fun deletePainPoint(id: Long) {
                // no-op for test
            }
            override suspend fun deleteOldPainPoints(cutoffMillis: Long) { /* no-op */ }
            override suspend fun getPainPointsByLogId(logId: Long) = emptyList<PainPoint>()
        }
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    try {
            val vm = PainTrackerViewModel(fakeRepository, null)
            vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.3f, 0.4f))
            val result = vm.savePainPoints()
            // should return a non-null navigation route string when repository provided
            assertNotNull(result)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
        // Note: insert happens on viewModelScope asynchronously; we only assert the return value here.
    }
}
