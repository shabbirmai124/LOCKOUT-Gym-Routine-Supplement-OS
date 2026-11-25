package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MuscleGroup
import com.example.domain.RecoveryStatus
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle
import kotlin.math.hypot

private data class AnatomyNode(
    val muscle: MuscleGroup,
    val points: List<Pair<Float, Float>>, // Normalized (x, y) on 2:3 front anatomy plate
    val anatomySubtitle: String
)

private val AnatomyNodes = listOf(
    AnatomyNode(
        muscle = MuscleGroup.SHOULDERS,
        points = listOf(0.34f to 0.22f, 0.66f to 0.22f),
        anatomySubtitle = "Anterior & Lateral Deltoids"
    ),
    AnatomyNode(
        muscle = MuscleGroup.CHEST,
        points = listOf(0.43f to 0.25f, 0.57f to 0.25f),
        anatomySubtitle = "Pectoralis Major & Clavicular Head"
    ),
    AnatomyNode(
        muscle = MuscleGroup.BACK,
        points = listOf(0.38f to 0.31f, 0.62f to 0.31f),
        anatomySubtitle = "Latissimus Dorsi & Serratus"
    ),
    AnatomyNode(
        muscle = MuscleGroup.ARMS,
        points = listOf(0.29f to 0.32f, 0.71f to 0.32f),
        anatomySubtitle = "Biceps Brachii & Brachioradialis"
    ),
    AnatomyNode(
        muscle = MuscleGroup.CORE,
        points = listOf(0.50f to 0.36f),
        anatomySubtitle = "Rectus Abdominis & Obliques"
    ),
    AnatomyNode(
        muscle = MuscleGroup.QUADS,
        points = listOf(0.42f to 0.58f, 0.58f to 0.58f),
        anatomySubtitle = "Rectus Femoris & Vastus Lateralis"
    ),
    AnatomyNode(
        muscle = MuscleGroup.HAMSTRINGS,
        points = listOf(0.41f to 0.77f, 0.59f to 0.77f),
        anatomySubtitle = "Posterior Chain & Gastrocnemius"
    )
)

