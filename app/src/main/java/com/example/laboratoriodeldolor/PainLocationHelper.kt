package com.example.laboratoriodeldolor

/**
 * Helper utilities to map a stored PainPoint to a descriptive location key.
 * Text shown to users is kept in string resources; this helper returns a key
 * that UI code can convert to a string resource id.
 */
enum class PainLocationKey {
    FRONT_UPPER,
    FRONT_MIDDLE,
    FRONT_LOWER,
    BACK_UPPER,
    BACK_MIDDLE,
    BACK_LOWER,
    UNKNOWN
}

/** Map a PainPoint to a PainLocationKey according to simple band thresholds. */
fun mapPainPointToLocationKey(pp: PainPoint): PainLocationKey {
    val y = pp.y.coerceIn(0f, 1f)
    
    // More refined mapping based on vertical position only
    return when (pp.view) {
        "front" -> {
            when {
                // Head and neck area
                y < 0.2f -> PainLocationKey.FRONT_UPPER
                // Chest, shoulders, arms
                y < 0.5f -> PainLocationKey.FRONT_UPPER
                // Abdomen, middle torso
                y < 0.7f -> PainLocationKey.FRONT_MIDDLE
                // Hips, legs, feet
                else -> PainLocationKey.FRONT_LOWER
            }
        }
        "back" -> {
            when {
                // Head, neck, upper back
                y < 0.2f -> PainLocationKey.BACK_UPPER
                // Shoulders, middle back
                y < 0.5f -> PainLocationKey.BACK_UPPER
                // Lower back, middle spine
                y < 0.7f -> PainLocationKey.BACK_MIDDLE
                // Lower back, glutes, legs
                else -> PainLocationKey.BACK_LOWER
            }
        }
        else -> PainLocationKey.UNKNOWN
    }
}

/** Map a PainLocationKey to a string resource id. UI code should call stringResource(resId).
 * Keep all user-facing text in `strings.xml`.
 */
fun painLocationKeyToStringRes(key: PainLocationKey): Int {
    return when (key) {
        PainLocationKey.FRONT_UPPER -> R.string.dolor_frontal_superior
        PainLocationKey.FRONT_MIDDLE -> R.string.dolor_frontal_medio
        PainLocationKey.FRONT_LOWER -> R.string.dolor_frontal_inferior
        PainLocationKey.BACK_UPPER -> R.string.dolor_espalda_superior
        PainLocationKey.BACK_MIDDLE -> R.string.dolor_espalda_medio
        PainLocationKey.BACK_LOWER -> R.string.dolor_espalda_inferior
        PainLocationKey.UNKNOWN -> R.string.dolor_ubicacion_desconocida
    }
}

/**
 * Map a PainLocationKey to the corresponding navigation route for technique screens.
 * This enables direct navigation from pain tracking to relevant exercises.
 */
fun painLocationKeyToRoute(key: PainLocationKey): String {
    return when (key) {
        PainLocationKey.FRONT_UPPER -> "front_upper_body"
        PainLocationKey.BACK_UPPER -> "back_upper_body"
        PainLocationKey.FRONT_MIDDLE -> "front_middle_body"
        PainLocationKey.BACK_MIDDLE -> "back_middle_body"
        PainLocationKey.FRONT_LOWER -> "front_lower_body"
        PainLocationKey.BACK_LOWER -> "back_lower_body"
        PainLocationKey.UNKNOWN -> "exercises" // fallback to exercise hub
    }
}

/**
 * Analyze a collection of pain points and determine the most relevant navigation target.
 * Returns the route for the most prevalent pain area, or a general route if multiple areas are affected.
 */
fun analyzePainPointsForNavigation(painPoints: List<PainPoint>): String {
    if (painPoints.isEmpty()) {
        return "exercises" // fallback to general exercises
    }
    
    // Group pain points by location and count occurrences
    val locationCounts = painPoints
        .groupBy { mapPainPointToLocationKey(it) }
        .mapValues { (_, points) -> 
            // Weight by intensity: higher intensity points count more
            points.sumOf { it.intensity }
        }
        .filterKeys { it != PainLocationKey.UNKNOWN }
    
    if (locationCounts.isEmpty()) {
        return "exercises"
    }
    
    // Find the location with the highest weighted score
    val dominantLocation = locationCounts.maxByOrNull { it.value }?.key
        ?: return "exercises"
    
    return painLocationKeyToRoute(dominantLocation)
}

