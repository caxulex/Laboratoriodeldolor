package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.laboratoriodeldolor.ui.AmbientBackground
import com.example.laboratoriodeldolor.ui.theme.Dimens
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PainCheckInScreen(painViewModel: PainTrackerViewModel, onFinish: () -> Unit, onSkip: () -> Unit) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(true) }

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(260)), exit = fadeOut(animationSpec = tween(260))) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
                Text(text = stringResource(id = R.string.checkin_pain_title), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                // Reuse the existing PainTrackerCanvas behavior: show the canvas and allow toggling
                // For simplicity, we instruct users to tap the body map to add points (PainTrackerScreen has this logic).
                Text(text = stringResource(id = R.string.checkin_pain_instruction))
                Spacer(modifier = Modifier.height(8.dp))

                    // Use settings preference for gender in the preview
                    val settingsVmPreview: SettingsViewModel = viewModel()
                    val genderPreview by settingsVmPreview.gender.collectAsState()

                    Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                        // Use the same optimized painters as the main PainTracker route to avoid broken images
                        PainTrackerScreen(
                            isMale = genderPreview,
                            // Use safePainter to ensure images load correctly in check-in mode
                            maleFrontPainter = safePainter(R.drawable.boy_front),
                            maleBackPainter = safePainter(R.drawable.boy_back),
                            femaleFrontPainter = safePainter(R.drawable.girl_front),
                            femaleBackPainter = safePainter(R.drawable.girl_back),
                            compact = true,
                            onPointsChanged = { pts ->
                                // Keep the ViewModel in sync so save uses normalized points correctly
                                painViewModel.clearPainPoints()
                                val front = pts.filter { it.view == "front" }
                                val back = pts.filter { it.view == "back" }
                                if (front.isNotEmpty()) {
                                    painViewModel.selectView("front")
                                    front.forEach { lp -> painViewModel.addPainPointNormalized(androidx.compose.ui.geometry.Offset(lp.xNorm, lp.yNorm), lp.intensity) }
                                }
                                if (back.isNotEmpty()) {
                                    painViewModel.selectView("back")
                                    back.forEach { lp -> painViewModel.addPainPointNormalized(androidx.compose.ui.geometry.Offset(lp.xNorm, lp.yNorm), lp.intensity) }
                                }
                            },
                            onSave = { /* saving handled by external Finish button */ }
                        )
                    }

                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.checkin_finish), onClick = {
                        // subtle haptic feedback when finishing
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        scope.launch { painViewModel.savePainPoints() }
                        visible = false
                        coroutineScope.launch {
                            delay(300)
                            onFinish()
                        }
                    }, modifier = Modifier.height(Dimens.buttonHeight))
                    Spacer(modifier = Modifier.width(8.dp))
                    com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.checkin_skip), onClick = {
                        visible = false
                        coroutineScope.launch {
                            delay(260)
                            onSkip()
                        }
                    }, modifier = Modifier.height(Dimens.buttonHeight))
                }
            }
        }
    }
}
