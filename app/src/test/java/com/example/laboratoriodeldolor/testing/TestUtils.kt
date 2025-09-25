package com.example.laboratoriodeldolor.testing

import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import app.cash.turbine.test
import org.junit.Assert.*

/**
 * Utility class for common testing patterns and assertions
 */
@OptIn(ExperimentalCoroutinesApi::class)
object TestUtils {

    /**
     * Creates a test scope with UnconfinedTestDispatcher for immediate execution
     */
    fun createTestScope() = TestScope(UnconfinedTestDispatcher())

    /**
     * Runs a test with proper coroutine context
     */
    fun runBlockingTest(testBody: suspend TestScope.() -> Unit) = runTest {
        testBody()
    }

    /**
     * Tests a Flow emission with Turbine
     */
    suspend fun <T> Flow<T>.testEmissions(
        expectedCount: Int? = null,
        assertion: suspend (List<T>) -> Unit
    ) {
        test {
            val emissions = mutableListOf<T>()
            
            // Collect expected number of emissions or until completion
            if (expectedCount != null) {
                repeat(expectedCount) {
                    emissions.add(awaitItem())
                }
            } else {
                // Collect all emissions until completion
                while (true) {
                    try {
                        emissions.add(awaitItem())
                    } catch (e: Exception) {
                        break
                    }
                }
            }
            
            assertion(emissions)
            
            // Clean up
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Asserts that a Flow emits specific values in order
     */
    suspend fun <T> Flow<T>.assertEmitsInOrder(vararg expectedValues: T) {
        test {
            expectedValues.forEach { expected ->
                assertEquals(expected, awaitItem())
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Asserts that a Flow emits a single value
     */
    suspend fun <T> Flow<T>.assertEmitsSingle(expectedValue: T) {
        test {
            assertEquals(expectedValue, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Asserts that a Flow emits no values (is empty)
     */
    suspend fun <T> Flow<T>.assertEmpty() {
        test {
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * Collects all emissions from a Flow into a list
     */
    suspend fun <T> Flow<T>.collectAll(): List<T> {
        val emissions = mutableListOf<T>()
        test {
            while (true) {
                try {
                    emissions.add(awaitItem())
                } catch (e: Exception) {
                    break
                }
            }
            cancelAndIgnoreRemainingEvents()
        }
        return emissions
    }
}

/**
 * Custom assertions for domain-specific testing
 */
object DomainAssertions {

    /**
     * Asserts that a mood score is within valid range
     */
    fun assertValidMoodScore(score: Int, message: String = "Mood score should be between 1-10") {
        assertTrue(message, score in 1..10)
    }

    /**
     * Asserts that a pain level is within valid range
     */
    fun assertValidPainLevel(level: Int, message: String = "Pain level should be between 1-10") {
        assertTrue(message, level in 1..10)
    }

    /**
     * Asserts that exercise duration is reasonable
     */
    fun assertValidExerciseDuration(duration: Int, message: String = "Exercise duration should be positive") {
        assertTrue(message, duration > 0)
    }

    /**
     * Asserts that a date range is valid (start <= end)
     */
    fun assertValidDateRange(
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate,
        message: String = "Start date should be before or equal to end date"
    ) {
        assertTrue(message, !startDate.isAfter(endDate))
    }

    /**
     * Asserts that a list is not empty and contains valid items
     */
    fun <T> assertNonEmptyList(
        list: List<T>,
        message: String = "List should not be empty"
    ) {
        assertFalse(message, list.isEmpty())
        assertTrue("List should contain items", list.isNotEmpty())
    }

    /**
     * Asserts that a list is sorted by a specific property
     */
    fun <T, R : Comparable<R>> assertSortedBy(
        list: List<T>,
        selector: (T) -> R,
        ascending: Boolean = true,
        message: String = "List should be sorted"
    ) {
        if (list.size <= 1) return
        
        val sorted = if (ascending) {
            list.sortedBy(selector)
        } else {
            list.sortedByDescending(selector)
        }
        
        assertEquals(message, sorted, list)
    }
}

/**
 * Mock verification utilities
 */
object MockVerification {

    /**
     * Verifies that a suspend function was called with specific parameters
     */
    inline fun <reified T> verifySuspendCall(
        mock: T,
        crossinline verification: suspend T.() -> Unit
    ) {
        io.mockk.coVerify { mock.verification() }
    }

    /**
     * Verifies that a function was never called
     */
    inline fun <reified T> verifyNeverCalled(
        mock: T,
        crossinline verification: T.() -> Unit
    ) {
        io.mockk.verify(exactly = 0) { mock.verification() }
    }

    /**
     * Verifies that a function was called exactly N times
     */
    inline fun <reified T> verifyCalledTimes(
        mock: T,
        times: Int,
        crossinline verification: T.() -> Unit
    ) {
        io.mockk.verify(exactly = times) { mock.verification() }
    }
}

/**
 * Test rule utilities
 */
class TestCoroutineRule {
    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val testScope = TestScope(testDispatcher)
    
    fun runTest(block: suspend TestScope.() -> Unit) = testScope.runTest {
        block()
    }
}