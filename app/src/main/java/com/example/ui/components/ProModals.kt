package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppPrefsEntity
import com.example.data.MuscleGroup
import com.example.domain.BiofeedbackReport
import com.example.domain.LockoutDomainEngine
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle

// ==================== 1. RP-STYLE MESOCYCLE BIOFEEDBACK DIALOG ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiofeedbackDialog(
    onSubmit: (BiofeedbackReport) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var muscle by remember { mutableStateOf(MuscleGroup.QUADS) }
    var pump by remember { mutableIntStateOf(2) }
    var soreness by remember { mutableIntStateOf(1) }
    var jointPain by remember { mutableIntStateOf(0) }
    var rir by remember { mutableIntStateOf(2) }

    val livePreview = remember(muscle, pump, soreness, jointPain, rir) {
        LockoutDomainEngine.evaluateMesocycleFeedback(
            BiofeedbackReport(muscle, pump, soreness, jointPain, rir)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = palette.volt)
                    Spacer(Modifier.width(8.dp))
                    Text("MESOCYCLE BIOFEEDBACK (MEV -> MRV)", style = MaterialTheme.typography.labelMedium, color = palette.volt)
                }

                Text("Target Muscle Group", style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MuscleGroup.entries.forEach { mg ->
                        FilterChip(
                            selected = muscle == mg,
                            onClick = { muscle = mg },
                            label = { Text(mg.label, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.volt.copy(alpha = 0.2f),
                                selectedLabelColor = palette.volt
                            )
                        )
                    }
                }

                RatingSelectorRow("Pump Score (1–3)", listOf(1 to "Low", 2 to "Solid", 3 to "Max"), pump) { pump = it }
                RatingSelectorRow("Soreness (1–3)", listOf(1 to "Fresh", 2 to "Mild", 3 to "Sore"), soreness) { soreness = it }
                RatingSelectorRow("Joint Pain (0–3)", listOf(0 to "None", 1 to "Mild", 2 to "Mod", 3 to "Sharp"), jointPain) { jointPain = it }
                RatingSelectorRow("Avg RIR (0–4)", listOf(0 to "0", 1 to "1", 2 to "2", 3 to "3+"), rir) { rir = it }

                // Live prescription preview
                val previewAccent = if (livePreview.triggerDeload) palette.crimson else if (livePreview.setDelta > 0) palette.volt else palette.cyan
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(previewAccent.copy(alpha = 0.12f))
                        .border(1.dp, previewAccent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (livePreview.triggerDeload) "AUTO-DELOAD TRIGGERED (-50% VOL)" else "PRESCRIPTION: ${if (livePreview.setDelta >= 0) "+${livePreview.setDelta}" else "${livePreview.setDelta}"} SETS",
                        style = MaterialTheme.typography.labelLarge,
                        color = previewAccent
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(livePreview.coachingNote, style = MaterialTheme.typography.bodySmall, color = palette.textPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) {
                        Text("Cancel", color = palette.textSecondary)
                    }
                    Button(
                        onClick = {
                            onSubmit(BiofeedbackReport(muscle, pump, soreness, jointPain, rir))
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("apply_biofeedback_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.volt)
                    ) {
                        Text("APPLY", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSelectorRow(
    label: String,
    options: List<Pair<Int, String>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val palette = LockoutColors.current
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { (value, text) ->
                val active = value == selected
                Surface(
                    onClick = { onSelect(value) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (active) palette.volt else palette.surface2,
                    border = BorderStroke(1.dp, if (active) palette.volt else palette.border)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (active) palette.onVolt else palette.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==================== 2. PRE-LIFT 5-MIN DYNAMIC MOBILITY FLOW ====================
@Composable
fun MobilityFlowDialog(
    dayTitle: String,
    onStartDrillTimer: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    val drills = remember(dayTitle) { LockoutDomainEngine.getPreLiftMobilityFlow(dayTitle) }
    val completed = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = palette.cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("5-MIN PRE-LIFT MOBILITY FLOW", style = MaterialTheme.typography.labelMedium, color = palette.cyan)
                }
                Text("Matched to $dayTitle", style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)

                drills.forEach { drill ->
                    val isDone = drill.name in completed
                    Surface(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (isDone) completed.remove(drill.name) else completed.add(drill.name)
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDone) palette.emerald.copy(alpha = 0.12f) else palette.surface2,
                        border = BorderStroke(1.dp, if (isDone) palette.emerald else palette.border)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isDone) palette.emerald else palette.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(drill.name, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                    Text(drill.prescription, style = MaterialTheme.typography.labelSmall, color = palette.cyan)
                                    Text(drill.cue, style = MaterialTheme.typography.bodySmall, color = palette.textSecondary)
                                }
                            }
                            Surface(
                                onClick = {
                                    onStartDrillTimer(drill.durationSeconds, drill.name)
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = palette.canvas,
                                border = BorderStroke(1.dp, palette.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = palette.volt, modifier = Modifier.size(14.dp))
                                    Text("${drill.durationSeconds}s", style = TabularNumberStyle.copy(fontSize = 11.sp), color = palette.volt)
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.volt)
                ) {
                    Text("READY TO LIFT (${completed.size}/${drills.size})", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ==================== 3. PASTE-TO-ROUTINE IMPORTER ====================
@Composable
fun PasteRoutineDialog(
    targetDayTitle: String,
    onImportText: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var rawText by remember { mutableStateOf("") }
    val previewCount = remember(rawText) {
        LockoutDomainEngine.parsePastedRoutine(rawText, "preview", targetDayTitle).size
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = palette.volt)
                Spacer(Modifier.width(8.dp))
                Text("Paste-to-Routine Importer", color = palette.textPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste any routine from WhatsApp, Reddit, or your coach. Parsed straight into $targetDayTitle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .testTag("paste_routine_input"),
                    placeholder = { Text("Incline Bench Press 4x8 80kg\nWeighted Pull-Ups 4x6 15kg\nHandstand Hold 4x45s") }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (previewCount > 0) "Detected $previewCount exercises" else "1 exercise per line",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (previewCount > 0) palette.volt else palette.textSecondary
                    )
                    TextButton(
                        onClick = {
                            rawText = "Barbell Bench Press 4x6 90kg\nWeighted Ring Dips 3x10 15kg\nCable Lateral Raise 4x15 10kg\nL-Sit Hold 3x30s"
                        }
                    ) {
                        Text("Load Sample", color = palette.cyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        onImportText(rawText)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.volt),
                modifier = Modifier.testTag("confirm_paste_routine_btn")
            ) {
                Text("IMPORT ($previewCount)", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.textSecondary)
            }
        }
    )
}

// ==================== 4. EMERGENCY ICE SAFETY CARD ====================
@Composable
fun EmergencyIceDialog(
    prefs: AppPrefsEntity,
    onSave: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    var contactName by remember(prefs) { mutableStateOf(prefs.iceContactName) }
    var contactPhone by remember(prefs) { mutableStateOf(prefs.iceContactPhone) }
    var bloodType by remember(prefs) { mutableStateOf(prefs.iceBloodType) }
    var allergies by remember(prefs) { mutableStateOf(prefs.iceAllergies) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.surface1,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = palette.crimson)
                Spacer(Modifier.width(8.dp))
                Text("Emergency ICE Card", color = palette.textPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Offline medical & emergency safety details for solo late-night training sessions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it },
                    label = { Text("Emergency Contact Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Emergency Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        label = { Text("Blood Type") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Allergies") },
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(contactName, contactPhone, bloodType, allergies)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = palette.crimson),
                modifier = Modifier.testTag("save_ice_card_btn")
            ) {
                Text("SAVE ICE CARD", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = palette.textSecondary)
            }
        }
    )
}

// ==================== 5. DATA SOVEREIGNTY EXPORT PREVIEW ====================
@Composable
fun DataExportPreviewDialog(
    title: String,
    payload: String,
    mimeType: String,
    onCopied: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LockoutColors.current
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = palette.surface1,
            border = BorderStroke(1.dp, palette.border)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("DATA SOVEREIGNTY EXPORT", style = MaterialTheme.typography.labelMedium, color = palette.volt)
                Text(title, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.canvas)
                        .border(1.dp, palette.border, RoundedCornerShape(12.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = payload,
                        style = TabularNumberStyle.copy(fontSize = 11.sp),
                        color = palette.textSecondary
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText(title, payload))
                            onCopied()
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy", color = palette.textPrimary)
                    }
                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(Intent.EXTRA_SUBJECT, "LOCKOUT Export — $title")
                                putExtra(Intent.EXTRA_TEXT, payload)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Export LOCKOUT Data"))
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.volt)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = palette.onVolt, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share", color = palette.onVolt, fontWeight = FontWeight.ExtraBold)
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close", color = palette.textSecondary)
                }
            }
        }
    }
}