@Composable
fun MuscleHeatmapCard(
    recoveryMap: Map<MuscleGroup, RecoveryStatus>,
    onUpdateMuscleStatus: (MuscleGroup, RecoveryStatus) -> Unit = { _, _ -> },
    onTrainMuscle: (MuscleGroup) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current
    val haptics = LocalHapticFeedback.current
    var selectedMuscle by remember { mutableStateOf<MuscleGroup?>(MuscleGroup.CHEST) }

    val pulseTransition = rememberInfiniteTransition(label = "anatomy_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    fun statusColor(group: MuscleGroup): Color {
        return when (recoveryMap[group] ?: RecoveryStatus.RECOVERED) {
            RecoveryStatus.RECOVERED -> palette.emerald
            RecoveryStatus.RECOVERING -> palette.amber
            RecoveryStatus.FATIGUED -> palette.crimson
        }
    }

    ObsidianCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BIOMETRIC RECOVERY TELEMETRY",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.cyan
                )
                Text(
                    text = "Interactive Muscle Map",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
            }
            StatusPill(text = "TAP BODY", color = palette.volt)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Real Anatomical Muscle Illustration with Direct-Tap Telemetry Hotspots
            Box(
                modifier = Modifier
                    .width(152.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141820))
                    .border(
                        width = 1.dp,
                        color = selectedMuscle?.let { statusColor(it).copy(alpha = 0.55f) } ?: palette.border,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("anatomy_body_viewport")
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            val nx = tapOffset.x / size.width.toFloat().coerceAtLeast(1f)
                            val ny = tapOffset.y / size.height.toFloat().coerceAtLeast(1f)

                            var nearestMuscle: MuscleGroup? = null
                            var bestDist = Float.MAX_VALUE
                            AnatomyNodes.forEach { node ->
                                node.points.forEach { (px, py) ->
                                    val dist = hypot(nx - px, ny - py)
                                    if (dist < bestDist) {
                                        bestDist = dist
                                        nearestMuscle = node.muscle
                                    }
                                }
                            }
                            if (nearestMuscle != null && bestDist < 0.28f) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedMuscle = nearestMuscle
                            }
                        }
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_anatomy_front_1795602946018),
                    contentDescription = stringResource(R.string.cd_muscle_anatomy),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Precision Biometric Overlay (keeps striations crystal-clear)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    AnatomyNodes.forEach { node ->
                        val color = statusColor(node.muscle)
                        val isSelected = selectedMuscle == node.muscle

                        node.points.forEach { (nx, ny) ->
                            val center = Offset(w * nx, h * ny)
                            val auraRadius = if (isSelected) w * 0.145f * pulseScale else w * 0.085f

                            // Soft translucent biometric aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        color.copy(alpha = if (isSelected) 0.52f else 0.24f),
                                        color.copy(alpha = 0f)
                                    ),
                                    center = center,
                                    radius = auraRadius
                                ),
                                radius = auraRadius,
                                center = center
                            )

                            // Selected muscle target reticle
                            if (isSelected) {
                                drawCircle(
                                    color = color.copy(alpha = 0.9f),
                                    radius = w * 0.068f * (0.9f + (pulseScale - 0.85f) * 0.25f),
                                    center = center,
                                    style = Stroke(width = 1.8.dp.toPx())
                                )
                            }

                            // Crisp core telemetry marker
                            drawCircle(
                                color = Color(0xFF0B0D11),
                                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                center = center
                            )
                            drawCircle(
                                color = color,
                                radius = if (isSelected) 3.4.dp.toPx() else 2.3.dp.toPx(),
                                center = center
                            )
                        }
                    }
                }

                // Bottom scan tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xCC0B0D11))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = palette.volt,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = selectedMuscle?.label?.uppercase() ?: "ANATOMY SCAN",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color(0xFFF8FAFC)
                        )
                    }
                }
            }

            // Interactive Per-Muscle Recovery List
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MuscleGroup.entries.forEach { group ->
                    val status = recoveryMap[group] ?: RecoveryStatus.RECOVERED
                    val dotColor = statusColor(group)
                    val isSelected = selectedMuscle == group
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) dotColor.copy(alpha = 0.14f) else palette.surface2)
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) dotColor else palette.border.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedMuscle = group
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("muscle_row_${group.name.lowercase()}"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = group.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = palette.textPrimary
                            )
                        }
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = dotColor
                        )
                    }
                }
            }
        }

        // Expandable Selected Muscle Biometric Inspector
        AnimatedVisibility(
            visible = selectedMuscle != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            selectedMuscle?.let { muscle ->
                val status = recoveryMap[muscle] ?: RecoveryStatus.RECOVERED
                val accent = statusColor(muscle)
                val node = AnatomyNodes.firstOrNull { it.muscle == muscle }
                val readinessPct = when (status) {
                    RecoveryStatus.RECOVERED -> 100
                    RecoveryStatus.RECOVERING -> 62
                    RecoveryStatus.FATIGUED -> 24
                }
                val guidance = when (status) {
                    RecoveryStatus.RECOVERED -> "Fully primed for heavy progressive overload & top sets."
                    RecoveryStatus.RECOVERING -> "Fibers repairing. Favor light pump work, mobility & protein."
                    RecoveryStatus.FATIGUED -> "High local fatigue. Prioritize 24–48h recovery, hydration & sleep."
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.surface2)
                        .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${muscle.label.uppercase()} • $readinessPct% READY",
                                style = TabularNumberStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.ExtraBold),
                                color = accent
                            )
                            Text(
                                text = node?.anatomySubtitle ?: muscle.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.textSecondary
                            )
                        }
                        Surface(
                            onClick = { onTrainMuscle(muscle) },
                            shape = RoundedCornerShape(8.dp),
                            color = palette.volt,
                            modifier = Modifier.testTag("train_selected_muscle_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = palette.onVolt,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "TRAIN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.onVolt,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = guidance,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "LOG FEELING / OVERRIDE STATUS:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecoveryStatus.entries.forEach { option ->
                            val optColor = when (option) {
                                RecoveryStatus.RECOVERED -> palette.emerald
                                RecoveryStatus.RECOVERING -> palette.amber
                                RecoveryStatus.FATIGUED -> palette.crimson
                            }
                            val active = status == option
                            Surface(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUpdateMuscleStatus(muscle, option)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) optColor.copy(alpha = 0.22f) else palette.canvas,
                                border = BorderStroke(1.dp, if (active) optColor else palette.border),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("muscle_status_${option.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (active) optColor else palette.textSecondary,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendDot("Recovered", palette.emerald)
            LegendDot("Recovering", palette.amber)
            LegendDot("Fatigued", palette.crimson)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    val palette = LockoutColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = palette.textSecondary
        )
    }
}
