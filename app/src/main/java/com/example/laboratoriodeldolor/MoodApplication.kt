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
    }
}