package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ExerciseEntity
import com.example.data.GymProfile
import com.example.data.LockoutSeedData
import com.example.domain.LockoutDomainEngine
import com.example.domain.SwapAlternative
import com.example.ui.components.BarbellPlateLoaderDialog
import com.example.ui.components.InjuryGuardDialog
import com.example.ui.components.MobilityFlowDialog
import com.example.ui.components.ObsidianCard
import com.example.ui.components.PrShareCardDialog
import com.example.ui.components.RpeOverloadDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.SmartSwapDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.WarmUpRampDialog
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.LockoutUiState

@Composable
fun TrainingScreen(
    state: LockoutUiState,
    onSelectProgram: (String) -> Unit,
    onSelectDay: (String) -> Unit,
    onCompleteSet: (ExerciseEntity) -> Unit,
    onAdjustWeight: (ExerciseEntity, Float) -> Unit,
    onCycleSetType: (ExerciseEntity) -> Unit,
    onSubmitRpe: (ExerciseEntity, Float) -> Unit,
    onApplySwap: (ExerciseEntity, SwapAlternative) -> Unit,
    onAdaptDayToProfile: (GymProfile) -> Unit,
    onStartHoldTimer: (ExerciseEntity) -> Unit,
    onStartCustomRest: (Int, String) -> Unit,
    onSetBarbellWeight: (Float) -> Unit,
    onToggleJointFlag: (String) -> Unit,
    onOpenSkillTree: () -> Unit,
    onOpenPasteImporter: () -> Unit,
    onResetDaySets: () -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val useLbs = state.prefs.useLbs
    val activeProfile = state.prefs.gymProfile
    val activeProgram = LockoutSeedData.programs.firstOrNull { it.id == state.prefs.selectedProgramId }
        ?: LockoutSeedData.programs.first()

    var plateLoaderExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var swapExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var warmUpExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var rpeExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var prExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var showInjuryDialog by remember { mutableStateOf(false) }
    var showMobilityDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = "${activeProfile.shortLabel.uppercase()} • ${activeProgram.daysPerWeek}",
                title = stringResource(R.string.label_training_routine),
                trailing = {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = { showInjuryDialog = true },
                            modifier = Modifier.testTag("injury_guard_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.cd_injury_guard),
                                tint = if (state.flaggedJoints.isNotEmpty()) palette.crimson else palette.textSecondary
                            )
                        }
                        IconButton(
                            onClick = onOpenSkillTree,
                            modifier = Modifier.testTag("open_skill_tree_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = stringResource(R.string.cd_skill_tree),
                                tint = palette.volt
                            )
                        }
                        IconButton(
                            onClick = onResetDaySets,
                            modifier = Modifier.testTag("reset_day_sets_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset day sets",
                                tint = palette.textSecondary
                            )
                        }
                    }
                }
            )
        }

        // Photorealistic Iron & Dumbbell Studio Hero Banner
        item {
            val doneSets = state.activeDayExercises.sumOf { it.completedSets }
            val totalSets = state.activeDayExercises.sumOf { it.targetSets }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(114.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, palette.border, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_training_hero_1795606983764),
                    contentDescription = stringResource(R.string.cd_training_hero),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x550B0D11), Color(0xE00B0D11))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = activeProgram.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.volt
                        )
                        Text(
                            text = state.prefs.selectedDayTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                    StatusPill(
                        text = "$doneSets / $totalSets SETS",
                        color = palette.volt
                    )
                }
            }
        }

        // Program Switcher Row (4 Flagship Programs)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LockoutSeedData.programs.forEach { prog ->
                    val selected = prog.id == state.prefs.selectedProgramId
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectProgram(prog.id) },
                        label = { Text(prog.title, style = MaterialTheme.typography.labelLarge) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = palette.volt,
                            selectedLabelColor = palette.onVolt
                        ),
                        modifier = Modifier.testTag("program_chip_${prog.id}")
                    )
                }
            }
        }

        // Day Selector Row + Quick Mobility & Paste Importer Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeProgram.days.forEach { day ->
                    val selected = day == state.prefs.selectedDayTitle
                    Surface(
                        onClick = { onSelectDay(day) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) palette.surface2 else palette.canvas,
                        border = BorderStroke(1.dp, if (selected) palette.volt else palette.border),
                        modifier = Modifier.testTag("day_chip_$day")
                    ) {
                        Text(
                            text = day.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) palette.volt else palette.textSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Pre-Lift Mobility + Paste-to-Routine + Non-Commercial Profile Auto-Adapter
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UtilityChip(
                    icon = Icons.Default.SelfImprovement,
                    label = "5-Min Mobility Flow",
                    tag = "open_mobility_flow_btn",
                    accent = palette.cyan,
                    onClick = { showMobilityDialog = true }
                )
                UtilityChip(
                    icon = Icons.Default.ContentPaste,
                    label = "Paste Routine",
                    tag = "train_paste_routine_btn",
                    accent = palette.volt,
                    onClick = onOpenPasteImporter
                )
                if (activeProfile != GymProfile.COMMERCIAL) {
                    UtilityChip(
                        icon = Icons.Default.AutoFixHigh,
                        label = "Adapt to ${activeProfile.shortLabel}",
                        tag = "adapt_profile_btn",
                        accent = palette.amber,
                        onClick = { onAdaptDayToProfile(activeProfile) }
                    )
                }
            }
        }

        // Active Joint/Injury Guard Banner
        if (state.flaggedJoints.isNotEmpty()) {
            item {
                ObsidianCard(
                    accentBorderColor = palette.crimson.copy(alpha = 0.65f),
                    elevatedSurface = true,
                    onClick = { showInjuryDialog = true }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = palette.crimson)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "INJURY GUARD ACTIVE: ${state.flaggedJoints.joinToString(", ").uppercase()}",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.crimson
                            )
                            Text(
                                text = "High-stress movements are flagged below. Use 1-Tap Swap for joint-safe alternatives.",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Exercise Cards
        items(state.activeDayExercises, key = { it.id }) { ex ->
            val isDone = ex.completedSets >= ex.targetSets
            val est1Rm = remember(ex.weightKg, ex.targetRepsOrSec) {
                LockoutDomainEngine.estimateOneRepMax(ex.weightKg, ex.targetRepsOrSec)
            }
            val jointWarning = ex.jointRiskArea.split(",")
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() && it in state.flaggedJoints }

            val recommendedGearList = ex.recommendedGear.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            ObsidianCard(
                accentBorderColor = when {
                    jointWarning != null -> palette.crimson.copy(alpha = 0.65f)
                    isDone -> palette.volt.copy(alpha = 0.65f)
                    else -> null
                }
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = ex.category.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.volt
                            )
                            Surface(
                                onClick = { onCycleSetType(ex) },
                                shape = RoundedCornerShape(6.dp),
                                color = palette.surface2,
                                border = BorderStroke(1.dp, palette.border)
                            ) {
                                Text(
                                    text = ex.setType.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = palette.cyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (ex.lastLoggedRpe > 0f) {
                                StatusPill(text = "RPE ${ex.lastLoggedRpe}", color = palette.amber)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = ex.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = palette.textPrimary
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val ghostText = if (ex.previousWeightKg > 0f && !ex.isIsometric) {
                            " • Last: ${LockoutDomainEngine.formatWeight(ex.previousWeightKg, useLbs)}"
                        } else ""
                        Text(
                            text = "${ex.targetSets} sets × ${ex.targetRepsOrSec} • Rest ${ex.restSeconds}s$ghostText",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }

                    if (!ex.isIsometric && est1Rm > 0f) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EST 1RM", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.textSecondary)
                            Text(
                                text = LockoutDomainEngine.formatWeight(est1Rm, useLbs),
                                style = TabularNumberStyle.copy(fontSize = 13.sp),
                                color = palette.cyan
                            )
                        }
                    }
                }

                if (jointWarning != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.crimson.copy(alpha = 0.14f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = palette.crimson, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Caution: Loads flagged $jointWarning — tap Swap for joint-safe alternative.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.crimson
                        )
                    }
                }

                if (ex.coachingCue.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cue: ${ex.coachingCue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
                    )
                }

                if (recommendedGearList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Backpack, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(14.dp))
                        recommendedGearList.forEach { gearName ->
                            val owned = gearName in state.ownedGearNames
                            StatusPill(
                                text = if (owned) "✓ $gearName" else "+ Need $gearName",
                                color = if (owned) palette.emerald else palette.amber
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gym-Floor Ergonomic Action Row: Weight / Hold Timer + 54dp 1-Tap Complete Set Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (ex.isIsometric) {
                        Surface(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStartHoldTimer(ex)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("hold_timer_btn_${ex.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = palette.surface2,
                            border = BorderStroke(1.dp, palette.cyan.copy(alpha = 0.6f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = stringResource(R.string.cd_hold_timer), tint = palette.cyan)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HOLD ${ex.holdTargetSec}s",
                                    style = TabularNumberStyle.copy(fontSize = 14.sp),
                                    color = palette.cyan
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(palette.surface2)
                                .border(1.dp, palette.border, RoundedCornerShape(14.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(54.dp)
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onAdjustWeight(ex, -2.5f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    if (ex.weightKg > 0f) plateLoaderExercise = ex
                                }
                            ) {
                                Text(
                                    text = if (ex.weightKg > 0f) LockoutDomainEngine.formatWeight(ex.weightKg, useLbs) else "BODYWEIGHT",
                                    style = TabularNumberStyle.copy(fontSize = 15.sp),
                                    color = palette.textPrimary
                                )
                                if (ex.weightKg >= 20f) {
                                    Text("TAP PLATES", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.volt)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(54.dp)
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onAdjustWeight(ex, +2.5f)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase weight", tint = palette.textPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCompleteSet(ex)
                        },
                        modifier = Modifier
                            .width(126.dp)
                            .height(54.dp)
                            .testTag("complete_set_btn_${ex.id}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDone) palette.volt else palette.surface2,
                            contentColor = if (isDone) palette.onVolt else palette.volt
                        ),
                        border = BorderStroke(1.5.dp, palette.volt)
                    ) {
                        if (isDone) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = "${ex.completedSets} / ${ex.targetSets}",
                            style = TabularNumberStyle.copy(fontSize = 16.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UtilityChip(
                        icon = Icons.Default.SwapHoriz,
                        label = "Swap",
                        tag = "swap_btn_${ex.id}",
                        onClick = { swapExercise = ex }
                    )
                    if (!ex.isIsometric && ex.weightKg >= 20f) {
                        UtilityChip(
                            icon = Icons.Default.FitnessCenter,
                            label = "Plates",
                            tag = "plates_btn_${ex.id}",
                            onClick = { plateLoaderExercise = ex }
                        )
                    }
                    if (ex.isCompoundBarbell && ex.weightKg >= 40f) {
                        UtilityChip(
                            icon = Icons.Default.LocalFireDepartment,
                            label = "Warm-Up",
                            tag = "warmup_btn_${ex.id}",
                            onClick = { warmUpExercise = ex }
                        )
                    }
                    UtilityChip(
                        icon = Icons.Default.Speed,
                        label = "RPE Overload",
                        tag = "rpe_btn_${ex.id}",
                        onClick = { rpeExercise = ex }
                    )
                    UtilityChip(
                        icon = Icons.Default.Share,
                        label = "PR Card",
                        tag = "pr_card_btn_${ex.id}",
                        onClick = { prExercise = ex }
                    )
                }
            }
        }
    }

    plateLoaderExercise?.let { ex ->
        BarbellPlateLoaderDialog(
            exerciseName = ex.name,
            initialWeightKg = ex.weightKg,
            barWeightKg = state.prefs.barbellWeightKg,
            useLbs = useLbs,
            onSelectBarWeight = onSetBarbellWeight,
            onDismiss = { plateLoaderExercise = null }
        )
    }

    swapExercise?.let { ex ->
        SmartSwapDialog(
            exercise = ex,
            gymProfile = activeProfile,
            useLbs = useLbs,
            onSelectSwap = { alt ->
                onApplySwap(ex, alt)
                swapExercise = null
            },
            onDismiss = { swapExercise = null }
        )
    }

    warmUpExercise?.let { ex ->
        WarmUpRampDialog(
            exercise = ex,
            barWeightKg = state.prefs.barbellWeightKg,
            useLbs = useLbs,
            onStartWarmUpRest = { secs -> onStartCustomRest(secs, "${ex.name} Warm-Up") },
            onDismiss = { warmUpExercise = null }
        )
    }

    rpeExercise?.let { ex ->
        RpeOverloadDialog(
            exercise = ex,
            onSubmitRpe = { rpe ->
                onSubmitRpe(ex, rpe)
                rpeExercise = null
            },
            onDismiss = { rpeExercise = null }
        )
    }

    prExercise?.let { ex ->
        PrShareCardDialog(
            exercise = ex,
            onDismiss = { prExercise = null }
        )
    }

    if (showInjuryDialog) {
        InjuryGuardDialog(
            flaggedJoints = state.flaggedJoints,
            onToggleJoint = onToggleJointFlag,
            onDismiss = { showInjuryDialog = false }
        )
    }

    if (showMobilityDialog) {
        MobilityFlowDialog(
            dayTitle = state.prefs.selectedDayTitle,
            onStartDrillTimer = onStartCustomRest,
            onDismiss = { showMobilityDialog = false }
        )
    }
}

@Composable
private fun UtilityChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String,
    accent: androidx.compose.ui.graphics.Color? = null,
    onClick: () -> Unit
) {
    val palette = LockoutColors.current
    val tint = accent ?: palette.textSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.canvas)
            .border(1.dp, (accent ?: palette.border).copy(alpha = if (accent != null) 0.55f else 1f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = accent ?: palette.textPrimary)
    }
}
