package com.example.laboratoriodeldolor

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Top-level enum describing body areas used by detection and UI.
 * Kept at top-level to make the detection logic easily testable from unit tests.
 */
enum class BodyArea { UPPER, MIDDLE, LOWER }

/**
 * Pure function that maps a list of normalized points (y in 0..1) to a set of BodyArea.
 * - Top third: UPPER (y in [0, 1/3))
 * - Middle third: MIDDLE (y in [1/3, 2/3))
 * - Bottom third: LOWER (y in [2/3, 1])
 */
fun detectBodyAreas(points: List<Offset>): Set<BodyArea> {
    val found = mutableSetOf<BodyArea>()
    for (p in points) {
        val y = p.y.coerceIn(0f, 1f)
        when {
            y < (1f / 3f) -> found.add(BodyArea.UPPER)
            y < (2f / 3f) -> found.add(BodyArea.MIDDLE)
            else -> found.add(BodyArea.LOWER)
        }
    }
    return found
}
/**
 * ViewModel that holds the list of pain points on the body image.
 *
 * All coordinates are stored as normalized offsets (0f..1f) relative to the image/canvas
 * so they remain correctly positioned when the image scales.
 */

class PainTrackerViewModel(
    private val painPointDao: PainPointDao? = null,
    private val painLogDao: PainLogDao? = null
) : ViewModel() {
    // Stored coordinates are normalized (x = 0..1, y = 0..1)
    // Separate lists for front and back views so users can mark independently.
    private val _frontPainPoints = MutableStateFlow<List<LocalPainPoint>>(emptyList())
    val frontPainPoints: StateFlow<List<LocalPainPoint>> = _frontPainPoints

    private val _backPainPoints = MutableStateFlow<List<LocalPainPoint>>(emptyList())
    val backPainPoints: StateFlow<List<LocalPainPoint>> = _backPainPoints

    // Expose stored DB points as Flow if DAO provided (optional)
    val painPointsFromDb: Flow<List<PainPoint>>? = painPointDao?.getAll()

    // Expose which areas currently have pain points (set of BodyArea)
    private val _selectedAreas = MutableStateFlow<Set<BodyArea>>(emptySet())
    val selectedAreas: StateFlow<Set<BodyArea>> = _selectedAreas

    // New UI state: selected gender and view
    private val _selectedGender = MutableStateFlow<String>("male") // "male" or "female"
    val selectedGender: StateFlow<String> = _selectedGender

    private val _selectedView = MutableStateFlow<String>("front") // "front" or "back"
    val selectedView: StateFlow<String> = _selectedView

    // Recompute selected areas based on normalized painPoints lists (combine front+back)
    private fun computeSelectedAreasFromPoints() {
        val combinedOffsets = (_frontPainPoints.value.map { Offset(it.xNorm, it.yNorm) } + _backPainPoints.value.map { Offset(it.xNorm, it.yNorm) })
        _selectedAreas.value = detectBodyAreas(combinedOffsets)
    }

    /**
     * Delete all persisted pain points.
     */
    fun deleteAllPersisted() {
        val dao = painPointDao ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.deleteAll() }
            } catch (e: Exception) { println("deleteAllPersisted failed: ${'$'}e") }
        }
    }

    /**
     * Delete a persisted pain point by its database id.
     */
    fun deletePersistedById(id: Long) {
        val dao = painPointDao ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { dao.deleteById(id) }
            } catch (e: Exception) { println("deletePersistedById failed: ${'$'}e") }
        }
    }

    /**
     * Add a pain point coordinate expressed as normalized fractions of the image size.
     * @param normalizedPoint Offset where x and y are in range 0f..1f
     */
    /**
     * Add a pain point coordinate (normalized) with an intensity value (1..3)
     */
    fun addPainPointNormalized(normalizedPoint: Offset, intensity: Int = 1) {
        // Clamp to [0,1] to be safe
        val nx = normalizedPoint.x.coerceIn(0f, 1f)
        val ny = normalizedPoint.y.coerceIn(0f, 1f)
        val lp = LocalPainPoint(nx, ny, intensity.coerceIn(1, 3))
        when (_selectedView.value) {
            "front" -> _frontPainPoints.value = _frontPainPoints.value + lp
            "back" -> _backPainPoints.value = _backPainPoints.value + lp
            else -> _frontPainPoints.value = _frontPainPoints.value + lp
        }
        // Update detected areas across both lists
        computeSelectedAreasFromPoints()
    }

    /**
     * Clear all recorded pain points.
     */
    fun clearPainPoints() {
        // Clear only the currently selected view's in-memory points
        when (_selectedView.value) {
            "front" -> _frontPainPoints.value = emptyList()
            "back" -> _backPainPoints.value = emptyList()
            else -> { _frontPainPoints.value = emptyList(); _backPainPoints.value = emptyList() }
        }
        computeSelectedAreasFromPoints()
    }

    /**
     * Pretend to save pain points - placeholder for persistence.
     * In a real app this should persist to a database or remote endpoint.
     */
    /**
     * Persist current in-memory pain points and return the most relevant route
     * representing the dominant pain region, or null if none.
     * This is a suspend function so callers can await completion before navigating.
     */
    suspend fun savePainPoints(): String? {
        val dao = painPointDao ?: return null

        // Save points from both front and back lists with correct view tags
        val frontToSave = _frontPainPoints.value.map { lp -> PainPoint(x = lp.xNorm, y = lp.yNorm, view = "front", intensity = lp.intensity) }
        val backToSave = _backPainPoints.value.map { lp -> PainPoint(x = lp.xNorm, y = lp.yNorm, view = "back", intensity = lp.intensity) }
        val toSave = frontToSave + backToSave

        return try {
            // Run DB operations on IO
            val toSaveWithLog = withContext(Dispatchers.IO) {
                val logId = try { painLogDao?.insertLog(PainLog()) ?: 0L } catch (_: Exception) { 0L }
                toSave.map { it.copy(logId = logId) }
            }

            withContext(Dispatchers.IO) {
                dao.insertAll(toSaveWithLog)
            }

            // After persisting, recompute areas from the in-memory lists
            computeSelectedAreasFromPoints()

            // Use the enhanced navigation logic for smarter recommendations
            return analyzePainPointsForNavigation(toSaveWithLog)
        } catch (e: Exception) {
            println("PainTrackerViewModel: savePainPoints failed: ${'$'}e")
            null
        }
    }

    /**
     * Persist the provided list of LocalPainPoint without mutating the ViewModel's in-memory lists.
     * Returns a target navigation route (or null) similar to savePainPoints().
     */
    suspend fun savePoints(points: List<LocalPainPoint>): String? {
        val dao = painPointDao ?: return null
        val frontToSave = points.filter { it.view == "front" }.map { lp -> PainPoint(x = lp.xNorm, y = lp.yNorm, view = "front", intensity = lp.intensity) }
        val backToSave = points.filter { it.view == "back" }.map { lp -> PainPoint(x = lp.xNorm, y = lp.yNorm, view = "back", intensity = lp.intensity) }
        val toSave = frontToSave + backToSave

        return try {
            val toSaveWithLog = withContext(Dispatchers.IO) {
                val logId = try { painLogDao?.insertLog(PainLog()) ?: 0L } catch (_: Exception) { 0L }
                toSave.map { it.copy(logId = logId) }
            }

            withContext(Dispatchers.IO) {
                dao.insertAll(toSaveWithLog)
            }

            // Don't mutate the in-memory lists; keep computeSelectedAreasFromPoints working from existing lists.

            return analyzePainPointsForNavigation(toSaveWithLog)
        } catch (e: Exception) {
            println("PainTrackerViewModel: savePoints failed: ${'$'}e")
            null
        }
    }

    // UI actions for gender/view selection
    fun selectGender(g: String) { _selectedGender.value = g }
    fun selectView(v: String) { _selectedView.value = v }
}
