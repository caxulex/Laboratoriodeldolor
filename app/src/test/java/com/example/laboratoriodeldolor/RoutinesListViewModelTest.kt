package com.example.laboratoriodeldolor

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalCoroutinesApi::class)
class RoutinesListViewModelTest {

    @Test
    fun routinesFlow_emitsListFromDao() = runTest {
        val sample = listOf(Routine(id = 1, title = "R1", bodyRegion = "upper", summary = "s1"))
        val fakeDao = object : RoutineDao {
            override fun getAll() = flowOf(sample)
            override suspend fun getById(id: Long) = sample.firstOrNull()
            override suspend fun insert(routine: Routine) = 1L
            // Provide explicit type so flowOf(null) is not ambiguous
            override fun getWithSteps(id: Long) = flowOf<RoutineWithSteps?>(null)
        }

        // Use backgroundScope so collection job doesn't count as an active child of the test scope
        val vm = RoutinesListViewModel(fakeDao, externalScope = this.backgroundScope)

        // Subscribe to the flow so SharingStarted.WhileSubscribed will start upstream collection
        vm.routines.test {
            val first = awaitItem()
            val list = if (first.isEmpty()) awaitItem() else first
            assertEquals(1, list.size)
            assertEquals("R1", list[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun routinesFlow_emitsEmptyOnDaoError() = runTest {
        val fakeDao = object : RoutineDao {
            // Specify generic for the flow builder so the type matches Flow<List<Routine>>
            override fun getAll() = kotlinx.coroutines.flow.flow<List<Routine>> { throw RuntimeException("boom") }
            override suspend fun getById(id: Long): Routine? = null
            override suspend fun insert(routine: Routine) = 0L
            override fun getWithSteps(id: Long) = flowOf<RoutineWithSteps?>(null)
        }

        // Use backgroundScope so collection job doesn't count as an active child of the test scope
        val vm = RoutinesListViewModel(fakeDao, externalScope = this.backgroundScope)

        vm.routines.test {
            val list = awaitItem()
            assertEquals(0, list.size)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
