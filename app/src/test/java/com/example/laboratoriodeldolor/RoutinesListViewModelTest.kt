package com.example.laboratoriodeldolor

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.OptIn
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow

@OptIn(ExperimentalCoroutinesApi::class)
class RoutinesListViewModelTest {

    @Test
    fun routinesFlow_emitsListFromDao() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)

        val sample = listOf(Routine(id = 1, title = "R1", bodyRegion = "upper", summary = "s1"))
        val fakeDao = object : RoutineDao {
            override fun getAll(): kotlinx.coroutines.flow.Flow<List<Routine>> = flowOf(sample)
            override suspend fun getById(id: Long) = sample.firstOrNull()
            override suspend fun insert(routine: Routine): Long { return 1L }
            override fun getWithSteps(id: Long): kotlinx.coroutines.flow.Flow<RoutineWithSteps?> = flowOf(null)
        }

        val vm = RoutinesListViewModel(fakeDao, dispatcher = dispatcher)

        // Move dispatcher so stateIn collects
        dispatcher.scheduler.advanceUntilIdle()

    // advance until the stateIn collection runs and updates the StateFlow
    dispatcher.scheduler.advanceUntilIdle()
    val item = vm.routines.value
    assertEquals(1, item.size)
    assertEquals("R1", item[0].title)
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun routinesFlow_emitsEmptyOnDaoError() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)

        val fakeDao = object : RoutineDao {
            override fun getAll(): kotlinx.coroutines.flow.Flow<List<Routine>> = flow { throw RuntimeException("boom") }
            override suspend fun getById(id: Long): Routine? = null
            override suspend fun insert(routine: Routine): Long = 0L
            override fun getWithSteps(id: Long): kotlinx.coroutines.flow.Flow<RoutineWithSteps?> = flowOf(null)
        }

        val vm = RoutinesListViewModel(fakeDao, dispatcher = dispatcher)
        dispatcher.scheduler.advanceUntilIdle()

    dispatcher.scheduler.advanceUntilIdle()
    val item = vm.routines.value
    // on error the ViewModel should provide an empty list
    assertEquals(0, item.size)
        kotlinx.coroutines.Dispatchers.resetMain()
    }
}
