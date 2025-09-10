package com.example.laboratoriodeldolor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text as MText
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import android.graphics.BitmapFactory
import android.content.res.Resources
import com.example.laboratoriodeldolor.ui.theme.LaboratorioDelDolorTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Install a default uncaught exception handler to log crashes from any thread (including Compose)
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("startup", "Uncaught exception", throwable)
            previousHandler?.uncaughtException(thread, throwable)
        }

        setContent {
            // Observe persisted theme preference and pass it to the app theme so changes apply instantly
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDark by settingsViewModel.isDarkMode.collectAsState()

            LaboratorioDelDolorTheme(isDark = isDark) {
                val navController = rememberNavController()

                // Determine start destination after DB warmup and preference check. Default to checkin flow
                val startDestinationState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    try {
                        (application as? MoodApplication)?.awaitDatabaseReady()
                        val prefs = (application as MoodApplication).preferencesRepository
                        val seen = prefs.hasSeenOnboardingFlow.first()
                        // If user has seen onboarding, set Diario as start; otherwise onboarding
                        startDestinationState.value = if (seen) Screen.Diario.route else "onboarding"
                    } catch (t: Throwable) {
                        // keep startup resilient; fallback to checkin mood if anything fails
                        startDestinationState.value = Screen.CheckinMood.route
                    }
                }

                val items = listOf(Screen.Home, Screen.Diario, Screen.Progress, Screen.Ajustes)

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide the BottomBar during the check-in flow
                val showBottomBar = currentRoute != Screen.CheckinMood.route && currentRoute != Screen.CheckinPain.route

                Scaffold(
                    bottomBar = { if (showBottomBar) BottomBar(navController = navController, items = items) }
                ) { innerPadding ->
                    // NavHost sits inside the app-level scaffold; individual screens draw AppScaffold which renders the gradient
                    // Use startDestinationState if available; otherwise default to the checkin flow while we wait
                    val startDest = startDestinationState.value ?: Screen.CheckinMood.route
                    NavHost(navController = navController, startDestination = startDest, modifier = Modifier.padding(innerPadding)) {
                        // Check-in flow
                        composable(Screen.CheckinMood.route) {
                            val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModelFactory((application as MoodApplication).database.moodDao(), (application as MoodApplication).database.exerciseDao(), (application as MoodApplication).preferencesRepository))
                            MoodCheckInScreen(moodViewModel,
                                onNext = { navController.navigate(Screen.CheckinPain.route) },
                                onSkip = { navController.navigate(Screen.CheckinPain.route) },
                                onSaved = { emoji ->
                                    if (emoji == "😞") {
                                        moodViewModel.setDashboardPriority(MoodViewModel.DashboardPriority.BREATH)
                                    } else {
                                        moodViewModel.setDashboardPriority(MoodViewModel.DashboardPriority.DIARY)
                                    }
                                }
                            )
                        }
                        composable(Screen.CheckinPain.route) {
                            val painTrackerViewModel: PainTrackerViewModel = viewModel(factory = PainTrackerViewModelFactory((application as MoodApplication).database.painPointDao(), (application as MoodApplication).database.painLogDao()))
                            val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModelFactory((application as MoodApplication).database.moodDao(), (application as MoodApplication).database.exerciseDao(), (application as MoodApplication).preferencesRepository))
                            PainCheckInScreen(painTrackerViewModel, onFinish = {
                                // user saved pain -> prioritize PAIN module
                                moodViewModel.setDashboardPriority(MoodViewModel.DashboardPriority.PAIN)
                                navController.navigate(Screen.Diario.route) {
                                    popUpTo(Screen.CheckinMood.route) { inclusive = true }
                                }
                            }, onSkip = {
                                // didn't save pain: fallback to DIARY by default (or mood-based prioritization handled in mood save)
                                navController.navigate(Screen.Diario.route) {
                                    popUpTo(Screen.CheckinMood.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Diario.route) {
                            val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModelFactory((application as MoodApplication).database.moodDao(), (application as MoodApplication).database.exerciseDao(), (application as MoodApplication).preferencesRepository))
                            DailyMoodScreen(
                                viewModel = moodViewModel,
                                onNavigateToPainTracker = { navController.navigate(Screen.Dolor.route) },
                                onNavigateToSettings = { navController.navigate(Screen.Ajustes.route) },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToBreath = { navController.navigate(Screen.Respiracion.route) },
                                onNavigateToDiary = { navController.navigate(Screen.Diary.route) },
                                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                                onOpenPainChart = { navController.navigate("pain_chart") },
                                onOpenTechniquesLibrary = { navController.navigate("techniques") },
                                onOpenExerciseHub = { navController.navigate(Screen.Exercises.route) }
                            )
                        }

                            // Onboarding / Home flow
                            composable("onboarding") {
                                OnboardingScreen(onFinish = {
                                    // mark as seen and navigate to Diario (daily mood) so Diario becomes the landing screen
                                    lifecycleScope.launch {
                                        (application as MoodApplication).preferencesRepository.setHasSeenOnboarding(true)
                                        navController.navigate(Screen.Diario.route) {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                }, onSkip = {
                                    lifecycleScope.launch {
                                        (application as MoodApplication).preferencesRepository.setHasSeenOnboarding(true)
                                        navController.navigate(Screen.Diario.route) {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                })
                            }

                            composable(Screen.Home.route) {
                                HomeScreen(
                                    routineDao = (application as MoodApplication).database.routineDao(),
                                    onOpenPainRegion = { rid -> navController.navigate("pain_region/$rid") },
                                    onOpenPainTracker = { navController.navigate(Screen.Dolor.route) },
                                    onOpenTechniques = { navController.navigate("techniques") },
                                    onOpenSettings = { navController.navigate(Screen.Ajustes.route) }
                                )
                            }
                            composable(Screen.Dolor.route) {
                                val painTrackerViewModel: PainTrackerViewModel = viewModel(factory = PainTrackerViewModelFactory((application as MoodApplication).database.painPointDao(), (application as MoodApplication).database.painLogDao()))
                                // Read persisted gender preference and pass into the PainTrackerScreen
                                val settingsVmForPain: SettingsViewModel = viewModel()
                                val genderPref by settingsVmForPain.gender.collectAsState()

                                PainTrackerScreen(
                                    isMale = genderPref,
                                    maleFrontPainter = safePainter(R.drawable.boy_front),
                                    maleBackPainter = safePainter(R.drawable.boy_back),
                                    femaleFrontPainter = safePainter(R.drawable.girl_front),
                                    femaleBackPainter = safePainter(R.drawable.girl_back),
                                    onSave = { points ->
                                        // Save points using ViewModel and navigate to recommended exercise
                                        lifecycleScope.launch {
                                            // Clear existing points in ViewModel
                                            painTrackerViewModel.clearPainPoints()
                                            
                                            // Group points by view (front/back) and add them properly
                                            val frontPoints = points.filter { it.view == "front" }
                                            val backPoints = points.filter { it.view == "back" }
                                            
                                            // Add front view points
                                            if (frontPoints.isNotEmpty()) {
                                                painTrackerViewModel.selectView("front")
                                                frontPoints.forEach { lp ->
                                                    painTrackerViewModel.addPainPointNormalized(
                                                        androidx.compose.ui.geometry.Offset(lp.xNorm, lp.yNorm), 
                                                        lp.intensity
                                                    )
                                                }
                                            }
                                            
                                            // Add back view points
                                            if (backPoints.isNotEmpty()) {
                                                painTrackerViewModel.selectView("back")
                                                backPoints.forEach { lp ->
                                                    painTrackerViewModel.addPainPointNormalized(
                                                        androidx.compose.ui.geometry.Offset(lp.xNorm, lp.yNorm), 
                                                        lp.intensity
                                                    )
                                                }
                                            }
                                            
                                            // Save to database and get recommended exercise route
                                            val recommendedRoute = painTrackerViewModel.savePainPoints()
                                            
                                            // Navigate to the recommended exercise screen using smart analysis
                                            if (recommendedRoute != null) {
                                                navController.navigate(recommendedRoute)
                                            } else {
                                                // Fallback: analyze points directly and navigate
                                                val painPoints = (frontPoints + backPoints).map { lp ->
                                                    PainPoint(
                                                        x = lp.xNorm,
                                                        y = lp.yNorm,
                                                        view = lp.view,
                                                        intensity = lp.intensity
                                                    )
                                                }
                                                val smartRoute = analyzePainPointsForNavigation(painPoints)
                                                navController.navigate(smartRoute)
                                            }
                                        }
                                    }
                                )
                            }
                            composable(Screen.Ajustes.route) {
                                // Use a distinct local name to avoid shadowing the top-level settingsViewModel
                                val settingsVm: SettingsViewModel = viewModel()
                                SettingsScreen(viewModel = settingsVm, onNavigateToAbout = { navController.navigate("about") })
                            }
                            composable(Screen.Diary.route) {
                                val diaryViewModel: DiaryViewModel = viewModel(factory = DiaryViewModelFactory((application as MoodApplication).database.moodDao()))
                                DiaryScreen(diaryViewModel = diaryViewModel, onBack = { navController.popBackStack() })
                            }
                            composable("about") {
                                AboutScreen(onBack = { navController.popBackStack() })
                            }
                            composable(Screen.Exercises.route) {
                                ExerciseHubScreen(
                                    onNavigateToFrontUpper = { navController.navigate(Screen.FrontUpperBody.route) },
                                    onNavigateToBackUpper = { navController.navigate(Screen.BackUpperBody.route) },
                                    onNavigateToFrontMiddle = { navController.navigate(Screen.FrontMiddleBody.route) },
                                    onNavigateToBackMiddle = { navController.navigate(Screen.BackMiddleBody.route) },
                                    onNavigateToFrontLower = { navController.navigate(Screen.FrontLowerBody.route) },
                                    onNavigateToBackLower = { navController.navigate(Screen.BackLowerBody.route) }
                                )
                            }
                            // Recommendation screen removed - users are navigated directly to technique pages after saving pain points
                            composable(Screen.Respiracion.route) {
                                val breathWorkViewModel: BreathWorkViewModel = viewModel(factory = BreathWorkViewModelFactory((application as MoodApplication).database.moodDao()))
                                BreathWorkScreen(viewModel = breathWorkViewModel, onInstruction = { _ -> /* TODO: navigate to instructions */ })
                            }
                            composable(Screen.Progress.route) {
                                // Provide MoodDao and PainDao from application to the chart screen
                                com.example.laboratoriodeldolor.MoodProgressScreen(
                                    moodDao = (application as MoodApplication).database.moodDao(),
                                    painDao = (application as MoodApplication).database.painPointDao(),
                                    onOpenPainChart = {
                                        navController.navigate("pain_chart")
                                    }
                                )
                            }
                            composable("pain_chart") {
                                // Placeholder - implemented in PainChartScreen.kt
                                PainChartScreen(painDao = (application as MoodApplication).database.painPointDao(), onBack = { navController.popBackStack() })
                            }
                            // Techniques library
                            composable("techniques") {
                                TechniquesLibraryScreen(techniqueDao = (application as MoodApplication).database.techniqueDao()) { id ->
                                    navController.navigate("technique/$id")
                                }
                            }
                            composable("routines") {
                                val routinesVm: RoutinesListViewModel = viewModel(factory = RoutinesListViewModelFactory((application as MoodApplication).database.routineDao()))
                                RoutinesListScreen(viewModel = routinesVm) { id -> navController.navigate("pain_region/$id") }
                            }
                            composable("technique/{id}") { backStack ->
                                val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                                TechniqueDetailScreen(techniqueId = id, techniqueDao = (application as MoodApplication).database.techniqueDao(), onBack = { navController.popBackStack() })
                            }
                            composable("pain_region/{routineId}") { backStack ->
                                val rid = backStack.arguments?.getString("routineId")?.toLongOrNull() ?: 0L
                                PainRegionScreen(routineId = rid, routineDao = (application as MoodApplication).database.routineDao(), techniqueDao = (application as MoodApplication).database.techniqueDao(), onNavigateToTechnique = { tid -> navController.navigate("technique/$tid") })
                            }
                            composable("history") {
                                val moodHistoryViewModel: MoodHistoryViewModel = viewModel(factory = MoodHistoryViewModelFactory((application as MoodApplication).database.moodDao()))
                                MoodHistoryScreen(viewModel = moodHistoryViewModel)
                            }
                            // New specific front/back exercise screens
                            composable(Screen.FrontUpperBody.route) { FrontUpperBodyExerciseScreen(onBack = { navController.popBackStack() }) }
                            composable(Screen.BackUpperBody.route) { BackUpperBodyExerciseScreen(onBack = { navController.popBackStack() }) }
                            composable(Screen.FrontMiddleBody.route) { FrontMiddleBodyExerciseScreen(onBack = { navController.popBackStack() }) }
                            composable(Screen.BackMiddleBody.route) { BackMiddleBodyExerciseScreen(onBack = { navController.popBackStack() }) }
                            composable(Screen.FrontLowerBody.route) { FrontLowerBodyExerciseScreen(onBack = { navController.popBackStack() }) }
                            composable(Screen.BackLowerBody.route) { BackLowerBodyExerciseScreen(onBack = { navController.popBackStack() }) }
                        }
                    }
                }
            }
        }
    }

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.home_title, Icons.Filled.Home)
    // Primary bottom navigation: Inicio, Diario, Progreso
    object Diario : Screen("diario", R.string.mood_tracker_title, Icons.Filled.Create)
    object Diary : Screen("diary", R.string.diary_title, Icons.Filled.Create)
    // LocalHospital comes from the extended material icons pack; to avoid adding a new dependency here
    // we use a built-in icon (Home) as a placeholder. Replace with a different icon if you add
    // the material-icons-extended dependency in build.gradle.
    object Dolor : Screen("dolor", R.string.pain_tracker_title, Icons.Filled.Home)
    object UpperBody : Screen("upper_body", R.string.upper_body_exercises_title, Icons.Filled.Home)
    object MiddleBody : Screen("middle_body", R.string.middle_body_exercises_title, Icons.Filled.Home)
    object LowerBody : Screen("lower_body", R.string.lower_body_exercises_title, Icons.Filled.Home)
    object FrontUpperBody : Screen("front_upper_body", R.string.front_upper_body_title, Icons.Filled.Home)
    object BackUpperBody : Screen("back_upper_body", R.string.back_upper_body_title, Icons.Filled.Home)
    object FrontMiddleBody : Screen("front_middle_body", R.string.front_middle_body_title, Icons.Filled.Home)
    object BackMiddleBody : Screen("back_middle_body", R.string.back_middle_body_title, Icons.Filled.Home)
    object FrontLowerBody : Screen("front_lower_body", R.string.front_lower_body_title, Icons.Filled.Home)
    object BackLowerBody : Screen("back_lower_body", R.string.back_lower_body_title, Icons.Filled.Home)
    object CheckinMood : Screen("checkin_mood", R.string.checkin_mood_title, Icons.Filled.Home)
    object CheckinPain : Screen("checkin_pain", R.string.checkin_pain_title, Icons.Filled.Home)
    // Recommendation screen removed - navigation now goes directly to technique screens from Pain Tracker
    object Respiracion : Screen("respiracion", R.string.breath_title, Icons.Filled.Home)
    object Progress : Screen("progress", R.string.progress_title, Icons.Filled.MoreVert)
    object Ajustes : Screen("ajustes", R.string.settings_title, Icons.Filled.Settings)
    object Exercises : Screen("exercises", R.string.exercises_label, Icons.Filled.Home)
    object Techniques : Screen("techniques", R.string.techniques_label, Icons.Filled.Home)
    object Routines : Screen("routines", R.string.routines_label, Icons.Filled.Home)
}

