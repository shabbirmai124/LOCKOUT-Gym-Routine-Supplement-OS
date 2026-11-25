package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.ExerciseEntity
import com.example.data.GymProfile
import com.example.data.MuscleGroup
import com.example.data.SkillMilestoneEntity
import com.example.data.SupplementTiming
import com.example.domain.LockoutDomainEngine
import com.example.domain.SwapAlternative
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianCanvas
import com.example.ui.theme.ObsidianSurface1
import com.example.ui.theme.TabularNumberStyle
import kotlin.math.roundToInt

// ==================== 1. VISUAL BARBELL PLATE LOADER ====================
@Composable
fun BarbellPlateLoaderDialog(
    exerciseName: String,
    initialWeightKg: Float,
    barWeightKg: Float,
    useLbs: Boolean = false,
    onSelectBarWeight: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var weight by remember { mutableFloatStateOf(initialWeightKg.coerceAtLeast(barWeightKg)) }
    val plan = remember(weight, barWeightKg) {
        LockoutDomainEngine.calculateBarbellPlates(weight, barWeightKg)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("VISUAL BARBELL LOADER", style = MaterialTheme.typography.labelMedium, color = palette.volt)
                Text(exerciseName, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                // Bar weight selector (20kg vs 15kg)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(20f to "20 kg Men's Bar", 15f to "15 kg Women's Bar").forEach { (kg, label) ->
                        FilterChip(
                            selected = barWeightKg == kg,
                            onClick = { onSelectBarWeight(kg) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.volt.copy(alpha = 0.2f),
                                selectedLabelColor = palette.volt
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Target weight adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { weight = (weight - 2.5f).coerceAtLeast(barWeightKg) }) {
                        Text("-2.5", color = palette.textPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LockoutDomainEngine.formatWeight(plan.achievedWeightKg, useLbs),
                            style = TabularNumberStyle.copy(fontSize = 28.sp),
                            color = palette.volt
                        )
                        Text(
                            text = "TOTAL LOAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                    OutlinedButton(onClick = { weight += 2.5f }) {
                        Text("+2.5", color = palette.textPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual Barbell Sleeve Representation (One Side)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(palette.canvas)
                        .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Barbell Shaft
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF64748B))
                    )
                    // Collar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFCBD5E1))
                        )
                        if (plan.isBarOnly) {
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "EMPTY BAR ONLY",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary
                            )
                        } else {
                            plan.platesPerSide.take(8).forEach { plate ->
                                val plateHeight = when {
                                    plate.weightKg >= 20f -> 72.dp
                                    plate.weightKg >= 15f -> 64.dp
                                    plate.weightKg >= 10f -> 56.dp
                                    plate.weightKg >= 5f -> 46.dp
                                    else -> 36.dp
                                }
                                val plateWidth = if (plate.weightKg >= 15f) 22.dp else 18.dp
                                Box(
                                    modifier = Modifier
                                        .width(plateWidth)
                                        .height(plateHeight)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(plate.colorHex)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = plate.label,
                                        style = TabularNumberStyle.copy(fontSize = 8.sp),
                                        color = if (plate.weightKg == 5f || plate.weightKg == 15f || plate.weightKg == 1.25f) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Exact text breakdown per side
                val groupedText = if (plan.isBarOnly) {
                    "No plates required — lift the ${barWeightKg.toInt()} kg bar."
                } else {
                    plan.platesPerSide
                        .groupBy { it.label }
                        .entries
                        .joinToString("  +  ") { "${it.value.size}×${it.key} kg" } + " per side"
                }

                Text(
                    text = "LOAD EACH SIDE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = groupedText,
                    style = MaterialTheme.typography.titleSmall,
                    color = palette.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.volt)
                ) {
                    Text("LOCKED & LOADED", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ==================== 2. 1-TAP SMART EQUIPMENT SWAPPER ====================
@Composable
fun SmartSwapDialog(
    exercise: ExerciseEntity,
    gymProfile: GymProfile = GymProfile.COMMERCIAL,
    useLbs: Boolean = false,
    onSelectSwap: (SwapAlternative) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    val alternatives = remember(exercise.id, exercise.name, exercise.weightKg, gymProfile) {
        LockoutDomainEngine.getSmartSwapAlternatives(exercise, gymProfile)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = palette.volt)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SWAP (${gymProfile.shortLabel.uppercase()})", style = MaterialTheme.typography.labelMedium, color = palette.volt)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Matched to ${gymProfile.label}. Tap to swap ${exercise.name} with auto-converted working weight.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    alternatives.forEach { alt ->
                        Surface(
                            onClick = { onSelectSwap(alt) },
                            shape = RoundedCornerShape(14.dp),
                            color = palette.surface2,
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(alt.name, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                    Text(alt.note, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                StatusPill(
                                    text = LockoutDomainEngine.formatWeight(alt.convertedWeightKg, useLbs),
                                    color = palette.volt
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(stringResource(R.string.label_cancel), color = palette.textSecondary)
                }
            }
        }
    }
}

// ==================== 3. AUTOMATIC WARM-UP RAMP DIALOG ====================
@Composable
fun WarmUpRampDialog(
    exercise: ExerciseEntity,
    barWeightKg: Float,
    useLbs: Boolean = false,
    onStartWarmUpRest: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    val ramp = remember(exercise.weightKg, barWeightKg) {
        LockoutDomainEngine.generateWarmUpRamp(exercise.weightKg, barWeightKg)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("AUTO WARM-UP RAMP", style = MaterialTheme.typography.labelMedium, color = palette.amber)
                Text(exercise.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Text(
                    text = "Working target: ${LockoutDomainEngine.formatWeight(exercise.weightKg, useLbs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ramp.forEach { set ->
                        Surface(
                            onClick = {
                                onStartWarmUpRest(set.restSeconds)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = palette.surface2,
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "SET ${set.stepNumber} • ${set.percentage}% (${set.purpose})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.amber
                                    )
                                    Text(
                                        text = "${LockoutDomainEngine.formatWeight(set.weightKg, useLbs)} × ${set.reps} reps",
                                        style = TabularNumberStyle.copy(fontSize = 17.sp),
                                        color = palette.textPrimary
                                    )
                                }
                                StatusPill(text = "${set.restSeconds}s rest", color = palette.cyan)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("DONE", color = palette.volt, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== 4. AUTO-OVERLOAD RPE CHECK-IN DIALOG ====================
@Composable
fun RpeOverloadDialog(
    exercise: ExerciseEntity,
    onSubmitRpe: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var rpe by remember { mutableFloatStateOf(exercise.targetRpe.coerceIn(6f, 10f)) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("AUTO-PROGRESSIVE OVERLOAD", style = MaterialTheme.typography.labelMedium, color = palette.volt)
                Text(exercise.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rate session exertion (RPE). Under 8.0 triggers automatic +2.5 kg or +5s hold progression.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RPE ${"%.1f".format(rpe)}",
                    style = TabularNumberStyle.copy(fontSize = 30.sp),
                    color = when {
                        rpe < 8f -> palette.volt
                        rpe >= 9.5f -> palette.crimson
                        else -> palette.cyan
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Slider(
                    value = rpe,
                    onValueChange = { rpe = (it * 2f).roundToInt() / 2f },
                    valueRange = 6f..10f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = palette.volt,
                        activeTrackColor = palette.volt
                    )
                )

                val previewHint = when {
                    rpe < 8f -> if (exercise.isIsometric) "Auto-Upgrade: +5s hold target next session" else "Auto-Upgrade: +2.5 kg load next session"
                    rpe >= 9.5f -> "Peak Effort: Hold current load & prioritize sleep/recovery"
                    else -> "Sweet Spot: Maintain load and push +1 rep next week"
                }
                Text(previewHint, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(stringResource(R.string.label_cancel), color = palette.textSecondary)
                    }
                    Button(
                        onClick = { onSubmitRpe(rpe) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.volt)
                    ) {
                        Text("APPLY", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ==================== 5. GYMNASTIC SKILL TREE MODAL ====================
@Composable
fun SkillTreeDialog(
    milestones: List<SkillMilestoneEntity>,
    onToggleMilestone: (SkillMilestoneEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val grouped = remember(milestones) { milestones.groupBy { it.treeName } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("GYMNASTICS SKILL TREE", style = MaterialTheme.typography.labelMedium, color = palette.volt)
                Text("Unlock Progression Milestones", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    grouped.forEach { (treeName, steps) ->
                        item(key = "tree_$treeName") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(palette.surface2)
                                    .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val unlocked = steps.count { it.isUnlocked }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(treeName.uppercase(), style = MaterialTheme.typography.labelLarge, color = palette.volt)
                                    Text("$unlocked / ${steps.size}", style = TabularNumberStyle.copy(fontSize = 12.sp), color = palette.textSecondary)
                                }
                                steps.forEach { step ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onToggleMilestone(step)
                                            }
                                            .background(if (step.isUnlocked) palette.volt.copy(alpha = 0.12f) else palette.canvas)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (step.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (step.isUnlocked) palette.volt else palette.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(step.title, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                            Text(step.requirement, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.volt)
                ) {
                    Text("CLOSE SKILL TREE", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ==================== 6. JOINT / INJURY GUARDRAIL MODAL ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InjuryGuardDialog(
    flaggedJoints: Set<String>,
    onToggleJoint: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    val areas = listOf("Shoulders", "Knees", "Lower Back", "Wrists", "Elbows")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = palette.crimson)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Joint & Injury Guardrails", color = palette.textPrimary)
            }
        },
        text = {
            Column {
                Text(
                    text = "Flag any tight or irritated joint. LOCKOUT highlights high-risk lifts and suggests joint-friendly swaps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    areas.forEach { area ->
                        val flagged = area in flaggedJoints
                        FilterChip(
                            selected = flagged,
                            onClick = { onToggleJoint(area) },
                            label = { Text(area) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.crimson.copy(alpha = 0.2f),
                                selectedLabelColor = palette.crimson
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("DONE", color = palette.volt, fontWeight = FontWeight.Bold)
            }
        }
    )
}

// ==================== 7. DARK-LUXURY PR SHARE CARD PREVIEW ====================
@Composable
fun PrShareCardDialog(
    exercise: ExerciseEntity,
    onDismiss: () -> Unit
) {
    val est1Rm = remember(exercise.weightKg, exercise.targetRepsOrSec) {
        LockoutDomainEngine.estimateOneRepMax(exercise.weightKg, exercise.targetRepsOrSec)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ObsidianCanvas,
            border = BorderStroke(1.5.dp, Color(0xFFC6FF00).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(ObsidianSurface1, ObsidianCanvas)
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFC6FF00), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW LOCKOUT PR",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFC6FF00),
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (exercise.isIsometric) exercise.targetRepsOrSec.uppercase()
                    else LockoutDomainEngine.formatKg(exercise.weightKg).uppercase(),
                    style = TabularNumberStyle.copy(fontSize = 42.sp),
                    color = Color(0xFFF8FAFC)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.name.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF94A3B8)
                )
                if (!exercise.isIsometric && est1Rm > 0f) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianBorder.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "EST. 1RM: ${LockoutDomainEngine.formatKg(est1Rm)} • ${exercise.targetSets}×${exercise.targetRepsOrSec}",
                            style = TabularNumberStyle.copy(fontSize = 12.sp),
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "LOCKOUT  •  FINISH EVERY REP.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6FF00))
                ) {
                    Text("DONE", color = ObsidianCanvas, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ==================== 8. ADD CUSTOM EXERCISE DIALOG ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddExerciseDialog(
    onAdd: (String, String, MuscleGroup, Int, String, Float, Int, Boolean, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var name by remember { mutableStateOf("") }
    var setsText by remember { mutableStateOf("4") }
    var repsText by remember { mutableStateOf("8-10 reps") }
    var weightText by remember { mutableStateOf("60") }
    var restText by remember { mutableStateOf("90") }
    var selectedMuscle by remember { mutableStateOf(MuscleGroup.CHEST) }
    var isIsometric by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = { Text("Add Custom Exercise", color = palette.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    placeholder = { Text("e.g., Weighted Ring Dips") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_exercise_name")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Isometric Hold (Timed)", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                    Switch(checked = isIsometric, onCheckedChange = {
                        isIsometric = it
                        repsText = if (it) "30s hold" else "8-10 reps"
                    })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Sets") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { repsText = it },
                        label = { Text(if (isIsometric) "Hold Target" else "Reps") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Load (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = restText,
                        onValueChange = { restText = it.filter { c -> c.isDigit() } },
                        label = { Text("Rest (s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MuscleGroup.entries.forEach { muscle ->
                        FilterChip(
                            selected = selectedMuscle == muscle,
                            onClick = { selectedMuscle = muscle },
                            label = { Text(muscle.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val sets = setsText.toIntOrNull() ?: 4
                        val weight = weightText.toFloatOrNull() ?: 0f
                        val rest = restText.toIntOrNull() ?: 90
                        val holdSec = Regex("\\d+").find(repsText)?.value?.toIntOrNull() ?: 30
                        onAdd(name, selectedMuscle.label, selectedMuscle, sets, repsText, weight, rest, isIsometric, holdSec)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("confirm_add_exercise")
            ) {
                Text(stringResource(R.string.label_save), color = palette.onVolt, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel), color = palette.textSecondary)
            }
        }
    )
}

// ==================== 9. ADD CUSTOM SUPPLEMENT DIALOG ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSupplementDialog(
    onAdd: (String, String, SupplementTiming, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var servingsText by remember { mutableStateOf("30") }
    var timing by remember { mutableStateOf(SupplementTiming.POST_WORKOUT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = { Text("Add to Supplement Stack", color = palette.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Supplement Name") },
                    placeholder = { Text("e.g., Zinc Picolinate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_supplement_name")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage") },
                        placeholder = { Text("30 mg") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                    OutlinedTextField(
                        value = servingsText,
                        onValueChange = { servingsText = it.filter { c -> c.isDigit() } },
                        label = { Text("Servings") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }
                Text("Timing Window", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SupplementTiming.entries.forEach { window ->
                        FilterChip(
                            selected = timing == window,
                            onClick = { timing = window },
                            label = { Text(window.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, dosage, timing, servingsText.toIntOrNull() ?: 30)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("confirm_add_supplement")
            ) {
                Text(stringResource(R.string.label_save), color = palette.onVolt, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.label_cancel), color = palette.textSecondary)
            }
        }
    )
}
