package com.example.laboratoriodeldolor

import android.app.Application

class MoodApplication : Application() {
    // Using lazy so the database is only created when it's first needed
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    // Preferences repository for lightweight persisted user choices
    val preferencesRepository: com.example.laboratoriodeldolor.data.UserPreferencesRepository by lazy { com.example.laboratoriodeldolor.data.UserPreferencesRepository(this) }

    companion object {
        // A simple static reference so ViewModel factories can access the app DB
        @JvmStatic
        var instance: MoodApplication? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Warm up the Room database on a background thread so any migrations or
        // initialization work doesn't run on the main thread and cause an ANR
        // when ViewModels request DAOs during initial composition.
        Thread {
            try {
                // Accessing `database` will trigger the lazy initializer and run
                // any pending migrations off the main thread. Assign to a
                // non-reserved local variable so the compiler doesn't complain.
                val dbInstance = database
                // Optionally touch a DAO to ensure underlying DB file is opened
                @Suppress("UNUSED_VARIABLE")
                val _maybe = dbInstance // keep reference briefly to avoid optimization
            } catch (t: Throwable) {
                // Don't crash the app if pre-initialization fails; the DB will be
                // created lazily later when needed. Log to aid debugging.
                t.printStackTrace()
            }
        }.start()
    }
}