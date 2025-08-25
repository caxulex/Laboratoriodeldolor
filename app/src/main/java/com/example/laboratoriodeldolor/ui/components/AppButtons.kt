package com.example.laboratoriodeldolor.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.MaterialTheme

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
    val colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor)
    Button(onClick = onClick, modifier = modifier.height(height), colors = colors, shape = shape) {
        Text(text = text, style = textStyle ?: MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    textStyle: TextStyle? = null
) {
    val shape = RoundedCornerShape(12.dp)
    OutlinedButton(onClick = onClick, modifier = modifier.height(height), shape = shape) {
        Text(text = text, style = textStyle ?: MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
