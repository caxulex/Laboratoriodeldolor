package com.example.laboratoriodeldolor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

/**
 * Clean, self-contained PainTrackerScreen replacement.
 * - Gender + front/back selection
 * - Optional silhouette painters passed from caller
 * - Tap / press / long-press gestures -> intensity 1..3
 * - List of recorded points, clear and save actions
 */

data class LocalPainPoint(
	val xNorm: Float,
	val yNorm: Float,
	val intensity: Int,
	val view: String = "front", // "front" or "back"
	val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun PainTrackerScreen(
	modifier: Modifier = Modifier,
	maleFrontPainter: Painter? = null,
	maleBackPainter: Painter? = null,
	femaleFrontPainter: Painter? = null,
	femaleBackPainter: Painter? = null,
	onSave: (List<LocalPainPoint>) -> Unit = {}
) {
	val genderMale = remember { mutableStateOf(true) }
	val frontView = remember { mutableStateOf(true) }
	val points = remember { mutableStateListOf<LocalPainPoint>() }
	val boxSize = remember { mutableStateOf(IntSize(300, 600)) }
	val scope = rememberCoroutineScope()

	Column(modifier = modifier.padding(16.dp)) {
		// Controls: gender + front/back
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Column {
				Text(text = "Selecciona género", style = MaterialTheme.typography.titleMedium)
				Row(verticalAlignment = Alignment.CenterVertically) {
					RadioButton(selected = genderMale.value, onClick = { genderMale.value = true })
					Spacer(modifier = Modifier.size(6.dp))
					Text(text = "Hombre", modifier = Modifier.padding(end = 12.dp))
					RadioButton(selected = !genderMale.value, onClick = { genderMale.value = false })
					Spacer(modifier = Modifier.size(6.dp))
					Text(text = "Mujer")
				}
			}

			Column(horizontalAlignment = Alignment.End) {
				Text(text = "Vista", style = MaterialTheme.typography.titleMedium)
				Row(verticalAlignment = Alignment.CenterVertically) {
					OutlinedButton(onClick = { frontView.value = true }) { Text(text = "Frente") }
					Spacer(modifier = Modifier.size(8.dp))
					OutlinedButton(onClick = { frontView.value = false }) { Text(text = "Espalda") }
				}
			}
		}

		Spacer(modifier = Modifier.height(12.dp))
		Divider()
		Spacer(modifier = Modifier.height(12.dp))

		// Body area with gestures
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(420.dp)
				.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
				.background(Color(0xFFF9F9F9))
		) {
			val selectedPainter = when {
				genderMale.value && frontView.value -> maleFrontPainter
				genderMale.value && !frontView.value -> maleBackPainter
				!genderMale.value && frontView.value -> femaleFrontPainter
				else -> femaleBackPainter
			}

			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp)
					.onSizeChanged { size -> boxSize.value = size }
					.pointerInput(Unit) {
						detectTapGestures(
							onTap = { offset ->
								val (xNorm, yNorm) = offsetToNormalized(offset, boxSize.value)
								val currentView = if (frontView.value) "front" else "back"
								points.add(LocalPainPoint(xNorm, yNorm, 1, currentView))
							},
							onLongPress = { offset ->
								val (xNorm, yNorm) = offsetToNormalized(offset, boxSize.value)
								val currentView = if (frontView.value) "front" else "back"
								points.add(LocalPainPoint(xNorm, yNorm, 3, currentView))
							},
							onPress = { offset ->
								val start = System.currentTimeMillis()
								try {
									tryAwaitRelease()
									val duration = System.currentTimeMillis() - start
									val intensity = when {
										duration > 2000L -> 3
										duration > 1000L -> 2
										else -> 1
									}
									val (xNorm, yNorm) = offsetToNormalized(offset, boxSize.value)
									val currentView = if (frontView.value) "front" else "back"
									points.add(LocalPainPoint(xNorm, yNorm, intensity, currentView))
								} catch (_: Exception) {
									// cancelled
								}
							}
						)
					}
			) {
				if (selectedPainter != null) {
					Image(
						painter = selectedPainter,
						contentDescription = "Silhouette",
						modifier = Modifier.fillMaxSize()
					)

					Canvas(modifier = Modifier.fillMaxSize()) {
						val w = size.width
						val h = size.height
						points.forEach { p ->
							val cx = p.xNorm * w
							val cy = p.yNorm * h
							val radius = 18f
							val color = when (p.intensity) {
								3 -> Color.Red
								2 -> Color(0xFFFFA500)
								else -> Color.Yellow
							}
							drawCircle(color = color, radius = radius, center = Offset(cx, cy))
						}
					}
				} else {
					Canvas(modifier = Modifier.fillMaxSize()) {
						val w = size.width
						val h = size.height
						drawRoundRect(
							color = if (frontView.value) Color(0xFFEFEFEF) else Color(0xFFF0F0F8),
							topLeft = Offset(0f, 0f),
							size = size,
							cornerRadius = CornerRadius(24f, 24f)
						)
						drawCircle(
							color = Color(0xFFD0D0D0),
							radius = (w.coerceAtMost(h) * 0.08f),
							center = Offset(w * 0.5f, h * 0.15f)
						)
						points.forEach { p ->
							val cx = p.xNorm * w
							val cy = p.yNorm * h
							val radius = 18f
							val color = when (p.intensity) {
								3 -> Color.Red
								2 -> Color(0xFFFFA500)
								else -> Color.Yellow
							}
							drawCircle(color = color, radius = radius, center = Offset(cx, cy))
						}
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		// Recorded points list
		Text(text = stringResource(id = R.string.recorded_points_title), style = MaterialTheme.typography.titleMedium)
		LazyColumn(modifier = Modifier.fillMaxHeight(0.25f)) {
			itemsIndexed(points) { index, point ->
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 6.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					val intensityLabel = when (point.intensity) {
						3 -> stringResource(id = R.string.intensity_severe)
						2 -> stringResource(id = R.string.intensity_high)
						else -> stringResource(id = R.string.intensity_moderate)
					}
					Text(text = "Punto ${index + 1}: Intensidad = $intensityLabel")
					Spacer(modifier = Modifier.weight(1f))
					IconButton(onClick = { points.removeAt(index) }) {
						Icon(imageVector = Icons.Filled.Delete, contentDescription = "Eliminar")
					}
				}
				Divider()
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		// Actions
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier.fillMaxWidth()
		) {
			Button(onClick = { points.clear() }) { Text(text = stringResource(id = R.string.clear_button)) }

			Button(onClick = { scope.launch { onSave(points.toList()) } }) { Text(text = stringResource(id = R.string.save_pain_button)) }
		}
	}
}

/** Convert pointer offset to normalized 0..1 coords using captured box size. */
private fun offsetToNormalized(offset: Offset, size: IntSize): Pair<Float, Float> {
	val w = size.width.takeIf { it > 0 } ?: 1
	val h = size.height.takeIf { it > 0 } ?: 1
	val x = (offset.x / w).coerceIn(0f, 1f)
	val y = (offset.y / h).coerceIn(0f, 1f)
	return Pair(x, y)
}
