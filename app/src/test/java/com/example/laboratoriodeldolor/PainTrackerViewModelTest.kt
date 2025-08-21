package com.example.laboratoriodeldolor

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PainTrackerViewModelTest {

    @Test
    fun addAndClearPoints_updatesStateFlow() = runTest {
        val vm = PainTrackerViewModel(null)

        // start empty
        assertEquals(0, vm.painPoints.value.size)

        vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.5f, 0.5f))
        vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.1f, 0.2f))

        assertEquals(2, vm.painPoints.value.size)

        vm.clearPainPoints()
        assertEquals(0, vm.painPoints.value.size)
    }

    @Test
    fun savePainPoints_returnsFalseWhenNoDao() = runTest {
        val vm = PainTrackerViewModel(null)
        val result = vm.savePainPoints()
        assertFalse(result)
    }

    @Test
    fun savePainPoints_returnsTrueWhenDaoProvided() = runTest {
        // Provide a fake DAO that records calls but does not require Room
        val fakeDao = object : PainPointDao {
            var saved: List<PainPoint>? = null
            override fun getAll() = throw NotImplementedError()
            override suspend fun insertAll(points: List<PainPoint>) {
                saved = points
            }
            override suspend fun deleteAll() {
                // no-op
            }
        }
        val vm = PainTrackerViewModel(fakeDao)
        vm.addPainPointNormalized(androidx.compose.ui.geometry.Offset(0.3f, 0.4f))
        val result = vm.savePainPoints()
        assertTrue(result)
        // Note: insert happens on viewModelScope asynchronously; we only assert the return value here.
    }
}
