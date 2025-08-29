package com.example.laboratoriodeldolor.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class UserPreferencesRepositoryTest {

    @Test
    fun `emojiPackFlow and setEmojiPack work via injected DataStore`() = runBlocking {
        val tmpDir = createTempDir(prefix = "prefs-")
        val prefsFile = File(tmpDir, "user_prefs.preferences_pb")

        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create { prefsFile }

        val repo = UserPreferencesRepository(context = androidx.test.core.app.ApplicationProvider.getApplicationContext(), providedDataStore = dataStore)

        // initial value should be default 0
        val initial = repo.emojiPackFlow.first()
        assertEquals(0, initial)

        // write a new pack
        runTest {
            repo.setEmojiPack(1)
        }

        val after = repo.emojiPackFlow.first()
        assertEquals(1, after)
    }
}
