package com.example.laboratoriodeldolor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.laboratoriodeldolor.ui.GradientBackground
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch

import com.example.laboratoriodeldolor.ui.theme.Dimens

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PainTrackerScreen(
	viewModel: PainTrackerViewModel,
	onNavigateToUpper: () -> Unit = {},
	onNavigateToMiddle: () -> Unit = {},
	onNavigateToLower: () -> Unit = {},
	onNavigateToExercise: (String) -> Unit = {}
) {
	val scope = rememberCoroutineScope()

	val frontPainPoints by viewModel.frontPainPoints.collectAsState()
	val backPainPoints by viewModel.backPainPoints.collectAsState()
	val selectedAreas by viewModel.selectedAreas.collectAsState()
	val gender by viewModel.selectedGender.collectAsState()
	val viewSel by viewModel.selectedView.collectAsState()

	var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }
	var toDeleteId by remember { mutableStateOf<Long?>(null) }

	androidx.compose.material3.Scaffold { innerPadding ->
		// Full-screen gradient background, content layered above
		Box(modifier = Modifier.fillMaxSize()) {
			GradientBackground()
			Column(modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.padding(16.dp)) {

				androidx.compose.material3.Text(
					text = stringResource(id = R.string.pain_tracker_title),
					style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
					modifier = Modifier.padding(bottom = 16.dp).testTag("screen_title")
				)

				Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
					// Segmented gender buttons: Masculino / Femenino
					if (gender == "male") {
						com.example.laboratoriodeldolor.ui.components.PrimaryButton(
							text = stringResource(id = R.string.gender_male),
							onClick = { viewModel.selectGender("male") },
							modifier = Modifier.height(32.dp).testTag("gender_male"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					} else {
						com.example.laboratoriodeldolor.ui.components.SecondaryButton(
							text = stringResource(id = R.string.gender_male),
							onClick = { viewModel.selectGender("male") },
							modifier = Modifier.height(32.dp).testTag("gender_male"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					}

					if (gender == "female") {
						com.example.laboratoriodeldolor.ui.components.PrimaryButton(
							text = stringResource(id = R.string.gender_female),
							onClick = { viewModel.selectGender("female") },
							modifier = Modifier.height(32.dp).testTag("gender_female"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					} else {
						com.example.laboratoriodeldolor.ui.components.SecondaryButton(
							text = stringResource(id = R.string.gender_female),
							onClick = { viewModel.selectGender("female") },
							modifier = Modifier.height(32.dp).testTag("gender_female"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					}
				}

				Spacer(modifier = Modifier.height(Dimens.spaceMedium))

				Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
					if (viewSel == "front") {
						com.example.laboratoriodeldolor.ui.components.PrimaryButton(
							text = stringResource(id = R.string.view_front),
							onClick = { viewModel.selectView("front") },
							modifier = Modifier.height(32.dp).testTag("view_front"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					} else {
						com.example.laboratoriodeldolor.ui.components.SecondaryButton(
							text = stringResource(id = R.string.view_front),
							onClick = { viewModel.selectView("front") },
							modifier = Modifier.height(32.dp).testTag("view_front"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					}

					if (viewSel == "back") {
						com.example.laboratoriodeldolor.ui.components.PrimaryButton(
							text = stringResource(id = R.string.view_back),
							onClick = { viewModel.selectView("back") },
							modifier = Modifier.height(32.dp).testTag("view_back"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					} else {
						com.example.laboratoriodeldolor.ui.components.SecondaryButton(
							text = stringResource(id = R.string.view_back),
							onClick = { viewModel.selectView("back") },
							modifier = Modifier.height(32.dp).testTag("view_back"),
							textStyle = androidx.compose.material3.MaterialTheme.typography.bodySmall
						)
					}
				}

				Spacer(modifier = Modifier.height(Dimens.spaceLarge))

				// Precompute overlay colors in the composable scope so they aren't accessed from DrawScope
				val upperOverlayColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.33f)
				val middleOverlayColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.33f)
				val lowerOverlayColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.33f)
				// color for pain points (use theme's error red)
				val painPointColor = androidx.compose.material3.MaterialTheme.colorScheme.error

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(420.dp),
					contentAlignment = Alignment.TopCenter
				) {
					val drawableId = when (gender to viewSel) {
						Pair("male", "front") -> R.drawable.boy_front
						Pair("male", "back") -> R.drawable.boy_back
						Pair("female", "front") -> R.drawable.girl_front
						Pair("female", "back") -> R.drawable.girl_back
						else -> R.drawable.body_outline
					}

					Image(
						painter = rememberAsyncImagePainter(
							ImageRequest.Builder(LocalContext.current).data(drawableId).build()
						),
						contentDescription = stringResource(id = R.string.body_outline_desc),
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Fit
					)

					Canvas(modifier = Modifier
						.fillMaxSize()
						.onSizeChanged { canvasSize = it }
						.testTag("bodyCanvas")
						.pointerInput(Unit) {
							detectTapGestures { tap: Offset ->
								if (canvasSize.width > 0 && canvasSize.height > 0) {
									val nx = tap.x / canvasSize.width.toFloat()
									val ny = tap.y / canvasSize.height.toFloat()
									viewModel.addPainPointNormalized(Offset(nx, ny))
								}
							}
						}
					) {
						val radius = size.minDimension * 0.03f

						val bandHeight = size.height / 3f
						if (selectedAreas.contains(BodyArea.UPPER)) {
							drawRect(
								color = upperOverlayColor,
								topLeft = Offset(0f, 0f),
								size = Size(size.width, bandHeight)
							)
						}
						if (selectedAreas.contains(BodyArea.MIDDLE)) {
							drawRect(
								color = middleOverlayColor,
								topLeft = Offset(0f, bandHeight),
								size = Size(size.width, bandHeight)
							)
						}
						if (selectedAreas.contains(BodyArea.LOWER)) {
							drawRect(
								color = lowerOverlayColor,
								topLeft = Offset(0f, bandHeight * 2f),
								size = Size(size.width, bandHeight)
							)
						}

						// Draw only the points for the currently selected view
						val pointsToDraw = if (viewSel == "front") frontPainPoints else backPainPoints
						pointsToDraw.forEach { normalizedPt ->
							val px = normalizedPt.x * size.width
							val py = normalizedPt.y * size.height
							drawCircle(color = painPointColor, radius = radius, center = Offset(px, py))
						}
					}
				}

				val hasPoints = frontPainPoints.isNotEmpty() || backPainPoints.isNotEmpty()
				AnimatedVisibility(
					visible = hasPoints,
					enter = fadeIn(animationSpec = tween(250)) + slideInVertically(animationSpec = tween(250), initialOffsetY = { it / 4 }),
					exit = fadeOut(animationSpec = tween(180)) + slideOutVertically(animationSpec = tween(180), targetOffsetY = { it / 4 })
				) {
					Row(modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = Dimens.spaceSmall), verticalAlignment = Alignment.CenterVertically) {
						com.example.laboratoriodeldolor.ui.components.LottieSaveButton(enabled = hasPoints, onSave = { scope.launch { viewModel.savePainPoints() } })

						Spacer(modifier = Modifier.width(Dimens.spaceLarge))

						com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.clear_button), onClick = { viewModel.clearPainPoints() }, modifier = Modifier.height(Dimens.buttonHeight))
					}
				}

				val persistedFlow = viewModel.painPointsFromDb
					if (persistedFlow != null) {
					val persisted by persistedFlow.collectAsState(initial = emptyList())
					LazyColumn(modifier = Modifier.fillMaxWidth()) {
						items(persisted) { pp ->
							Row(modifier = Modifier
								.fillMaxWidth()
									.padding(8.dp).testTag("persisted_row_${'$'}{pp.id}"), verticalAlignment = Alignment.CenterVertically) {
								// Map persisted point to descriptive location
								val key = mapPainPointToLocationKey(pp)
								val labelRes = painLocationKeyToStringRes(key)
								androidx.compose.material3.Text(text = "#${pp.id}: " + stringResource(id = labelRes))
								Spacer(modifier = Modifier.width(16.dp))
								// Open specific exercises for this pain point
								androidx.compose.material3.OutlinedButton(onClick = {
									// Map to specific route based on PainLocationKey
									val key = mapPainPointToLocationKey(pp)
									val route = when (key) {
										PainLocationKey.FRONT_UPPER -> Screen.FrontUpperBody.route
										PainLocationKey.BACK_UPPER -> Screen.BackUpperBody.route
										PainLocationKey.FRONT_MIDDLE -> Screen.FrontMiddleBody.route
										PainLocationKey.BACK_MIDDLE -> Screen.BackMiddleBody.route
										PainLocationKey.FRONT_LOWER -> Screen.FrontLowerBody.route
										PainLocationKey.BACK_LOWER -> Screen.BackLowerBody.route
										else -> Screen.Exercises.route
									}
									onNavigateToExercise(route)
								}, modifier = Modifier.height(40.dp)) {
									androidx.compose.material3.Text(text = stringResource(id = R.string.view_exercises))
								}
							}
						}
					}
				}

				if (selectedAreas.isNotEmpty()) {
					Row(modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 16.dp), horizontalArrangement = Arrangement.Center) {
						if (selectedAreas.contains(BodyArea.UPPER)) {
							com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.upper_body_exercises_button), onClick = onNavigateToUpper, modifier = Modifier.height(48.dp))
						}
						if (selectedAreas.contains(BodyArea.MIDDLE)) {
							Spacer(modifier = Modifier.width(12.dp))
							com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.middle_body_exercises_button), onClick = onNavigateToMiddle, modifier = Modifier.height(48.dp))
						}
						if (selectedAreas.contains(BodyArea.LOWER)) {
							Spacer(modifier = Modifier.width(12.dp))
							com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.lower_body_exercises_button), onClick = onNavigateToLower, modifier = Modifier.height(48.dp))
						}
					}
				}

				if (toDeleteId != null) {
					androidx.compose.material3.AlertDialog(
						onDismissRequest = { toDeleteId = null },
						title = { androidx.compose.material3.Text(text = stringResource(id = R.string.delete_confirm_title)) },
						text = { androidx.compose.material3.Text(text = stringResource(id = R.string.delete_confirm_message)) },
						confirmButton = {
							com.example.laboratoriodeldolor.ui.components.PrimaryButton(
								text = stringResource(id = R.string.delete_confirm_yes),
								onClick = {
									viewModel.deletePersistedById(toDeleteId!!)
									toDeleteId = null
								},
								modifier = Modifier.height(40.dp)
							)
						},
						dismissButton = {
							com.example.laboratoriodeldolor.ui.components.SecondaryButton(
								text = stringResource(id = R.string.delete_confirm_no),
								onClick = { toDeleteId = null },
								modifier = Modifier.height(40.dp)
							)
						}
					)
				}

			}
		}
	}
}
