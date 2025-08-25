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
    val band = when {
        y < 0.33f -> "upper"
        y < 0.66f -> "middle"
        else -> "lower"
    }

    return when (pp.view) {
        "front" -> when (band) {
            "upper" -> PainLocationKey.FRONT_UPPER
            "middle" -> PainLocationKey.FRONT_MIDDLE
            else -> PainLocationKey.FRONT_LOWER
        }
        "back" -> when (band) {
            "upper" -> PainLocationKey.BACK_UPPER
            "middle" -> PainLocationKey.BACK_MIDDLE
            else -> PainLocationKey.BACK_LOWER
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
