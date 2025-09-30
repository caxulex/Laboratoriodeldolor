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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import android.util.LruCache
import android.graphics.Bitmap
import android.graphics.BitmapFactory.Options
import android.graphics.BitmapFactory
import android.content.res.Resources
import androidx.core.view.WindowCompat
import com.example.laboratoriodeldolor.ui.theme.LaboratorioDelDolorTheme
import com.example.laboratoriodeldolor.ui.rehabilitation.RehabilitationScreen
import com.example.laboratoriodeldolor.ui.rehabilitation.CategoryExercisesScreen
import com.example.laboratoriodeldolor.ui.rehabilitation.ExerciseSessionScreen
import com.example.laboratoriodeldolor.ui.screens.InitialConfigurationScreen
import com.example.laboratoriodeldolor.ui.viewmodels.InitialConfigurationViewModel
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
                // Edge-to-edge: make system bars transparent and set icon appearance based on theme
                val view = LocalView.current
                SideEffect {
                    val window = this@MainActivity.window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    @Suppress("DEPRECATION")
                    run {
                        window.statusBarColor = Color.Transparent.toArgb()
                        window.navigationBarColor = Color.Transparent.toArgb()
                    }
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !isDark
                    controller.isAppearanceLightNavigationBars = !isDark
                }
                val navController = rememberNavController()

                // Determine start destination after DB warmup and preference check. Priority: initial config -> onboarding -> daily checkin
                val startDestinationState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
                LaunchedEffect(Unit) {
                    try {
                        (application as? MoodApplication)?.awaitDatabaseReady()
                        val prefs = (application as MoodApplication).preferencesRepository
                        
                        // First check if initial configuration has been completed
                        val hasCompletedInitialConfig = prefs.hasCompletedInitialConfigFlow.first()
                        if (!hasCompletedInitialConfig) {
                            startDestinationState.value = "initial_configuration"
                            return@LaunchedEffect
                        }
                        
                        // Then check if onboarding has been seen
                        val seen = prefs.hasSeenOnboardingFlow.first()
                        if (!seen) {
                            startDestinationState.value = "onboarding"
                        } else {
                            // Show check-in once per day
                            val lastDay = prefs.lastCheckinEpochDayFlow.first()
                            val today = java.time.LocalDate.now(java.time.ZoneId.systemDefault()).toEpochDay()
                            startDestinationState.value = if (lastDay != today) Screen.CheckinMood.route else Screen.Diario.route
                        }
                    } catch (t: Throwable) {
                        // keep startup resilient; fallback to Diario if anything fails
                        startDestinationState.value = Screen.Diario.route
                    }
                }

                val items = listOf(Screen.Home, Screen.Diario, Screen.Progress, Screen.Ajustes)

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Hide the BottomBar during the check-in flow
                val showBottomBar = currentRoute != Screen.CheckinMood.route && currentRoute != Screen.CheckinPain.route

                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
                    // Draw gradient behind the entire app so the bottom bar can appear transparent over it
                    com.example.laboratoriodeldolor.ui.GradientBackground()

                    Scaffold(
                        containerColor = Color.Transparent,
                        bottomBar = { if (showBottomBar) BottomBar(navController = navController, items = items) }
                    ) { innerPadding ->
                        // NavHost sits inside the app-level scaffold; individual screens draw AppScaffold which renders the gradient
                        // Use startDestinationState if available; otherwise default to Diario while we wait to avoid flicker
                        val startDest = startDestinationState.value ?: Screen.Diario.route
                        NavHost(navController = navController, startDestination = startDest, modifier = Modifier.padding(innerPadding)) {
                        // Initial configuration screen for first-time users
                        composable("initial_configuration") {
                            val initialConfigViewModel: InitialConfigurationViewModel = viewModel(
                                factory = InitialConfigurationViewModel.Factory(
                                    (application as MoodApplication).preferencesRepository
                                )
                            )
                            InitialConfigurationScreen(
                                viewModel = initialConfigViewModel,
                                onConfigurationComplete = {
                                    navController.navigate(Screen.Diario.route) {
                                        popUpTo("initial_configuration") { inclusive = true }
                                    }
                                }
                            )
                        }
                        // Check-in flow
                        composable(Screen.CheckinMood.route) {
                            val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModelFactory((application as MoodApplication).moodRepository, (application as MoodApplication).exerciseRepository, (application as MoodApplication).preferencesRepository))
                            MoodCheckInScreen(moodViewModel,
                                onNext = { navController.navigate(Screen.CheckinPain.route) },
                                onSkip = {
                                    // Skip the entire check-in and go to Diario
                                    lifecycleScope.launch {
                                        (application as MoodApplication).preferencesRepository.markCheckedInToday()
                                    }
                                    navController.navigate(Screen.Diario.route) {
                                        popUpTo(Screen.CheckinMood.route) { inclusive = true }
                                    }
                                },
                                onSaved = { emoji ->
                                    // Use the first emoji (most negative) for breath prioritization
                                    if (emoji == MoodOptions.FIVE_LEVEL[0]) { // 😥 (very sad)
                                        moodViewModel.setDashboardPriority(MoodViewModel.DashboardPriority.BREATH)
                                    } else {
                                        moodViewModel.setDashboardPriority(MoodViewModel.DashboardPriority.DIARY)
                                    }
                                }
                            )
                        }
                        composable(Screen.CheckinPain.route) {
                            val painTrackerViewModel: PainTrackerViewModel = viewModel(factory = PainTrackerViewModelFactory((application as MoodApplication).painPointRepository, (application as MoodApplication).painLogRepository))
                            val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModelFactory((application as MoodApplication).moodRepository, (application as MoodApplication).exerciseRepository, (application as MoodApplication).preferencesRepository))
                            PainCheckInScreen(painTrackerViewModel, onFinish = {
                                // user saved pain -> prioritize PAIN module
                                moodViewModel.setDashboardPriority(MoodViewModel.DashboardPriority.PAIN)
                                lifecycleScope.launch {
                                    (application as MoodApplication).preferencesRepository.markCheckedInToday()
                                }
                                navController.navigate(Screen.Diario.route) {
                                    popUpTo(Screen.CheckinMood.route) { inclusive = true }
                                }
                            }, onSkip = {
                                // didn't save pain: fallback to DIARY by default (or mood-based prioritization handled in mood save)
                                lifecycleScope.launch {
                                    (application as MoodApplication).preferencesRepository.markCheckedInToday()
                                }
                                navController.navigate(Screen.Diario.route) {
                                    popUpTo(Screen.CheckinMood.route) { inclusive = true }
                                }
                            })
                        }
                        composable(Screen.Diario.route) {
                            val moodViewModel: MoodViewModel = viewModel(factory = MoodViewModelFactory((application as MoodApplication).moodRepository, (application as MoodApplication).exerciseRepository, (application as MoodApplication).preferencesRepository))
                            DailyMoodScreen(
                                viewModel = moodViewModel,
                                onNavigateToPainTracker = { navController.navigate(Screen.Dolor.route) },
                                onNavigateToSettings = { navController.navigate(Screen.Ajustes.route) },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToBreath = { navController.navigate(Screen.Respiracion.route) },
                                onNavigateToDiary = { navController.navigate("diary") },
                                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                                onOpenPainChart = { navController.navigate("pain_chart") },
                                onOpenMoodChart = { navController.navigate("mood_chart") },
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
                                    onOpenSettings = { navController.navigate(Screen.Ajustes.route) },
                                    onOpenRehabilitation = { navController.navigate(Screen.Rehabilitation.route) }
                                )
                            }
                            composable(Screen.Dolor.route) {
                                val painTrackerViewModel: PainTrackerViewModel = viewModel(factory = PainTrackerViewModelFactory((application as MoodApplication).painPointRepository, (application as MoodApplication).painLogRepository))
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
                                        // Save points using ViewModel, then show recommendations screen instead of auto-navigating
                                        lifecycleScope.launch {
                                            // Clear existing points in ViewModel
                                            painTrackerViewModel.clearPainPoints()

                                            // Group points by view (front/back) and add them properly
                                            val frontPoints = points.filter { it.view == "front" }
                                            val backPoints = points.filter { it.view == "back" }

                                            if (frontPoints.isNotEmpty()) {
                                                painTrackerViewModel.selectView("front")
                                                frontPoints.forEach { lp ->
                                                    painTrackerViewModel.addPainPointNormalized(
                                                        androidx.compose.ui.geometry.Offset(lp.xNorm, lp.yNorm),
                                                        lp.intensity
                                                    )
                                                }
                                            }

                                            if (backPoints.isNotEmpty()) {
                                                painTrackerViewModel.selectView("back")
                                                backPoints.forEach { lp ->
                                                    painTrackerViewModel.addPainPointNormalized(
                                                        androidx.compose.ui.geometry.Offset(lp.xNorm, lp.yNorm),
                                                        lp.intensity
                                                    )
                                                }
                                            }

                                            // Persist and then show recommendation summary
                                            painTrackerViewModel.savePainPoints()
                                            navController.navigate("recommendations")
                                        }
                                    }
                                )
                            }
                            composable(Screen.Ajustes.route) {
                                // Use a distinct local name to avoid shadowing the top-level settingsViewModel
                                val settingsVm: SettingsViewModel = viewModel()
                                SettingsScreen(
                                    viewModel = settingsVm, 
                                    onNavigateToAbout = { navController.navigate("about") },
                                    onNavigateToQClinic = { navController.navigate("qclinic") },
                                    onNavigateToInitialConfiguration = { navController.navigate("initial_configuration") }
                                )
                            }
                            composable("about") {
                                AboutScreen(
                                    onBack = { navController.popBackStack() },
                                    onOpenPrivacy = { navController.navigate("privacy_policy") }
                                )
                            }
                            composable("qclinic") {
                                com.example.laboratoriodeldolor.ui.screens.QClinicScreen(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("history") {
                                val moodHistoryViewModel: MoodHistoryViewModel = viewModel(factory = MoodHistoryViewModelFactory((application as MoodApplication).moodRepository))
                                MoodHistoryScreen(viewModel = moodHistoryViewModel)
                            }
                            composable("diary") {
                                val diaryViewModel: DiaryViewModel = viewModel(factory = DiaryViewModelFactory((application as MoodApplication).moodRepository))
                                DiaryScreen(
                                    viewModel = diaryViewModel,
                                    onBack = { navController.popBackStack() }
                                )
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
                                val breathWorkViewModel: BreathWorkViewModel = viewModel(factory = BreathWorkViewModelFactory((application as MoodApplication).moodRepository))
                                BreathWorkScreen(viewModel = breathWorkViewModel, onInstruction = { id ->
                                    navController.navigate("breath_instruction/$id")
                                })
                            }
                            composable("breath_instruction/{id}") { backStack ->
                                val id = backStack.arguments?.getString("id") ?: ""
                                BreathInstructionScreen(id = id, onBack = { navController.popBackStack() })
                            }
                            composable(Screen.Progress.route) {
                                // Provide MoodRepository and PainPointRepository from application to the chart screen
                                com.example.laboratoriodeldolor.MoodProgressScreen(
                                    moodRepository = (application as MoodApplication).moodRepository,
                                    painPointRepository = (application as MoodApplication).painPointRepository,
                                    onOpenPainChart = {
                                        navController.navigate("pain_chart")
                                    }
                                )
                            }
                            composable("privacy_policy") {
                                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                            }
                            
                            // Rehabilitation navigation
                            composable(Screen.Rehabilitation.route) {
                                RehabilitationScreen(
                                    onNavigateToCategory = { categoryId ->
                                        navController.navigate("rehabilitation/category/$categoryId")
                                    },
                                    onNavigateToExercise = { exerciseId ->
                                        navController.navigate("rehabilitation/exercise/$exerciseId")
                                    }
                                )
                            }
                            
                            composable("rehabilitation/category/{categoryId}") { backStack ->
                                val categoryId = backStack.arguments?.getString("categoryId") ?: ""
                                CategoryExercisesScreen(
                                    categoryId = categoryId,
                                    onNavigateToExercise = { exerciseId ->
                                        navController.navigate("rehabilitation/exercise/$exerciseId")
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            
                            composable("rehabilitation/exercise/{exerciseId}") { backStack ->
                                val exerciseId = backStack.arguments?.getString("exerciseId") ?: ""
                                ExerciseSessionScreen(
                                    exerciseId = exerciseId,
                                    onNavigateBack = { navController.popBackStack() },
                                    onSessionComplete = { navController.popBackStack() }
                                )
                            }
                            composable("pain_chart") {
                                // Placeholder - implemented in PainChartScreen.kt
                                PainChartScreen(painPointRepository = (application as MoodApplication).painPointRepository, onBack = { navController.popBackStack() })
                            }
                            // Techniques library
                            composable("techniques") {
                                TechniquesLibraryScreen(techniqueDao = (application as MoodApplication).database.techniqueDao()) { id ->
                                    navController.navigate("technique/$id")
                                }
                            }
                            composable("routines") {
                                val routinesVm: RoutinesListViewModel = viewModel(factory = RoutinesListViewModelFactory((application as MoodApplication).routineRepository))
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
                                val moodHistoryViewModel: MoodHistoryViewModel = viewModel(factory = MoodHistoryViewModelFactory((application as MoodApplication).moodRepository))
                                MoodHistoryScreen(
                                    viewModel = moodHistoryViewModel,
                                    onOpenMoodChart = { navController.navigate("mood_chart") }
                                )
                            }
                            // Mood chart visualization screen
                            composable("mood_chart") {
                                MoodProgressScreen(
                                    moodRepository = (application as MoodApplication).moodRepository,
                                    painPointRepository = (application as MoodApplication).painPointRepository,
                                    onOpenPainChart = { navController.navigate("pain_chart") }
                                )
                            }
                            // Diary screen for detailed journaling
                            composable("diary") {
                                val diaryViewModel: DiaryViewModel = viewModel(factory = DiaryViewModelFactory((application as MoodApplication).moodRepository))
                                DiaryScreen(
                                    viewModel = diaryViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            // Recommendation summary screen shown after saving pain points
                            composable("recommendations") {
                                val app = (application as MoodApplication)
                                val recVm: RecommendationViewModel = viewModel(
                                    factory = RecommendationViewModelFactory(
                                        app.moodRepository,
                                        app.painPointRepository,
                                        app.routineRepository,
                                        app.routineStepRepository,
                                        app.techniqueRepository
                                    )
                                )
                                RecommendationScreen(
                                    viewModel = recVm, 
                                    onOpenTechnique = { id -> navController.navigate("technique/$id") }, 
                                    onOpenTechniquesLibrary = { navController.navigate("techniques") },
                                    onOpenRoutine = { routineId -> navController.navigate("pain_region/$routineId") }
                                )
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
    }

sealed class Screen(val route: String, val labelRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.home_title, Icons.Filled.Home)
    // Primary bottom navigation: Inicio, Diario, Progreso
    object Diario : Screen("diario", R.string.mood_tracker_title, Icons.Filled.Create)
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
    object Rehabilitation : Screen("rehabilitation", R.string.rehabilitation_title, Icons.Filled.FitnessCenter)
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
        NavigationRail(containerColor = Color.Transparent) {
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
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
        ) {
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

// In-memory bitmap cache keyed by resId + maxDim to avoid repeated decodes across recompositions/screens
private object BitmapCache {
    private val maxKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = maxKb / 16 // use ~6.25% of max heap for image cache
    private val cache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bmp: Bitmap) { cache.put(key, bmp) }
}

// Downsampled decode to cap memory; attempts efficient decode for bitmap resources, with drawable fallback.
private fun decodeSampledBitmap(ctx: android.content.Context, resId: Int, maxDimPx: Int): Bitmap? {
    val key = "$resId:$maxDimPx"
    BitmapCache.get(key)?.let { return it }

    return try {
        // First decode with inJustDecodeBounds=true to check dimensions
        val opts = Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(ctx.resources, resId, opts)
        var inSampleSize = 1
        val outW = opts.outWidth
        val outH = opts.outHeight
        if (outW > 0 && outH > 0) {
            var halfW = outW / 2
            var halfH = outH / 2
            while ((halfW / inSampleSize) > maxDimPx || (halfH / inSampleSize) > maxDimPx) {
                inSampleSize *= 2
            }
        }
        val decodeOpts = Options().apply {
            inJustDecodeBounds = false
            inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.RGB_565 // half memory vs ARGB_8888, sufficient for silhouettes
        }
        val bmp = BitmapFactory.decodeResource(ctx.resources, resId, decodeOpts)
        if (bmp != null) {
            BitmapCache.put(key, bmp)
        }
        bmp
    } catch (_: Throwable) {
        // Fallback: render via Drawable onto a capped bitmap
        try {
            val dr = ResourcesCompat.getDrawable(ctx.resources, resId, ctx.theme)
            if (dr != null) {
                val w = maxDimPx
                val h = maxDimPx
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
                val canvas = android.graphics.Canvas(bmp)
                dr.setBounds(0, 0, w, h)
                dr.draw(canvas)
                BitmapCache.put(key, bmp)
                bmp
            } else null
        } catch (_: Throwable) { null }
    }
}

// Safe painter loader: downsample decode with cache and convert to BitmapPainter; fallback to ColorPainter
@Composable
fun safePainter(resId: Int): androidx.compose.ui.graphics.painter.Painter {
    val ctx = LocalContext.current
    // Target a reasonable maximum dimension in pixels (fits common phone screens without over-allocating)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val maxDimPx = with(density) { 420.dp.roundToPx().coerceAtLeast(512) } // align with container height in UI
    val bmp = remember(resId) { decodeSampledBitmap(ctx, resId, maxDimPx) }
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

