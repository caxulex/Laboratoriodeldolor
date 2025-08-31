package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class DiaryViewModel(private val moodDao: MoodDao) : ViewModel() {

    private val _entries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val entries: StateFlow<List<MoodEntry>> = _entries

    init {
        // Collecting Flow returned by Room is safe on main, but ensure any heavy processing is on IO.
        viewModelScope.launch {
            moodDao.getAllEntries().collectLatest { list ->
                _entries.value = list
            }
        }
    }

    fun saveEntry(emoji: String, note: String) {
        viewModelScope.launch {
            val entry = MoodEntry(emoji = emoji, note = note, timestamp = System.currentTimeMillis(), moodScore = MoodMapping.emojiToScore(emoji))
            withContext(Dispatchers.IO) {
                moodDao.insert(entry)
            }
        }
    }
}
