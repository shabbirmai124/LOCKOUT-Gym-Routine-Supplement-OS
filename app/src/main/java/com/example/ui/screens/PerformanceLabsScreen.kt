package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConditioningMode
import com.example.domain.LockoutDomainEngine
import com.example.domain.StrengthTier
import com.example.ui.components.ObsidianCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.LockoutUiState

@Composable
fun PerformanceLabsScreen(
    state: LockoutUiState,
    onStartConditioning: (ConditioningMode) -> Unit,
    onStopConditioning: () -> Unit,
    onUpdateAthleteProfile: (Boolean, Int, Float) -> Unit,
    onOpenBiofeedback: () -> Unit,
    onOpenPasteImporter: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val useLbs = state.prefs.useLbs
    val isMale = state.prefs.isMale
    val bodyWeightKg = state.nutrition.bodyWeightKg

    // Derive Big-3 1RMs from tracked Room lifts with realistic strength defaults
    val derivedSquat = remember(state.allExercises) {
        state.allExercises
            .filter { "squat" in it.name.lowercase() && it.weightKg > 0f }
            .maxOfOrNull { LockoutDomainEngine.estimateOneRepMax(it.weightKg, it.targetRepsOrSec) }
            ?: 140f
    }
    val derivedBench = remember(state.allExercises) {
        state.allExercises
            .filter { ("bench" in it.name.lowercase() || "incline" in it.name.lowercase()) && it.weightKg > 0f }
            .maxOfOrNull { LockoutDomainEngine.estimateOneRepMax(it.weightKg, it.targetRepsOrSec) }
            ?: 105f
    }
    val derivedDeadlift = remember(state.allExercises) {
        state.allExercises
            .filter { ("deadlift" in it.name.lowercase() || "rdl" in it.name.lowercase()) && it.weightKg > 0f }
            .maxOfOrNull { LockoutDomainEngine.estimateOneRepMax(it.weightKg, it.targetRepsOrSec) }
            ?: 165f
    }

    var squat1Rm by remember(derivedSquat) { mutableFloatStateOf(derivedSquat) }
    var bench1Rm by remember(derivedBench) { mutableFloatStateOf(derivedBench) }
    var deadlift1Rm by remember(derivedDeadlift) { mutableFloatStateOf(derivedDeadlift) }
    var selectedMeetLift by remember { mutableStateOf("Squat") }
    var selectedConditioning by remember { mutableStateOf(ConditioningMode.TABATA) }

    val totalKg = squat1Rm + bench1Rm + deadlift1Rm
    val dots = remember(totalKg, bodyWeightKg, isMale) {
        LockoutDomainEngine.calculateDotsScore(totalKg, bodyWeightKg, isMale)
    }
    val wilks = remember(totalKg, bodyWeightKg, isMale) {
        LockoutDomainEngine.calculateWilksScore(totalKg, bodyWeightKg, isMale)
    }
    val squatTier = LockoutDomainEngine.classifyStrengthTier("squat", squat1Rm, bodyWeightKg, isMale)
    val benchTier = LockoutDomainEngine.classifyStrengthTier("bench", bench1Rm, bodyWeightKg, isMale)
    val deadliftTier = LockoutDomainEngine.classifyStrengthTier("deadlift", deadlift1Rm, bodyWeightKg, isMale)

    val selectedMeet1Rm = when (selectedMeetLift) {
        "Bench" -> bench1Rm
        "Deadlift" -> deadlift1Rm
        else -> squat1Rm
    }
    val meetAttempts = remember(selectedMeetLift, selectedMeet1Rm) {
        LockoutDomainEngine.calculateMeetAttempts(selectedMeetLift, selectedMeet1Rm)
    }
    val zone2Range = remember(state.prefs.athleteAge) {
        LockoutDomainEngine.zone2HeartRateRange(state.prefs.athleteAge)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = "STRENGTH BENCHMARKS • CONDITIONING • DATA SOVEREIGNTY",
                title = "Performance Labs",
                trailing = {
                    StatusPill(text = "PRO SUITE", color = palette.volt)
                }
            )
        }

        // 1. DOTS & Wilks Strength Benchmark Card
        item {
            ObsidianCard(accentBorderColor = palette.volt.copy(alpha = 0.55f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = palette.volt, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("DOTS & Wilks Index", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = isMale,
                            onClick = { onUpdateAthleteProfile(true, state.prefs.athleteAge, bodyWeightKg) },
                            label = { Text("M", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = !isMale,
                            onClick = { onUpdateAthleteProfile(false, state.prefs.athleteAge, bodyWeightKg) },
                            label = { Text("F", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LabMetricBox("DOTS SCORE", "$dots", palette.volt, Modifier.weight(1f))
                    LabMetricBox("WILKS", "$wilks", palette.cyan, Modifier.weight(1f))
                    LabMetricBox("TOTAL", LockoutDomainEngine.formatWeight(totalKg, useLbs), palette.emerald, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                // Strength Tier Ranks per Lift with quick +/- adjustments
                LiftTierRow("Squat", squat1Rm, squatTier, useLbs, { squat1Rm = (squat1Rm - 2.5f).coerceAtLeast(20f) }, { squat1Rm += 2.5f })
                Spacer(Modifier.height(8.dp))
                LiftTierRow("Bench", bench1Rm, benchTier, useLbs, { bench1Rm = (bench1Rm - 2.5f).coerceAtLeast(20f) }, { bench1Rm += 2.5f })
                Spacer(Modifier.height(8.dp))
                LiftTierRow("Deadlift", deadlift1Rm, deadliftTier, useLbs, { deadlift1Rm = (deadlift1Rm - 2.5f).coerceAtLeast(20f) }, { deadlift1Rm += 2.5f })

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bodyweight: ${LockoutDomainEngine.formatWeight(bodyWeightKg, useLbs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            onClick = { onUpdateAthleteProfile(isMale, state.prefs.athleteAge, bodyWeightKg - 1f) },
                            shape = RoundedCornerShape(8.dp),
                            color = palette.surface2,
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text("-1 kg", style = MaterialTheme.typography.labelSmall, color = palette.textPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                        Surface(
                            onClick = { onUpdateAthleteProfile(isMale, state.prefs.athleteAge, bodyWeightKg + 1f) },
                            shape = RoundedCornerShape(8.dp),
                            color = palette.surface2,
                            border = BorderStroke(1.dp, palette.border)
                        ) {
                            Text("+1 kg", style = MaterialTheme.typography.labelSmall, color = palette.textPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // 2. Competition Meet Attempt Selector (90% / 96% / 102%)
        item {
            ObsidianCard {
                Text("COMPETITION & PR DAY", style = MaterialTheme.typography.labelMedium, color = palette.cyan)
                Text("Meet Attempt Selector", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Squat", "Bench", "Deadlift").forEach { lift ->
                        FilterChip(
                            selected = selectedMeetLift == lift,
                            onClick = { selectedMeetLift = lift },
                            label = { Text(lift, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.cyan.copy(alpha = 0.2f),
                                selectedLabelColor = palette.cyan
                            )
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttemptCard("OPENER (90%)", LockoutDomainEngine.formatWeight(meetAttempts.openerKg, useLbs), "Confidence Lock", palette.emerald, Modifier.weight(1f))
                    AttemptCard("2ND (96%)", LockoutDomainEngine.formatWeight(meetAttempts.secondKg, useLbs), "Match / Tie PR", palette.amber, Modifier.weight(1f))
                    AttemptCard("3RD (102%)", LockoutDomainEngine.formatWeight(meetAttempts.thirdKg, useLbs), "New All-Time PR", palette.volt, Modifier.weight(1f))
                }
            }
        }

        // 3. Conditioning, HIIT, EMOM, Tabata & Zone 2 Engine
        item {
            ObsidianCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = palette.amber, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Conditioning Timer Engine", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                    }
                    StatusPill(text = "MUSIC-SAFE", color = palette.cyan)
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConditioningMode.entries.forEach { mode ->
                        FilterChip(
                            selected = selectedConditioning == mode,
                            onClick = { selectedConditioning = mode },
                            label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.amber.copy(alpha = 0.2f),
                                selectedLabelColor = palette.amber
                            ),
                            modifier = Modifier.testTag("conditioning_mode_${mode.name.lowercase()}")
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(selectedConditioning.subtitle, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)

                if (selectedConditioning == ConditioningMode.ZONE2) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = palette.crimson, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Target Zone 2 Heart Rate (Age ${state.prefs.athleteAge}): ${zone2Range.first}–${zone2Range.second} BPM",
                            style = TabularNumberStyle.copy(fontSize = 12.sp),
                            color = palette.cyan
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                if (state.timerState.isConditioningRunning) {
                    Button(
                        onClick = onStopConditioning,
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stop_conditioning_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.crimson),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("STOP CONDITIONING", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartConditioning(selectedConditioning)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_conditioning_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = palette.onVolt)
                        Spacer(Modifier.width(6.dp))
                        Text("START ${selectedConditioning.label.uppercase()}", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // 4. RP Mesocycle Biofeedback Volume Auto-Regulator
        item {
            ObsidianCard(onClick = onOpenBiofeedback) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RP-STYLE VOLUME AUTO-REGULATION", style = MaterialTheme.typography.labelSmall, color = palette.volt)
                        Text("Mesocycle Biofeedback & Deload", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text(
                            text = "Rate Pump, Soreness, Joint Pain & RIR to auto-adjust weekly sets or trigger a smart deload.",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.textSecondary
                        )
                    }
                    Icon(Icons.Default.Tune, contentDescription = null, tint = palette.volt)
                }
            }
        }

        // 5. Data Sovereignty & Routine Importer
        item {
            ObsidianCard {
                Text("DATA SOVEREIGNTY", style = MaterialTheme.typography.labelMedium, color = palette.emerald)
                Text("Own Your Training History", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${state.allExercises.size} exercises and ${state.supplements.size} supplements stored locally in offline Room SQLite.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("export_csv_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = palette.volt, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("CSV", color = palette.volt, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onExportJson,
                        modifier = Modifier.weight(1f).height(48.dp).testTag("export_json_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = palette.cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("JSON", color = palette.cyan, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onOpenPasteImporter,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("labs_paste_routine_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.surface2),
                    border = BorderStroke(1.dp, palette.border),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("PASTE-TO-ROUTINE IMPORTER", color = palette.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LabMetricBox(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface2)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = palette.textSecondary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = TabularNumberStyle.copy(fontSize = 16.sp), color = accent)
    }
}

@Composable
private fun LiftTierRow(
    lift: String,
    oneRmKg: Float,
    tier: StrengthTier,
    useLbs: Boolean,
    onDec: () -> Unit,
    onInc: () -> Unit
) {
    val palette = LockoutColors.current
    val tierColor = when (tier) {
        StrengthTier.ELITE -> palette.volt
        StrengthTier.ADVANCED -> palette.cyan
        StrengthTier.INTERMEDIATE -> palette.emerald
        StrengthTier.NOVICE -> palette.amber
        StrengthTier.BEGINNER -> palette.textSecondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(lift, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                StatusPill(text = "${tier.badge} • ${tier.label.uppercase()}", color = tierColor)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Surface(onClick = onDec, shape = RoundedCornerShape(6.dp), color = palette.canvas) {
                Text(" − ", style = TabularNumberStyle.copy(fontSize = 14.sp), color = palette.textPrimary, modifier = Modifier.padding(4.dp))
            }
            Text(
                text = LockoutDomainEngine.formatWeight(oneRmKg, useLbs),
                style = TabularNumberStyle.copy(fontSize = 13.sp),
                color = palette.textPrimary
            )
            Surface(onClick = onInc, shape = RoundedCornerShape(6.dp), color = palette.canvas) {
                Text(" + ", style = TabularNumberStyle.copy(fontSize = 14.sp), color = palette.textPrimary, modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
private fun AttemptCard(
    label: String,
    weightText: String,
    sub: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface2)
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = accent)
        Spacer(Modifier.height(4.dp))
        Text(weightText, style = TabularNumberStyle.copy(fontSize = 16.sp), color = palette.textPrimary)
        Text(sub, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = palette.textSecondary)
    }
}
