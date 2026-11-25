package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AddExerciseDialog
import com.example.ui.components.AddSupplementDialog
import com.example.ui.components.IsometricHoldBanner
import com.example.ui.components.RestTimerBanner
import com.example.ui.components.SkillTreeDialog
import com.example.ui.theme.LockoutColors
import com.example.viewmodel.LockoutViewModel

private data class NavDestination(
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val Destinations = listOf(
    NavDestination(R.string.nav_overview, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_overview"),
    NavDestination(R.string.nav_training, Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "nav_training"),
    NavDestination(R.string.nav_stack, Icons.Filled.Medication, Icons.Outlined.Medication, "nav_stack"),
    NavDestination(R.string.nav_vault, Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "nav_vault")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockoutAppShell(viewModel: LockoutViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = LockoutColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSkillTree by remember { mutableStateOf(false) }
    var showAddExercise by remember { mutableStateOf(false) }
    var showAddSupplement by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Back press from secondary tabs returns smoothly to Overview
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.canvas)
    ) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = palette.canvas,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = palette.volt,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.5.sp
                                    ),
                                    color = palette.textPrimary
                                )
                                Text(
                                    text = stringResource(R.string.app_subtitle),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = palette.textSecondary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.toggleDaylightMode() },
                            modifier = Modifier.testTag("toggle_daylight_mode_btn")
                        ) {
                            Icon(
                                imageVector = if (state.prefs.isDaylightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = stringResource(R.string.cd_toggle_theme),
                                tint = palette.volt
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = palette.canvas,
                        scrolledContainerColor = palette.surface1
                    )
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = palette.surface2,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        Destinations.forEachIndexed { index, dest ->
                            val selected = selectedTab == index
                            NavigationBarItem(
                                selected = selected,
                                onClick = { selectedTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.titleRes)
                                    )
                                },
                                label = { Text(stringResource(dest.titleRes)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = palette.onVolt,
                                    selectedTextColor = palette.volt,
                                    indicatorColor = palette.volt,
                                    unselectedIconColor = palette.textSecondary,
                                    unselectedTextColor = palette.textSecondary
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                when (selectedTab) {
                    1 -> FloatingActionButton(
                        onClick = { showAddExercise = true },
                        containerColor = palette.volt,
                        contentColor = palette.onVolt,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_add_exercise")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_exercise)
                        )
                    }
                    2 -> FloatingActionButton(
                        onClick = { showAddSupplement = true },
                        containerColor = palette.volt,
                        contentColor = palette.onVolt,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_add_supplement")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.cd_add_supplement)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = palette.surface2,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Destinations.forEachIndexed { index, dest ->
                            val selected = selectedTab == index
                            NavigationRailItem(
                                selected = selected,
                                onClick = { selectedTab = index },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.titleRes)
                                    )
                                },
                                label = { Text(stringResource(dest.titleRes)) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = palette.onVolt,
                                    selectedTextColor = palette.volt,
                                    indicatorColor = palette.volt,
                                    unselectedIconColor = palette.textSecondary,
                                    unselectedTextColor = palette.textSecondary
                                ),
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Live Timestamp Rest Timer Banner (stays visible across tabs)
                        RestTimerBanner(
                            visible = state.timerState.isRestRunning,
                            secondsLeft = state.timerState.restSecondsRemaining,
                            totalSeconds = state.timerState.restTotalSeconds,
                            exerciseName = state.timerState.restExerciseName,
                            onAddSeconds = { viewModel.addRestSeconds(it) },
                            onStop = { viewModel.stopRestTimer() }
                        )
                        if (state.timerState.isRestRunning) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Live Isometric Hold Timer Banner (3-2-1 Audio Cue + Stopwatch)
                        IsometricHoldBanner(
                            visible = state.timerState.isHoldRunning,
                            exerciseName = state.timerState.holdExerciseName,
                            tick = state.timerState.holdTick,
                            onStop = { viewModel.stopIsometricHoldTimer() }
                        )
                        if (state.timerState.isHoldRunning) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (selectedTab) {
                                0 -> DashboardScreen(
                                    state = state,
                                    onAddWater = { viewModel.addWater(it) },
                                    onLogQuickMeal = { name, p, c, f, kcal ->
                                        viewModel.logQuickMeal(name, p, c, f, kcal)
                                    },
                                    onSelectGoal = { viewModel.setNutritionGoal(it) },
                                    onToggleCarbCycle = { viewModel.toggleTrainingDayCarbCycle() },
                                    onOpenTraining = { selectedTab = 1 },
                                    onOpenStack = { selectedTab = 2 }
                                )
                                1 -> TrainingScreen(
                                    state = state,
                                    onSelectProgram = { viewModel.selectProgram(it) },
                                    onSelectDay = { viewModel.selectProgramDay(it) },
                                    onCompleteSet = { viewModel.logSetCompleted(it) },
                                    onAdjustWeight = { ex, delta -> viewModel.adjustExerciseWeight(ex, delta) },
                                    onCycleSetType = { viewModel.cycleSetType(it) },
                                    onSubmitRpe = { ex, rpe -> viewModel.submitRpeCheckIn(ex, rpe) },
                                    onApplySwap = { ex, alt -> viewModel.applySmartSwap(ex, alt) },
                                    onStartHoldTimer = { viewModel.startIsometricHoldTimer(it) },
                                    onStartCustomRest = { secs, name -> viewModel.startRestTimer(secs, name) },
                                    onSetBarbellWeight = { viewModel.setBarbellWeight(it) },
                                    onToggleJointFlag = { viewModel.toggleJointFlag(it) },
                                    onOpenSkillTree = { showSkillTree = true },
                                    onResetDaySets = { viewModel.resetDaySets() }
                                )
                                2 -> SupplementScreen(
                                    state = state,
                                    onToggleSupplement = { viewModel.toggleSupplement(it) },
                                    onRestockSupplement = { viewModel.restockSupplement(it) },
                                    onImportGoalStack = { id, label -> viewModel.importGoalStack(id, label) },
                                    onSetWorkoutHour = { viewModel.setPlannedWorkoutHour(it) }
                                )
                                3 -> VaultScreen(
                                    state = state,
                                    onToggleGearBag = { viewModel.toggleGearBagItem(it) },
                                    onAddSupplementToStack = { viewModel.importVaultSupplementToStack(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSkillTree) {
        SkillTreeDialog(
            milestones = state.skillMilestones,
            onToggleMilestone = { viewModel.toggleSkillMilestone(it) },
            onDismiss = { showSkillTree = false }
        )
    }

    if (showAddExercise) {
        AddExerciseDialog(
            onAdd = { name, cat, muscle, sets, reps, weight, rest, isIso, holdSec ->
                viewModel.addCustomExercise(name, cat, muscle, sets, reps, weight, rest, isIso, holdSec)
            },
            onDismiss = { showAddExercise = false }
        )
    }

    if (showAddSupplement) {
        AddSupplementDialog(
            onAdd = { name, dosage, timing, servings ->
                viewModel.addCustomSupplement(name, dosage, timing, servings)
            },
            onDismiss = { showAddSupplement = false }
        )
    }
}
