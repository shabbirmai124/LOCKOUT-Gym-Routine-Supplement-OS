package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.LockoutSeedData
import com.example.data.NutritionGoal
import com.example.ui.components.MuscleHeatmapCard
import com.example.ui.components.ObsidianCard
import com.example.ui.components.ReadinessRingCanvas
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.LockoutUiState

@Composable
fun DashboardScreen(
    state: LockoutUiState,
    onAddWater: (Int) -> Unit,
    onLogQuickMeal: (String, Int, Int, Int, Int) -> Unit,
    onSelectGoal: (NutritionGoal) -> Unit,
    onToggleCarbCycle: () -> Unit,
    onOpenTraining: () -> Unit,
    onOpenStack: () -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val nutrition = state.nutrition
    val takenSupps = state.supplements.count { it.isTakenToday }
    val lowStockCount = state.supplements.count { it.isLowStock }
    val totalSetsDone = state.activeDayExercises.sumOf { it.completedSets }
    val totalSetsTarget = state.activeDayExercises.sumOf { it.targetSets }
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
                                text = "CREATINE ${state.creatineStreakDays}D",
                                color = palette.cyan
                            )
                            StatusPill(
                                text = "${state.ownedGearNames.size} GEAR PACKED",
                                color = palette.emerald
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

        // 3. Precision Nutrition & Dynamic Carb Cycling Card
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
                            text = "Macros & Carb Cycling",
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

                Spacer(modifier = Modifier.height(12.dp))

                // Calories Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Daily Energy", style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
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

                // 1-Tap Staple Meals (under 15 seconds logging)
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

        // 4. Hydration & Creatine Boost Card
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

        // 5. Muscle Recovery Heatmap
        item {
            MuscleHeatmapCard(recoveryMap = state.muscleRecovery)
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
