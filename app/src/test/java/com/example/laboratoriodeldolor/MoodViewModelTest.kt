package com.example.laboratoriodeldolor

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MoodViewModelTest {
	@Test
	fun fakeDao_defaultBehaves() = runTest {
	val dao = fakeMoodDaoImpl()
		// basic smoke: getRecent should be empty
		val recent = dao.getRecent(5)
		assertEquals(0, recent.size)
	}
}