@Composable
fun BottomBar(navController: NavHostController, items: List<Screen>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    // Simple responsive switch: use NavigationRail for wide screens (>= 600dp), NavigationBar otherwise
    // Use slightly larger icons for better visibility (24-28dp range)
    val iconSizeDp = 28.dp
    val navItemHeight = 56.dp // ensures 48dp touch target + padding

    val colorScheme = MaterialTheme.colorScheme
    if (screenWidthDp >= 600) {
        NavigationRail(containerColor = colorScheme.surface) {
            for (screen in items) {
                NavigationRailItem(
                    selected = currentRoute == screen.route,
                    onClick = { if (currentRoute != screen.route) navController.navigate(screen.route) },
                    icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.labelRes), modifier = Modifier.size(iconSizeDp), tint = if (currentRoute == screen.route) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.6f)) },
                    label = { MText(text = stringResource(id = screen.labelRes), color = if (currentRoute == screen.route) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.height(navItemHeight)
                )
            }
        }
    } else {
        NavigationBar(containerColor = colorScheme.surface, modifier = Modifier.navigationBarsPadding()) {
            for (screen in items) {
                NavigationBarItem(
                    selected = currentRoute == screen.route,
                    onClick = { if (currentRoute != screen.route) navController.navigate(screen.route) },
                    icon = { Icon(screen.icon, contentDescription = stringResource(id = screen.labelRes), modifier = Modifier.size(iconSizeDp), tint = if (currentRoute == screen.route) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.6f)) },
                    label = { MText(text = stringResource(id = screen.labelRes), color = if (currentRoute == screen.route) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.7f)) },
                    modifier = Modifier.height(navItemHeight)
                )
            }
        }
    }
}

