package com.example.laboratoriodeldolor

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        val fakeRepository = object : com.example.laboratoriodeldolor.repository.RoutineRepository {
            override fun getAllRoutines(): kotlinx.coroutines.flow.Flow<List<Routine>> = flowOf(sample)
            override suspend fun getRoutineById(id: Long) = sample.firstOrNull()
            override suspend fun insertRoutine(routine: Routine): Long = 1L
            override fun getRoutineWithSteps(id: Long): kotlinx.coroutines.flow.Flow<RoutineWithSteps?> = flowOf(null)
        }

    val vm = RoutinesListViewModel(fakeRepository, dispatcher = dispatcher, collectionScope = this, autoCollect = false)
        // ensure collection starts on the test scope explicitly
    vm.startCollecting(this)
        try {
            // Ensure the test dispatcher runs all scheduled work (collection) before asserting
            dispatcher.scheduler.advanceUntilIdle()
            val item = vm.routines.value
            assertEquals(1, item.size)
            assertEquals("R1", item[0].title)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    @Test
    fun routinesFlow_emitsEmptyOnDaoError() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(dispatcher)

        val fakeRepository = object : com.example.laboratoriodeldolor.repository.RoutineRepository {
            override fun getAllRoutines(): kotlinx.coroutines.flow.Flow<List<Routine>> = flow { throw RuntimeException("boom") }
            override suspend fun getRoutineById(id: Long): Routine? = null
            override suspend fun insertRoutine(routine: Routine): Long = 0L
            override fun getRoutineWithSteps(id: Long): kotlinx.coroutines.flow.Flow<RoutineWithSteps?> = flowOf(null)
        }

    val vm = RoutinesListViewModel(fakeRepository, dispatcher = dispatcher, collectionScope = this, autoCollect = false)
        // start collecting in the test scope, then advance scheduler
    vm.startCollecting(this)
        try {
            dispatcher.scheduler.advanceUntilIdle()
            val item = vm.routines.value
            // on error the ViewModel should provide an empty list
            assertEquals(0, item.size)
        } finally {
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }
}
