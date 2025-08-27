package com.example.laboratoriodeldolor

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDaoTests {
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun moodDao_insertAndRetrieve() = runBlocking {
        val mood = MoodEntry(emoji = "😊", note = "test", timestamp = System.currentTimeMillis(), moodScore = 5)
        db.moodDao().insert(mood)
        val recent = db.moodDao().getRecent(10)
        assertTrue(recent.any { it.emoji == "😊" && it.note == "test" })
    }

    @Test
    fun painDao_insertLogAndPoints_retrievesRelationship() = runBlocking {
        val logId = db.painLogDao().insertLog(PainLog())
        val pts = listOf(
            PainPoint(x = 0.1f, y = 0.2f, view = "front", logId = logId),
            PainPoint(x = 0.4f, y = 0.8f, view = "front", logId = logId)
        )
        db.painPointDao().insertAll(pts)
        val byLog = db.painPointDao().getByLogId(logId)
        assertEquals(2, byLog.size)
    }
}
