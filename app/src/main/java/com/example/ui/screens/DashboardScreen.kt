package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GymProfile
import com.example.data.LockoutSeedData
import com.example.data.MuscleGroup
import com.example.data.NutritionGoal
import com.example.domain.RecoveryStatus
import com.example.ui.components.MuscleHeatmapCard
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ReadinessRingCanvas
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.HabitDisplayItem
import com.example.viewmodel.LockoutUiState

@Composable
fun DashboardScreen(
    state: LockoutUiState,
    onSelectGymProfile: (GymProfile) -> Unit,
    onAddWater: (Int) -> Unit,
    onLogQuickMeal: (String, Int, Int, Int, Int) -> Unit,
    onSelectGoal: (NutritionGoal) -> Unit,
    onToggleCarbCycle: () -> Unit,
    onOpenCalorieCalculator: () -> Unit,
    onOpenCustomMeal: () -> Unit,
    onToggleHabit: (HabitDisplayItem) -> Unit,
    onOpenAddHabit: () -> Unit,
    onUpdateMuscleStatus: (MuscleGroup, RecoveryStatus) -> Unit,
    onOpenTraining: () -> Unit,
    onOpenStack: () -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val nutrition = state.nutrition
    val plan = state.caloriePlan
    val takenSupps = state.supplements.count { it.isTakenToday }
    val lowStockCount = state.supplements.count { it.isLowStock }
    val totalSetsDone = state.activeDayExercises.sumOf { it.completedSets }
    val totalSetsTarget = state.activeDayExercises.sumOf { it.targetSets }
    val habitsDone = state.habits.count { it.isCompletedToday }
    val currentProgram = LockoutSeedData.programs.firstOrNull { it.id == state.prefs.selectedProgramId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = "LOCKOUT COMMAND CENTER",
                title = stringResource(R.string.label_daily_performance),
                trailing = {
                    StatusPill(
                        text = "${state.prefs.trainingStreakDays}D STREAK",
                        color = palette.volt
                    )
                }
            )
        }

        // Multi-Gym Equipment Profile Switcher
        item {
            Column {
                Text(
                    text = "ACTIVE GYM EQUIPMENT PROFILE",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GymProfile.entries.forEach { profile ->
                        FilterChip(
                            selected = state.prefs.gymProfile == profile,
                            onClick = { onSelectGymProfile(profile) },
                            label = { Text(profile.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.volt,
                                selectedLabelColor = palette.onVolt
                            ),
                            modifier = Modifier.testTag("gym_profile_${profile.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 1. Readiness Ring & Active Session Launch Card
        item {
            ObsidianCard(accentBorderColor = palette.volt.copy(alpha = 0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ReadinessRingCanvas(score = state.readinessScore)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = (currentProgram?.title ?: "6-Day PPL").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.volt
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.prefs.selectedDayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPill(
                                text = "HABITS $habitsDone/${state.habits.size}",
                                color = palette.emerald
                            )
                            StatusPill(
                                text = "${state.ownedGearNames.size} GEAR",
                                color = palette.cyan
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onOpenTraining()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("dashboard_start_training_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = palette.onVolt)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (totalSetsDone > 0) "RESUME SESSION ($totalSetsDone/$totalSetsTarget)" else "ENTER GYM FLOOR",
                                color = palette.onVolt,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Quick KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "SETS LOCKED",
                    value = "$totalSetsDone / $totalSetsTarget",
                    accent = palette.volt,
                    icon = Icons.Default.Bolt,
                    onClick = onOpenTraining
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    label = "DAILY STACK",
                    value = "$takenSupps / ${state.supplements.size}",
                    accent = palette.amber,
                    icon = Icons.Default.Medication,
                    onClick = onOpenStack
                )
            }
        }

        // Low-Stock Tub Alert Banner
        if (lowStockCount > 0) {
            item {
                ObsidianCard(
                    accentBorderColor = palette.amber.copy(alpha = 0.6f),
                    elevatedSurface = true,
                    onClick = onOpenStack
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = palette.amber)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "$lowStockCount SUPPLEMENT TUB RUNNING LOW",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.amber
                                )
                                Text(
                                    text = "Under 7 servings remaining. Tap to restock.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive Real Anatomical Muscle Recovery Map
        item {
            MuscleHeatmapCard(
                recoveryMap = state.muscleRecovery,
                onUpdateMuscleStatus = onUpdateMuscleStatus,
                onTrainMuscle = { onOpenTraining() }
            )
        }

        // 4. Precision Nutrition, TDEE Calorie Calculator & Custom Meal Logger
        item {
            ObsidianCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = palette.volt, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nutrition & Calorie Engine",
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )
                    }
                    Surface(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleCarbCycle()
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (nutrition.isTrainingDay) palette.volt.copy(alpha = 0.16f) else palette.surface2,
                        modifier = Modifier.testTag("toggle_carb_cycle_btn")
                    ) {
                        Text(
                            text = if (nutrition.isTrainingDay) "TRAINING DAY" else "REST DAY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (nutrition.isTrainingDay) palette.volt else palette.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BMR / TDEE / BMI Metabolic Telemetry Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(palette.surface2)
                        .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("BMR", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.textSecondary)
                        Text("${plan.bmrKcal} kcal", style = TabularNumberStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MAINTENANCE TDEE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.textSecondary)
                        Text("${plan.tdeeKcal} kcal", style = TabularNumberStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = palette.cyan)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("BMI", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.textSecondary)
                        Text("${plan.bmi}", style = TabularNumberStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold), color = palette.emerald)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calorie Calculator & Log Meal CTA Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenCalorieCalculator,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_calorie_calculator_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = palette.onVolt, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("CALORIE CALC", color = palette.onVolt, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onOpenCustomMeal,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("open_custom_meal_btn"),
                        border = BorderStroke(1.dp, palette.cyan),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = palette.cyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("LOG MEAL", color = palette.cyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Goal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutritionGoal.entries.forEach { goal ->
                        FilterChip(
                            selected = nutrition.goal == goal,
                            onClick = { onSelectGoal(goal) },
                            label = { Text(goal.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.volt.copy(alpha = 0.18f),
                                selectedLabelColor = palette.volt
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calories Row
                val remainingKcal = (nutrition.caloriesTarget - nutrition.caloriesConsumed).coerceAtLeast(0)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Daily Calories", style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                        Text(
                            text = "$remainingKcal kcal remaining today",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.cyan
                        )
                    }
                    Text(
                        text = "${nutrition.caloriesConsumed} / ${nutrition.caloriesTarget} kcal",
                        style = TabularNumberStyle.copy(fontSize = 16.sp),
                        color = palette.volt
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = {
                        (nutrition.caloriesConsumed.toFloat() / nutrition.caloriesTarget.coerceAtLeast(1)).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = palette.volt,
                    trackColor = palette.canvas
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Protein / Carbs / Fats Macro Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacroMiniBar("PROTEIN", nutrition.proteinGrams, nutrition.proteinTarget, palette.emerald, Modifier.weight(1f))
                    MacroMiniBar("CARBS", nutrition.carbsGrams, nutrition.carbsTarget, palette.cyan, Modifier.weight(1f))
                    MacroMiniBar("FATS", nutrition.fatsGrams, nutrition.fatsTarget, palette.amber, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1-Tap Staple Meals
                Text(
                    text = "1-TAP STAPLE FUEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickFuelButton(
                        label = "+25g Whey Shake",
                        sub = "120 kcal",
                        tag = "add_protein_shake_btn",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLogQuickMeal("Whey Isolate Shake", 25, 3, 1, 120)
                        }
                    )
                    QuickFuelButton(
                        label = "+Chicken & Rice",
                        sub = "46P • 580 kcal",
                        tag = "add_chicken_rice_btn",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLogQuickMeal("200g Chicken + Rice", 46, 65, 8, 580)
                        }
                    )
                    QuickFuelButton(
                        label = "+4 Eggs & Oats",
                        sub = "32P • 490 kcal",
                        tag = "add_eggs_oats_btn",
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLogQuickMeal("4 Eggs + Oats", 32, 48, 20, 490)
                        }
                    )
                }
            }
        }

        // 5. Daily Healthy Body Habits Tracker
        item {
            ObsidianCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = palette.emerald, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("WHOLE-BODY HEALTH OS", style = MaterialTheme.typography.labelSmall, color = palette.emerald)
                            Text("Daily Healthy Habits", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                        }
                    }
                    Surface(
                        onClick = onOpenAddHabit,
                        shape = RoundedCornerShape(9.dp),
                        color = palette.surface2,
                        border = BorderStroke(1.dp, palette.emerald.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("add_habit_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add habit", tint = palette.emerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("HABIT", style = MaterialTheme.typography.labelSmall, color = palette.emerald, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$habitsDone of ${state.habits.size} body habits completed today",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                    Text(
                        text = "${if (state.habits.isEmpty()) 0 else (habitsDone * 100 / state.habits.size)}%",
                        style = TabularNumberStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = palette.emerald
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = {
                        if (state.habits.isEmpty()) 0f else (habitsDone.toFloat() / state.habits.size).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = palette.emerald,
                    trackColor = palette.canvas
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.habits.forEach { habitItem ->
                        HabitChecklistRow(
                            item = habitItem,
                            onToggle = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleHabit(habitItem)
                            }
                        )
                    }
                }
            }
        }

        // 6. Hydration & Creatine Boost Card
        item {
            ObsidianCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalDrink, contentDescription = null, tint = palette.cyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Hydration & Electrolytes", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                            Text(
                                text = "${nutrition.waterMl} / ${state.effectiveWaterTargetMl} ml" +
                                    if (state.effectiveWaterTargetMl > nutrition.waterTargetMl) " (+500ml Creatine)" else "",
                                style = TabularNumberStyle.copy(fontSize = 13.sp),
                                color = palette.cyan
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = {
                        (nutrition.waterMl.toFloat() / state.effectiveWaterTargetMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = palette.cyan,
                    trackColor = palette.canvas
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddWater(250)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_water_250_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.cyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.label_add_250ml), color = palette.canvas, fontWeight = FontWeight.ExtraBold)
                    }
                    OutlinedButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddWater(500)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_water_500_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.label_add_500ml), color = palette.cyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitChecklistRow(
    item: HabitDisplayItem,
    onToggle: () -> Unit
) {
    val palette = LockoutColors.current
    val done = item.isCompletedToday
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (done) palette.emerald.copy(alpha = 0.12f) else palette.surface2)
            .border(
                width = 1.dp,
                color = if (done) palette.emerald.copy(alpha = 0.55f) else palette.border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("habit_row_${item.entity.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (done) "Completed" else "Mark completed",
                tint = if (done) palette.emerald else palette.textSecondary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.entity.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = palette.textPrimary
                    )
                    Text(
                        text = item.entity.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = palette.cyan
                    )
                }
                Text(
                    text = item.entity.targetLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${item.totalDaysCompleted}d • ${item.streakDays}🔥",
                style = TabularNumberStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                color = if (done) palette.emerald else palette.textSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                item.last7DaysMask.forEach { hit ->
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (hit) palette.emerald else palette.border)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricTile(
    modifier: Modifier,
    label: String,
    value: String,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val palette = LockoutColors.current
    ObsidianCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = TabularNumberStyle.copy(fontSize = 24.sp),
            color = palette.textPrimary
        )
    }
}

@Composable
private fun MacroMiniBar(
    label: String,
    value: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = palette.textSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${value}g / ${target}g",
            style = TabularNumberStyle.copy(fontSize = 12.sp),
            color = palette.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value.toFloat() / target.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = palette.canvas
        )
    }
}

@Composable
private fun QuickFuelButton(
    label: String,
    sub: String,
    tag: String,
    onClick: () -> Unit
) {
    val palette = LockoutColors.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface2)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag(tag)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = palette.volt, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = palette.textPrimary)
        }
        Text(sub, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
    }
}
