package com.example.laboratoriodeldolor

import android.app.Application
import com.example.laboratoriodeldolor.repository.*
import com.example.laboratoriodeldolor.repository.impl.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MoodApplication : Application() {
    // Using lazy so the database is only created when it's first needed
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    
    // Preferences repository for lightweight persisted user choices
    val preferencesRepository: com.example.laboratoriodeldolor.data.UserPreferencesRepository by lazy { 
        com.example.laboratoriodeldolor.data.UserPreferencesRepository(this) 
    }
    
    // Repository pattern implementations - provide clean abstractions over DAOs
    val moodRepository: MoodRepository by lazy { 
        MoodRepositoryImpl(database.moodDao()) 
    }
    
    val painPointRepository: PainPointRepository by lazy { 
        PainPointRepositoryImpl(database.painPointDao()) 
    }
    
    val painLogRepository: PainLogRepository by lazy { 
        PainLogRepositoryImpl(database.painLogDao()) 
    }
    
    val exerciseRepository: ExerciseRepository by lazy { 
        ExerciseRepositoryImpl(database.exerciseDao()) 
    }
    
    val techniqueRepository: TechniqueRepository by lazy { 
        TechniqueRepositoryImpl(database.techniqueDao()) 
    }
    
    val routineRepository: RoutineRepository by lazy { 
        RoutineRepositoryImpl(database.routineDao()) 
    }
    
    val routineStepRepository: RoutineStepRepository by lazy { 
        RoutineStepRepositoryImpl(database.routineStepDao()) 
    }

    // CompletableDeferred lets activities await DB warmup without blocking the main thread
    private val _databaseReady = CompletableDeferred<Unit>()
    val databaseReady get() = _databaseReady

    companion object {
        // A simple static reference so ViewModel factories can access the app DB
        @JvmStatic
        var instance: MoodApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Warm up the Room database on a background coroutine so any migrations or
        // initialization work doesn't run on the main thread and cause an ANR
        // when ViewModels request DAOs during initial composition.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Force DB creation on the IO dispatcher
                database

                // Use suspendable APIs for DataStore and DAO calls (safe on IO)
                try {
                    // Keep warmup lightweight; seeding handled by DatabaseSeeder on DB creation
                    val prefs = preferencesRepository
                    prefs.hasSeededContentFlow.map { it }.first()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            } catch (t: Throwable) {
                // Keep startup resilient
                t.printStackTrace()
            } finally {
                // Signal that DB warmup finished (success or failure)
                if (!_databaseReady.isCompleted) _databaseReady.complete(Unit)
                // Schedule daily data retention cleanup (reads prefs to determine cutoff)
                try {
                    DataRetentionWorker.scheduleDaily(this@MoodApplication)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    /**
     * Suspendable helper for activities to await DB warmup.
     * Activities should call this from a coroutine (e.g. lifecycleScope) and
     * it will not block the main thread while waiting.
     */
    suspend fun awaitDatabaseReady() {
        databaseReady.await()
    }
}