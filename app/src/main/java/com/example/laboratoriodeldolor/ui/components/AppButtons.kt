package com.example.laboratoriodeldolor.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    textStyle: TextStyle? = null
) {
    val shape = RoundedCornerShape(12.dp)
    NeumorphicButton(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Text(text = text, style = textStyle ?: MaterialTheme.typography.labelLarge, color = contentColor)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    textStyle: TextStyle? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val shape = RoundedCornerShape(12.dp)
    NeumorphicButton(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Text(text = text, style = textStyle ?: MaterialTheme.typography.labelLarge, color = contentColor)
    }
}
