package com.example.laboratoriodeldolor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row  
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.laboratoriodeldolor.ui.accessibility.getPainIntensityDescription

/**
 * Accessible pain intensity selector component
 * Provides clear labels and descriptions for screen readers
 */
@Composable
fun AccessiblePainIntensitySelector(
    selectedIntensity: Int,
    onIntensityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .semantics {
                contentDescription = "Pain intensity selector"
            }
    ) {
        Text(
            text = "Pain Intensity Level",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        val intensityOptions = listOf(
            0 to "No Pain",
            1 to "Mild Pain", 
            2 to "Moderate Pain",
            3 to "Severe Pain"
        )
        
        intensityOptions.forEach { (intensity, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // Minimum touch target
                    .selectable(
                        selected = selectedIntensity == intensity,
                        onClick = { onIntensityChange(intensity) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp)
                    .semantics {
                        contentDescription = getPainIntensityDescription(intensity)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedIntensity == intensity,
                    onClick = null, // Click handled by parent
                    modifier = Modifier.clearAndSetSemantics { }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
        }
    }
}

/**
 * Accessible body view selector (front/back toggle)
 */
@Composable  
fun AccessibleBodyViewSelector(
    selectedView: String,
    onViewChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectableGroup()
            .semantics {
                contentDescription = "Body view selector - choose front or back view"
            },
        horizontalArrangement = Arrangement.Center
    ) {
        val views = listOf("front" to "Front View", "back" to "Back View")
        
        views.forEachIndexed { index, (view, label) ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = selectedView == view,
                        onClick = { onViewChange(view) },
                        role = Role.RadioButton
                    )
                    .padding(8.dp)
                    .semantics {
                        contentDescription = "$label ${if (selectedView == view) "selected" else "not selected"}"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedView == view,
                    onClick = null, // Click handled by parent
                    modifier = Modifier.clearAndSetSemantics { }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
            
            if (index < views.size - 1) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}