/**
 * Get a user-friendly description of the dominant pain area for the given points.
 * Returns a string resource ID that can be used with stringResource().
 */
fun getDominantPainAreaDescription(painPoints: List<PainPoint>): Int {
    if (painPoints.isEmpty()) {
        return R.string.no_pain_areas
    }
    
    val locationCounts = painPoints
        .groupBy { mapPainPointToLocationKey(it) }
        .mapValues { (_, points) -> points.sumOf { it.intensity } }
        .filterKeys { it != PainLocationKey.UNKNOWN }
    
    val dominantLocation = locationCounts.maxByOrNull { it.value }?.key
        ?: return R.string.dolor_ubicacion_desconocida
    
    return painLocationKeyToStringRes(dominantLocation)
}

/**
 * Map a PainLocationKey to one or more Techniques Library region keys (Routine.bodyRegion).
 * These keys correspond to the routines seeded in the database and to strings:
 *  - face_head -> R.string.region_face_head
 *  - neck_shoulders -> R.string.region_neck_shoulders
 *  - upper_back -> R.string.region_upper_back
 *  - abdomen_pelvis -> R.string.region_mid_back_stomach
 *  - lower_back -> R.string.region_lower_back
 *  - arms_hands -> R.string.region_fingers_wrist_forearm
 *  - legs_feet -> R.string.region_ankles_feet_toes
 */
fun painLocationKeyToRegionKeys(key: PainLocationKey): List<String> {
    return when (key) {
        // Upper body pains relate to head/neck/upper back work
        PainLocationKey.FRONT_UPPER -> listOf("face_head", "neck_shoulders", "upper_back")
        PainLocationKey.BACK_UPPER -> listOf("upper_back", "neck_shoulders")
        // Middle area ties to abdomen/pelvis and sometimes upper back support
        PainLocationKey.FRONT_MIDDLE -> listOf("abdomen_pelvis", "upper_back")
        PainLocationKey.BACK_MIDDLE -> listOf("upper_back", "abdomen_pelvis")
        // Lower area maps to lumbar/pelvis and legs/feet
        PainLocationKey.FRONT_LOWER -> listOf("lower_back", "legs_feet")
        PainLocationKey.BACK_LOWER -> listOf("lower_back", "legs_feet")
        PainLocationKey.UNKNOWN -> emptyList()
    }
}

/** Map a Techniques Library region key to its string resource id. */
fun regionKeyToStringRes(key: String): Int {
    return when (key) {
        "face_head" -> R.string.region_face_head
        "neck_shoulders" -> R.string.region_neck_shoulders
        "upper_back" -> R.string.region_upper_back
        "abdomen_pelvis" -> R.string.region_mid_back_stomach
        "lower_back" -> R.string.region_lower_back
        "arms_hands" -> R.string.region_fingers_wrist_forearm
        "legs_feet" -> R.string.region_ankles_feet_toes
        else -> R.string.pain_region_title
    }
}

/**
 * Analyze pain points and return a weighted list of region keys (most relevant first).
 * We map dominant pain areas to region keys and rank by summed intensity.
 */
fun analyzePainPointsToRegionKeys(painPoints: List<PainPoint>, maxRegions: Int = 3): List<String> {
    if (painPoints.isEmpty()) return emptyList()

    val locationWeight = painPoints
        .groupBy { mapPainPointToLocationKey(it) }
        .mapValues { (_, pts) -> pts.sumOf { it.intensity } }
        .filterKeys { it != PainLocationKey.UNKNOWN }

    if (locationWeight.isEmpty()) return emptyList()

    // Expand into region keys with inherited weights and aggregate
    val regionWeights = mutableMapOf<String, Int>()
    for ((loc, weight) in locationWeight) {
        val regions = painLocationKeyToRegionKeys(loc)
        for (rk in regions) {
            regionWeights[rk] = (regionWeights[rk] ?: 0) + weight
        }
    }

    return regionWeights.entries
        .sortedByDescending { it.value }
        .map { it.key }
        .take(maxRegions)
}
