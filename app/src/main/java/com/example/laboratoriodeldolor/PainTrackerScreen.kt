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
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * PainTrackerScreen - clean implementation with press-duration preview.
 * - Tap/press on the silhouette to record pain points.
 * - While holding, a preview dot appears and escalates color by duration:
 *   quick tap -> yellow (1), ~1s -> orange (2), ~2s -> red (3).
 */

data class LocalPainPoint(
	val xNorm: Float,
	val yNorm: Float,
	val intensity: Int,
	val view: String = "front",
	val timestamp: Long = System.currentTimeMillis()
)

data class PreviewPoint(
	val xNorm: Float,
	val yNorm: Float,
	val intensity: Int,
	val view: String = "front"
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
	// Use a small event queue to defer additions so we don't mutate the composition tree
	// while AnimatedContent/nav transitions may be in progress.
	val addPointFlow = remember { MutableSharedFlow<LocalPainPoint>(extraBufferCapacity = 16) }
	val boxSize = remember { mutableStateOf(IntSize(300, 600)) }
	val scope = rememberCoroutineScope()
	val preview = remember { mutableStateOf<PreviewPoint?>(null) }
	val haptic = LocalHapticFeedback.current

	// Collector coroutine: perform point additions off the immediate composition callbacks
	androidx.compose.runtime.LaunchedEffect(Unit) {
		addPointFlow.collect { p ->
			points.add(p)
		}
	}

	Column(modifier = modifier.padding(16.dp)) {
		// Controls: gender + front/back - balanced two-column layout for improved visual balance
		Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
			// Gender selector
			Column(modifier = Modifier.weight(1f)) {
				Text(text = stringResource(id = R.string.gender_label), style = MaterialTheme.typography.titleMedium)
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					OutlinedButton(onClick = { genderMale.value = true }, modifier = Modifier.weight(1f)) {
						Text(text = stringResource(id = R.string.gender_male))
					}
					OutlinedButton(onClick = { genderMale.value = false }, modifier = Modifier.weight(1f)) {
						Text(text = stringResource(id = R.string.gender_female))
					}
				}
			}

			// View (Front / Back) selector
			Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
				Text(text = stringResource(id = R.string.view_front), style = MaterialTheme.typography.titleMedium)
				Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					OutlinedButton(onClick = { frontView.value = true }) { Text(text = stringResource(id = R.string.view_front)) }
					OutlinedButton(onClick = { frontView.value = false }) { Text(text = stringResource(id = R.string.view_back)) }
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
						detectTapGestures(onPress = { offset ->
							val (xNorm, yNorm) = offsetToNormalized(offset, boxSize.value)
							val currentView = if (frontView.value) "front" else "back"

							// Start preview at intensity 1 and update while pressed
							val start = System.currentTimeMillis()
							preview.value = PreviewPoint(xNorm, yNorm, 1, currentView)
							val job = scope.launch {
								var lastIntensity = 1
								while (true) {
									val elapsed = System.currentTimeMillis() - start
									val newIntensity = when {
										elapsed > 2000L -> 3
										elapsed > 1000L -> 2
										else -> 1
									}
									if (newIntensity != lastIntensity) {
										// Haptic feedback when crossing thresholds upward
										if (newIntensity > lastIntensity) {
											try {
												haptic.performHapticFeedback(HapticFeedbackType.LongPress)
											} catch (_: Exception) {
												// ignore on platforms without haptic
											}
										}
										lastIntensity = newIntensity
										val current = preview.value
										if (current != null && current.intensity != newIntensity) {
											preview.value = current.copy(intensity = newIntensity)
										}
									}
									delay(100L)
								}
							}

							var released = false
							try {
								tryAwaitRelease()
								released = true
							} catch (_: Exception) {
								// cancelled or interrupted
							} finally {
								job.cancel()
								val finalIntensity = preview.value?.intensity ?: 1
								if (released) {
									// enqueue the point so the actual mutation happens in the collector
									addPointFlow.tryEmit(LocalPainPoint(xNorm, yNorm, finalIntensity, currentView))
								}
								preview.value = null
							}
						})
					}
			) {
				// Keep a stable composition: always render an Image composable (use a transparent ColorPainter when
				// there is no actual painter) and always render the Canvas. This avoids changing the number of
				// children during navigation AnimatedContent transitions which can trigger internal node-insertion
				// mismatches in some Compose versions.
				Image(
					painter = selectedPainter ?: ColorPainter(Color.Transparent),
					contentDescription = stringResource(id = R.string.body_outline_desc),
					modifier = Modifier.fillMaxSize()
				)

				Canvas(modifier = Modifier.fillMaxSize()) {
					val w = size.width
					val h = size.height

					// If no painter provided, draw a subtle background/body placeholder
					if (selectedPainter == null) {
						val bgColor = if (frontView.value) Color(0xFFEFEFEF) else Color(0xFFF0F0F8)
						val cornerRadius = CornerRadius(24f, 24f)
						drawRoundRect(
							color = bgColor,
							topLeft = Offset.Zero,
							size = size,
							cornerRadius = cornerRadius
						)
						val circleColor = Color(0xFFD0D0D0)
						val headRadius = w.coerceAtMost(h) * 0.08f
						val headCenter = Offset(w * 0.5f, h * 0.15f)
						drawCircle(color = circleColor, radius = headRadius, center = headCenter)
					}

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

					// Preview marker while pressing (scoped where w/h exist)
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

		// Recorded points list
		Text(text = stringResource(id = R.string.recorded_points_title), style = MaterialTheme.typography.titleMedium)
		LazyColumn(modifier = Modifier.fillMaxHeight(0.25f)) {
			itemsIndexed(items = points, key = { index, point -> "${point.timestamp}_${index}" }) { index, point ->
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
