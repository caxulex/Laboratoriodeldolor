package com.example.laboratoriodeldolor.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle

/**
 * Deprecated compatibility wrappers.
 * Use `PrimaryButton` and `SecondaryButton` in `AppButtons.kt` instead.
 */
@Deprecated("Use PrimaryButton in AppButtons.kt")
@Composable
@Suppress("UNUSED_PARAMETER")
fun AppButtonPrimary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    compactHeight: Dp? = null,
    containerColor: androidx.compose.ui.graphics.Color? = null,
    contentColor: androidx.compose.ui.graphics.Color? = null,
    textStyle: TextStyle? = null,
    _shape: androidx.compose.ui.graphics.Shape? = null,
    _elevation: Dp? = null
) {
    val h = compactHeight ?: height
    com.example.laboratoriodeldolor.ui.components.PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(h),
        height = h,
        containerColor = containerColor ?: androidx.compose.material3.MaterialTheme.colorScheme.primary,
        contentColor = contentColor ?: androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
        textStyle = textStyle
    )
}

@Deprecated("Use SecondaryButton in AppButtons.kt")
@Composable
@Suppress("UNUSED_PARAMETER")
fun AppButtonSecondary(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    compactHeight: Dp? = null,
    textStyle: TextStyle? = null,
    _shape: androidx.compose.ui.graphics.Shape? = null,
    _elevation: Dp? = null
) {
    val h = compactHeight ?: height
    com.example.laboratoriodeldolor.ui.components.SecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(h),
        height = h,
        textStyle = textStyle
    )
}
