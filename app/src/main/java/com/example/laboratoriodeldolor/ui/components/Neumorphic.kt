package com.example.laboratoriodeldolor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Dp = 8.dp,
    pressedElevation: Dp = 2.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val currentElevation = if (enabled) (if (pressed) pressedElevation else elevation) else 0.dp
    val lightOverlayAlpha = if (pressed) 0.08f else 0.14f

    val ambientShadow = Color(0x22000000) // soft dark
    val spotShadow = Color(0x33000000)

    val base = Modifier
        .shadow(currentElevation, shape = shape, clip = false, ambientColor = ambientShadow, spotColor = spotShadow)
        .clip(shape)
        .background(containerColor)
        .drawWithContent {
            drawContent()
            // subtle top-left light sheen
            val overlay = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = lightOverlayAlpha), Color.Transparent),
                start = androidx.compose.ui.geometry.Offset.Zero,
                end = androidx.compose.ui.geometry.Offset(size.width, size.height)
            )
            drawRect(overlay)
        }

    Row(
        modifier = modifier
            .then(base)
            .defaultMinSize(minHeight = 44.dp)
            .semantics { role = Role.Button }
            .clickable(
                enabled = enabled,
                role = Role.Button,
                indication = null, // keep the soft look; ripple can be too harsh
                interactionSource = interactionSource,
                onClick = onClick
            )
            .padding(contentPadding),
        content = {
            ProvideTextStyle(value = MaterialTheme.typography.labelLarge.copy(color = contentColor)) {
                this.content()
            }
        }
    )
}

@Composable
fun NeumorphicTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    NeumorphicButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        androidx.compose.material3.Text(text = text, color = contentColor)
    }
}

@Composable
fun NeumorphicSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val spacing = 6.dp
    Row(
        modifier = modifier,
        content = {
            options.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 10.dp, bottomEnd = 10.dp)
                    options.lastIndex -> RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 16.dp, bottomEnd = 16.dp)
                    else -> RoundedCornerShape(10.dp)
                }
                NeumorphicButton(
                    onClick = { onSelectedIndexChange(index) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = if (index < options.lastIndex) spacing else 0.dp),
                    shape = shape,
                    containerColor = if (isSelected) selectedColor else containerColor,
                    contentColor = if (isSelected) selectedContentColor else contentColor,
                    content = {
                        androidx.compose.material3.Text(text = label, color = if (isSelected) selectedContentColor else contentColor)
                    }
                )
            }
        }
    )
}
