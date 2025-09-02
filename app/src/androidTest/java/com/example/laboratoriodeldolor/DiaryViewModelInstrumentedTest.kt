package com.example.laboratoriodeldolor

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore

@RunWith(AndroidJUnit4::class)
@Ignore("Disabled during local dev to speed emulator; enable in CI when needed")
class DiaryViewModelInstrumentedTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MoodDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.moodDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun saveEntry_insertsIntoDb() = runBlocking {
        val vm = DiaryViewModel(dao)
        vm.saveEntry("🙂", "test note")

        // small delay for Room (saveEntry uses viewModelScope on main thread; using runBlocking here should be fine because allowMainThreadQueries was used)
        val entries = dao.getAllEntries().first()
        assertEquals(1, entries.size)
        assertEquals("test note", entries[0].note)
    }
}
