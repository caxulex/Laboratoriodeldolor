package com.example.laboratoriodeldolor

/**
 * Recommendation model now carries resource ids for title and description.
 * The UI (Composable) will resolve these IDs using stringResource().
 */
data class Recommendation(
    val id: String,
    val titleResId: Int,
    val descriptionResId: Int,
    val iconResName: String? = null
)
