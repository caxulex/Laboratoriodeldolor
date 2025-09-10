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
            val vm = PainTrackerViewModel(null)

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
    fun savePainPoints_returnsFalseWhenNoDao() = runTest {
        val vm = PainTrackerViewModel(null)
    val result = vm.savePainPoints()
    // savePainPoints returns String?; when no DAO it's null
    assertNull(result)
    }

    @Test
    fun savePainPoints_returnsTrueWhenDaoProvided() = runTest {
        // Provide a fake DAO that records calls but does not require Room
        val fakeDao = object : PainPointDao {
            var saved: List<PainPoint>? = null
            override fun getAll() = kotlinx.coroutines.flow.flowOf(emptyList<PainPoint>())
            override suspend fun getAllSnapshot(): List<PainPoint> = emptyList()
            override suspend fun insertAll(points: List<PainPoint>) {
                saved = points
            }
            override suspend fun deleteAll() {
                // no-op
            }
            override suspend fun deleteById(id: Long) {
                // no-op for test
            }
            override suspend fun deleteOlderThan(cutoffMillis: Long) { /* no-op */ }
            override suspend fun getByLogId(logId: Long) = emptyList<PainPoint>()
        }
    val dispatcher = UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    try {
            val vm = PainTrackerViewModel(fakeDao)
            vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.3f, 0.4f))
            val result = vm.savePainPoints()
            // should return a non-null navigation route string when DAO provided
            assertNotNull(result)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
        // Note: insert happens on viewModelScope asynchronously; we only assert the return value here.
    }
}
