package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SupplementTiming
import com.example.domain.AlertSeverity
import com.example.ui.components.ObsidianCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.SupplementAlarmDialog
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import com.example.viewmodel.LockoutUiState
import com.example.viewmodel.SupplementDisplayItem
import kotlin.math.roundToInt

@Composable
fun SupplementScreen(
    state: LockoutUiState,
    onToggleSupplement: (SupplementDisplayItem) -> Unit,
    onUpdateAlarm: (SupplementDisplayItem, Boolean, Int, Int) -> Unit,
    onTestAlarm: (SupplementDisplayItem) -> Unit,
    onRestockSupplement: (SupplementDisplayItem) -> Unit,
    onImportGoalStack: (String, String) -> Unit,
    onSetWorkoutHour: (Int) -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val grouped = state.supplements.groupBy { it.entity.timing }
    val takenCount = state.supplements.count { it.isTakenToday }
    val activeAlarmsCount = state.supplements.count { it.entity.reminderEnabled }
    val totalDaysLoggedAll = state.supplements.sumOf { it.totalDaysTaken }

    var alarmTargetItem by remember { mutableStateOf<SupplementDisplayItem?>(null) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled gracefully; alarm still schedules and plays in-app cues */ }

    fun openAlarmDialogFor(item: SupplementDisplayItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        alarmTargetItem = item
    }

    val hour = state.prefs.plannedWorkoutHour
    val formattedHour = when {
        hour == 12 -> "12:00 PM"
        hour > 12 -> "${hour - 12}:00 PM"
        else -> "$hour:00 AM"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 92.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                eyebrow = "DAYS-TAKEN TRACKER • DOSE ALARMS",
                title = stringResource(R.string.label_supplement_stack),
                trailing = {
                    StatusPill(
                        text = "$takenCount / ${state.supplements.size} DONE",
                        color = if (takenCount == state.supplements.size && takenCount > 0) palette.volt else palette.amber
                    )
                }
            )
        }

        // Supplement Compliance & Alarm Summary Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StackKpiTile(
                    label = "TAKEN TODAY",
                    value = "$takenCount/${state.supplements.size}",
                    accent = palette.volt,
                    modifier = Modifier.weight(1f)
                )
                StackKpiTile(
                    label = "ALARMS ARMED",
                    value = "$activeAlarmsCount",
                    accent = palette.amber,
                    modifier = Modifier.weight(1f)
                )
                StackKpiTile(
                    label = "DOSE DAYS",
                    value = "$totalDaysLoggedAll",
                    accent = palette.cyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Dynamic Pre-Workout Shift & Caffeine Cutoff Control
        item {
            ObsidianCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = palette.amber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Planned Gym Session Time", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                    }
                    Text(
                        text = formattedHour,
                        style = TabularNumberStyle.copy(fontSize = 15.sp),
                        color = palette.volt
                    )
                }
                Slider(
                    value = hour.toFloat(),
                    onValueChange = { onSetWorkoutHour(it.roundToInt()) },
                    valueRange = 5f..22f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = palette.volt,
                        activeTrackColor = palette.volt
                    ),
                    modifier = Modifier.testTag("workout_hour_slider")
                )
                Text(
                    text = "Pre-Workout window auto-shifts 30 mins before $formattedHour • Caffeine Sleep Guard active after 3:00 PM.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            }
        }

        // Clinical Interaction & Bio-Timing Guardrails
        if (state.clinicalGuardrails.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = palette.cyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CLINICAL GUARDRAILS & SYNERGY", style = MaterialTheme.typography.labelMedium, color = palette.cyan)
                    }
                    state.clinicalGuardrails.forEach { alert ->
                        val color = when (alert.severity) {
                            AlertSeverity.CRITICAL -> palette.crimson
                            AlertSeverity.WARNING -> palette.amber
                            AlertSeverity.INFO -> palette.cyan
                        }
                        val icon = when (alert.severity) {
                            AlertSeverity.INFO -> Icons.Default.Info
                            else -> Icons.Default.Warning
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(color.copy(alpha = 0.12f))
                                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(alert.title, style = MaterialTheme.typography.labelLarge, color = color)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(alert.detail, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        // 1-Tap Goal Stack Presets
        item {
            Column {
                Text("1-TAP GOAL STACK PRESETS", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StackPresetChip("Smart Budget ROI", "budget", onImportGoalStack)
                    StackPresetChip("Hypertrophy & Pump", "hypertrophy", onImportGoalStack)
                    StackPresetChip("Late-Night Zero-Stim", "late_night", onImportGoalStack)
                }
            }
        }

        // Windowed Supplement Checklist with Per-Supplement Days Taken + Alarm Scheduler
        SupplementTiming.entries.forEach { timing ->
            val list = grouped[timing].orEmpty()
            if (list.isNotEmpty()) {
                item(key = "timing_${timing.name}") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timing.label.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.amber
                        )
                        Text(
                            text = timing.defaultWindowHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.textSecondary
                        )
                    }
                }
                items(list, key = { it.entity.id }) { item ->
                    val sup = item.entity
                    ObsidianCard(
                        accentBorderColor = when {
                            item.isLowStock -> palette.amber.copy(alpha = 0.65f)
                            item.isTakenToday -> palette.volt.copy(alpha = 0.55f)
                            else -> null
                        },
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleSupplement(item)
                        },
                        modifier = Modifier.testTag("supplement_card_${sup.id}")
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
                                Checkbox(
                                    checked = item.isTakenToday,
                                    onCheckedChange = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onToggleSupplement(item)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = palette.volt,
                                        checkmarkColor = palette.onVolt
                                    ),
                                    modifier = Modifier.testTag("supplement_checkbox_${sup.id}")
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = sup.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.textPrimary
                                        )
                                        StatusPill(text = sup.tier, color = palette.cyan)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${sup.dosage}" + if (sup.cycleNote.isNotBlank()) " • ${sup.cycleNote}" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${sup.servingsLeft} servings left • Runs out ${item.runOutDateLabel}",
                                        style = TabularNumberStyle.copy(fontSize = 11.sp),
                                        color = if (item.isLowStock) palette.amber else palette.textSecondary
                                    )
                                }
                            }

                            if (item.isLowStock) {
                                Surface(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onRestockSupplement(item)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = palette.amber.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, palette.amber),
                                    modifier = Modifier.testTag("restock_btn_${sup.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = palette.amber, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.label_restock),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.amber,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Days-Taken Counter + 7-Day Adherence Dots + Daily Dose Alarm Chip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.surface2)
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = palette.cyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${item.totalDaysTaken} ${if (item.totalDaysTaken == 1) "day" else "days"} taken",
                                            style = TabularNumberStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = palette.textPrimary
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = if (item.streakDays > 0) palette.volt else palette.textSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${item.streakDays}d streak",
                                            style = TabularNumberStyle.copy(fontSize = 11.sp),
                                            color = if (item.streakDays > 0) palette.volt else palette.textSecondary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                // 7-Day Adherence Micro Dots
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    item.last7DaysTaken.forEach { taken ->
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (taken) palette.volt else palette.border)
                                        )
                                    }
                                }
                            }

                            // Dose Alarm Button
                            Surface(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    openAlarmDialogFor(item)
                                },
                                shape = RoundedCornerShape(9.dp),
                                color = if (sup.reminderEnabled) palette.amber.copy(alpha = 0.2f) else palette.canvas,
                                border = BorderStroke(
                                    1.dp,
                                    if (sup.reminderEnabled) palette.amber else palette.border
                                ),
                                modifier = Modifier.testTag("supplement_alarm_btn_${sup.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (sup.reminderEnabled) Icons.Default.Alarm else Icons.Default.NotificationsOff,
                                        contentDescription = "Configure dose alarm for ${sup.name}",
                                        tint = if (sup.reminderEnabled) palette.amber else palette.textSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (sup.reminderEnabled) item.reminderTimeLabel else "Set Alarm",
                                        style = TabularNumberStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = if (sup.reminderEnabled) palette.amber else palette.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    alarmTargetItem?.let { target ->
        // Keep latest snapshot from state so live changes reflect immediately
        val freshTarget = state.supplements.firstOrNull { it.entity.id == target.entity.id } ?: target
        SupplementAlarmDialog(
            item = freshTarget,
            onSaveAlarm = { enabled, h, m -> onUpdateAlarm(freshTarget, enabled, h, m) },
            onTestAlarm = { onTestAlarm(freshTarget) },
            onDismiss = { alarmTargetItem = null }
        )
    }
}

@Composable
private fun StackKpiTile(
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surface1)
            .border(1.dp, palette.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = palette.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = TabularNumberStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.ExtraBold),
            color = accent
        )
    }
}

@Composable
private fun StackPresetChip(
    label: String,
    presetId: String,
    onImport: (String, String) -> Unit
) {
    val palette = LockoutColors.current
    Surface(
        onClick = { onImport(presetId, label) },
        shape = RoundedCornerShape(10.dp),
        color = palette.surface2,
        border = BorderStroke(1.dp, palette.border),
        modifier = Modifier.testTag("preset_stack_$presetId")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = palette.textPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
        )
    }
}
