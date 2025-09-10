package com.example.laboratoriodeldolor

import android.app.Application
import android.content.SharedPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Minimal in-memory SharedPreferences for plain-JVM unit tests.
 */
class InMemoryPrefs : SharedPreferences {
    private val map = mutableMapOf<String, Any>()
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun getInt(key: String?, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getAll(): MutableMap<String, *> = map
    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        override fun clear(): SharedPreferences.Editor { map.clear(); return this }
        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putInt(key: String?, value: Int): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putLong(key: String?, value: Long): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor { if (key != null) map[key] = value; return this }
        override fun putString(key: String?, value: String?): SharedPreferences.Editor { if (key != null && value != null) map[key] = value; return this }
        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor { if (key != null && values != null) map[key] = values; return this }
        override fun remove(key: String?): SharedPreferences.Editor { if (key != null) map.remove(key); return this }
        override fun commit(): Boolean = true
        override fun apply() {}
    }
    // Unused in tests
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun getFloat(key: String?, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getString(key: String?, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = map[key] as? MutableSet<String> ?: defValues
}

class SettingsViewModelTest {

    @Test
    fun defaultRetentionIsThreeAndCanBeUpdated() = runTest {
        // Use an in-memory SharedPreferences so the test can run on the JVM without instrumentation
        val prefs = InMemoryPrefs()

        val vm = SettingsViewModel(application = Application(), injectedPrefs = prefs)

        // default should be 3 (months)
        val initial = vm.retentionMonths.value
        assertEquals(3, initial)

        // update to 6 and verify persisted
        vm.setRetentionMonths(6)
        val after = vm.retentionMonths.value
        assertEquals(6, after)

        // new instance should read persisted value from the provided prefs
        val vm2 = SettingsViewModel(application = Application(), injectedPrefs = prefs)
        assertEquals(6, vm2.retentionMonths.value)
    }
}