// Safe painter loader: load drawable with ResourcesCompat and convert to BitmapPainter, fallback to ColorPainter
@Composable
fun safePainter(resId: Int): androidx.compose.ui.graphics.painter.Painter {
    val ctx = LocalContext.current
    val bmp = remember(resId) {
        try {
            val dr = ResourcesCompat.getDrawable(ctx.resources, resId, ctx.theme)
            dr?.toBitmap()
        } catch (t: Throwable) {
            // try a secondary fallback using BitmapFactory (handles malformed pngs differently)
            try {
                val input = ctx.resources.openRawResource(resId)
                BitmapFactory.decodeStream(input)
            } catch (_: Throwable) {
                null
            }
        }
    }
    return if (bmp != null) BitmapPainter(bmp.asImageBitmap()) else ColorPainter(Color.Gray)
}

// Temporary debug composable - quick list counts and recent rows
@Composable
fun DebugDataScreen(
    moodDao: MoodDao,
    painDao: PainPointDao,
    exerciseDao: ExerciseDao,
    onBack: () -> Unit
) {
    val moods by moodDao.getAllEntries().collectAsState(initial = emptyList())
    val pains by painDao.getAll().collectAsState(initial = emptyList())
    val exercises by exerciseDao.getAll().collectAsState(initial = emptyList())

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        Column(modifier = Modifier.padding(16.dp).padding(innerPadding)) {
            MText(text = stringResource(id = R.string.debug_stored_rows), style = MaterialTheme.typography.titleLarge)
            MText(text = stringResource(id = R.string.debug_mood_entries, moods.size))
            MText(text = stringResource(id = R.string.debug_pain_points, pains.size))
            MText(text = stringResource(id = R.string.debug_exercise_logs, exercises.size))

            MText(text = stringResource(id = R.string.recent_mood_entries))
            for (m in moods.take(5)) {
                MText(text = stringResource(id = R.string.recent_mood_entry_format, m.emoji, m.timestamp.toString()))
            }

            MText(text = stringResource(id = R.string.recent_pain_points, 5))
            for (p in pains.take(5)) {
                MText(text = stringResource(id = R.string.recent_pain_point_format, p.id, p.x.toString(), p.y.toString(), p.timestamp.toString()))
            }

            MText(text = stringResource(id = R.string.recent_exercises))
            for (e in exercises.take(5)) {
                MText(text = stringResource(id = R.string.recent_exercise_entry_format, e.id, e.timestamp.toString()))
            }

            com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.back_button), onClick = onBack, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

