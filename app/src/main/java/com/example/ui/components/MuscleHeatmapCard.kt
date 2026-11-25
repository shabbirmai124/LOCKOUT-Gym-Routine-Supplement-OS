package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.MuscleGroup
import com.example.domain.RecoveryStatus
import com.example.ui.theme.LockoutColors

@Composable
fun MuscleHeatmapCard(
    recoveryMap: Map<MuscleGroup, RecoveryStatus>,
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current

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
                    text = "RECOVERY TELEMETRY",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.cyan
                )
                Text(
                    text = "Muscle Recovery Heatmap",
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.textPrimary
                )
            }
            StatusPill(text = "LIVE", color = palette.emerald)
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom Compose Canvas stylized anatomical front/posterior muscle diagram
            Canvas(
                modifier = Modifier
                    .width(126.dp)
                    .height(178.dp)
            ) {
                val w = size.width
                val h = size.height
                val cx = w / 2f

                // Head
                drawCircle(
                    color = palette.border,
                    radius = w * 0.11f,
                    center = Offset(cx, h * 0.08f)
                )

                // Shoulders (Left & Right Delts)
                val shoulderColor = statusColor(MuscleGroup.SHOULDERS)
                drawRoundRect(
                    color = shoulderColor,
                    topLeft = Offset(cx - w * 0.36f, h * 0.17f),
                    size = Size(w * 0.16f, h * 0.11f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = shoulderColor,
                    topLeft = Offset(cx + w * 0.20f, h * 0.17f),
                    size = Size(w * 0.16f, h * 0.11f),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                // Chest (Pecs)
                val chestColor = statusColor(MuscleGroup.CHEST)
                drawRoundRect(
                    color = chestColor,
                    topLeft = Offset(cx - w * 0.18f, h * 0.18f),
                    size = Size(w * 0.36f, h * 0.12f),
                    cornerRadius = CornerRadius(14f, 14f)
                )

                // Back / Lats flare edges
                val backColor = statusColor(MuscleGroup.BACK)
                drawRoundRect(
                    color = backColor.copy(alpha = 0.85f),
                    topLeft = Offset(cx - w * 0.24f, h * 0.28f),
                    size = Size(w * 0.07f, h * 0.15f),
                    cornerRadius = CornerRadius(10f, 10f)
                )
                drawRoundRect(
                    color = backColor.copy(alpha = 0.85f),
                    topLeft = Offset(cx + w * 0.17f, h * 0.28f),
                    size = Size(w * 0.07f, h * 0.15f),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Arms (Biceps/Triceps)
                val armsColor = statusColor(MuscleGroup.ARMS)
                drawRoundRect(
                    color = armsColor,
                    topLeft = Offset(cx - w * 0.41f, h * 0.29f),
                    size = Size(w * 0.12f, h * 0.22f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = armsColor,
                    topLeft = Offset(cx + w * 0.29f, h * 0.29f),
                    size = Size(w * 0.12f, h * 0.22f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Core (Abs)
                val coreColor = statusColor(MuscleGroup.CORE)
                drawRoundRect(
                    color = coreColor,
                    topLeft = Offset(cx - w * 0.14f, h * 0.32f),
                    size = Size(w * 0.28f, h * 0.19f),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Quads
                val quadsColor = statusColor(MuscleGroup.QUADS)
                drawRoundRect(
                    color = quadsColor,
                    topLeft = Offset(cx - w * 0.21f, h * 0.54f),
                    size = Size(w * 0.18f, h * 0.23f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                drawRoundRect(
                    color = quadsColor,
                    topLeft = Offset(cx + w * 0.03f, h * 0.54f),
                    size = Size(w * 0.18f, h * 0.23f),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                // Hamstrings / Lower Legs
                val hamsColor = statusColor(MuscleGroup.HAMSTRINGS)
                drawRoundRect(
                    color = hamsColor,
                    topLeft = Offset(cx - w * 0.18f, h * 0.79f),
                    size = Size(w * 0.13f, h * 0.18f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                drawRoundRect(
                    color = hamsColor,
                    topLeft = Offset(cx + w * 0.05f, h * 0.79f),
                    size = Size(w * 0.13f, h * 0.18f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }

            // Per-muscle recovery breakdown
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                MuscleGroup.entries.forEach { group ->
                    val status = recoveryMap[group] ?: RecoveryStatus.RECOVERED
                    val dotColor = statusColor(group)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.surface2)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
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
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.textPrimary
                            )
                        }
                        Text(
                            text = status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = dotColor
                        )
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
