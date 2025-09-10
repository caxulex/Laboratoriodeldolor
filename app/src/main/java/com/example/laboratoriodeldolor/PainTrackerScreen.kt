package com.example.laboratoriodeldolor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Data model for saved pain points.
 */
data class LocalPainPoint(
	val xNorm: Float,
	val yNorm: Float,
	val intensity: Int,
	val view: String = "front",
	val timestamp: Long = System.currentTimeMillis()
)

/**
 * Transient preview shown while the user presses on the silhouette.
 */
data class PreviewPoint(
	val xNorm: Float,
	val yNorm: Float,
	val intensity: Int,
	val view: String = "front"
)

/**
 * PainTrackerScreen
 * - Tap = immediate intensity 1
 * - Press: hold 0..1s => intensity 1 (yellow)
 *          1..2s => intensity 2 (orange)
 *          2s+   => intensity 3 (red)
 * - On release the final intensity is saved to the points list
 */
@Composable
fun PainTrackerScreen(
	modifier: Modifier = Modifier,
	maleFrontPainter: Painter? = null,
	maleBackPainter: Painter? = null,
	femaleFrontPainter: Painter? = null,
	femaleBackPainter: Painter? = null,
	isMale: Boolean = true,
	onSave: (List<LocalPainPoint>) -> Unit = {},
	onOpenTechniques: () -> Unit = {}
) {
	val frontView = remember { mutableStateOf(true) }
	val points = remember { mutableStateListOf<LocalPainPoint>() }
	val boxSize = remember { mutableStateOf(IntSize(300, 600)) }
	val scope = rememberCoroutineScope()
	val savedAt = remember { mutableStateOf<Long?>(null) }

	com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
	Column(modifier = modifier.padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState())) {
			val preview = remember { mutableStateOf<PreviewPoint?>(null) }

		// Controls: view toggle (gender moved to Settings)
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Spacer to push view controls to the right without forcing a minimum width
			Spacer(modifier = Modifier.weight(1f))

			// Right: view selector
			Column(horizontalAlignment = Alignment.End) {
				Text(text = stringResource(id = R.string.view_label), style = MaterialTheme.typography.titleMedium)
				Spacer(modifier = Modifier.height(6.dp))
				Row(verticalAlignment = Alignment.CenterVertically) {
					OutlinedButton(onClick = { frontView.value = true }, modifier = Modifier.width(100.dp)) {
						Text(text = stringResource(id = R.string.view_front))
					}
					Spacer(modifier = Modifier.width(8.dp))
					OutlinedButton(onClick = { frontView.value = false }, modifier = Modifier.width(100.dp)) {
						Text(text = stringResource(id = R.string.view_back))
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(12.dp))
		Divider()
		Spacer(modifier = Modifier.height(12.dp))

		// Body area with gestures and gradient background to match app theme
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(420.dp)
				.border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
				.background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceVariant)))
		) {
			val selectedPainter = when {
				isMale && frontView.value -> maleFrontPainter
				isMale && !frontView.value -> maleBackPainter
				!isMale && frontView.value -> femaleFrontPainter
				else -> femaleBackPainter
			}

			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp)
					.onSizeChanged { size -> boxSize.value = size }
					// draw a subtle background behind the silhouette so the image isn't obscured by the Canvas
					.background(if (frontView.value) Color(0xFFEFEFEF) else Color(0xFFF0F0F8))
					.pointerInput(Unit) {
						detectTapGestures(
							onTap = { offset ->
								// quick tap -> intensity 1
								val (xNorm, yNorm) = offsetToNormalized(offset, boxSize.value)
								val currentView = if (frontView.value) "front" else "back"
								points.add(LocalPainPoint(xNorm, yNorm, 1, currentView))
							},
							onPress = { offset ->
								val (xNorm, yNorm) = offsetToNormalized(offset, boxSize.value)
								val currentView = if (frontView.value) "front" else "back"

								// start preview
								val start = System.currentTimeMillis()
								preview.value = PreviewPoint(xNorm, yNorm, 1, currentView)

								// periodically update preview intensity while pressed
								val job = scope.launch {
									try {
										while (isActive) {
											val elapsed = System.currentTimeMillis() - start
											val newIntensity = when {
												elapsed > 2000L -> 3
												elapsed > 1000L -> 2
												else -> 1
											}
											val cur = preview.value
											if (cur != null && cur.intensity != newIntensity) {
												preview.value = cur.copy(intensity = newIntensity)
											}
											delay(100L)
										}
									} catch (_: Exception) {
										// canceled
									}
								}

								try {
									tryAwaitRelease()
									// on release, record final intensity
									val finalIntensity = preview.value?.intensity ?: 1
									points.add(LocalPainPoint(xNorm, yNorm, finalIntensity, currentView))
								} finally {
									job.cancel()
									preview.value = null
								}
							}
						)
					}
			) {
						// Draw the silhouette painter (any Painter).
						if (selectedPainter != null) {
							Image(
								painter = selectedPainter,
								contentDescription = stringResource(id = R.string.body_outline_desc),
								modifier = Modifier.fillMaxSize(),
								contentScale = ContentScale.Fit
							)
						} else {
							// Fallback placeholder when image resource failed to load or is null.
							Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
								Column(horizontalAlignment = Alignment.CenterHorizontally) {
									Box(modifier = Modifier.size(220.dp).background(Color(0xFFBDBDBD), RoundedCornerShape(8.dp)))
									Spacer(modifier = Modifier.height(8.dp))
									Text(text = "Imagen no disponible", color = Color.White)
								}
							}
						}

				// Unified Canvas: draw saved points and preview overlay on top of the silhouette
				Canvas(modifier = Modifier.fillMaxSize()) {
					val w = size.width
					val h = size.height

					// saved points
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

					// preview (on top)
					preview.value?.let { pv ->
						val pcx = pv.xNorm * w
						val pcy = pv.yNorm * h
						val pradius = 22f
						val pcolor = when (pv.intensity) {
							3 -> Color.Red
							2 -> Color(0xFFFFA500)
							else -> Color.Yellow
						}
						drawCircle(color = pcolor, radius = pradius, center = Offset(pcx, pcy))
					}
				}
			}
		}

	Spacer(modifier = Modifier.height(12.dp))

		// Recorded points list (simple column to avoid nested LazyColumn + outer scroll)
		Text(text = stringResource(id = R.string.recorded_points_title), style = MaterialTheme.typography.titleMedium)
		Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
			points.forEachIndexed { index, point ->
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
					Text(text = stringResource(id = R.string.recorded_point_format, index + 1, intensityLabel))
					Spacer(modifier = Modifier.weight(1f))
					IconButton(onClick = { points.removeAt(index) }) {
						Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(id = R.string.delete_confirm_title))
					}
				}
				Divider()
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		// Actions (clear / techniques / save)
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier.fillMaxWidth()
		) {
			Button(onClick = { points.clear() }) { Text(text = stringResource(id = R.string.clear_button)) }

			ElevatedButton(onClick = { onOpenTechniques() }, colors = ButtonDefaults.elevatedButtonColors()) {
				Text(text = stringResource(id = R.string.techniques_library_title))
			}

			Button(onClick = {
				scope.launch {
					onSave(points.toList())
					savedAt.value = System.currentTimeMillis()
				}
			}) { Text(text = stringResource(id = R.string.save_pain_button)) }
		}

		// Small confirmation when saved
		savedAt.value?.let { ts ->
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = stringResource(id = R.string.pain_save_confirmation), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
		}
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

