package com.example.laboratoriodeldolor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

// Simple in-memory fake DAOs for unit testing
class FakeExerciseDao : ExerciseDao {
    private val _logs = MutableStateFlow<List<ExerciseLog>>(emptyList())
    override fun getAll(): Flow<List<ExerciseLog>> = _logs
    override suspend fun insert(log: ExerciseLog) {
        _logs.value = listOf(log) + _logs.value
    }
    override suspend fun deleteMostRecent() {
        _logs.value = _logs.value.drop(1)
    }
}

class FakeMoodDao : MoodDao {
    override fun getAllEntries() = MutableStateFlow<List<MoodEntry>>(emptyList()) as Flow<List<MoodEntry>>
    override suspend fun insert(entry: MoodEntry) { /* no-op */ }
    override suspend fun getRecent(limit: Int): List<MoodEntry> = emptyList()
}

@OptIn(ExperimentalCoroutinesApi::class)
class MoodViewModelTest {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `logExerciseCompleted increases streak and sets loggedToday`() = runTest {
        val fakeExerciseDao = FakeExerciseDao()
        val fakeMoodDao = FakeMoodDao()
        val vm = MoodViewModel(fakeMoodDao, fakeExerciseDao, null)

        // Initially zero
        assertEquals(0, vm.streak.first())
        assertEquals(false, vm.loggedToday.first())

        // Call logExerciseCompleted and allow coroutines to run
        vm.logExerciseCompleted()

    // advance scheduler to process launched coroutines
    runCurrent()

        // Now the fake dao should have a log and ViewModel should reflect it
        val streak = vm.streak.first()
        val logged = vm.loggedToday.first()

        // Expect at least 1 (today) and loggedToday true
        assertEquals(1, streak)
        assertEquals(true, logged)
    }
}
