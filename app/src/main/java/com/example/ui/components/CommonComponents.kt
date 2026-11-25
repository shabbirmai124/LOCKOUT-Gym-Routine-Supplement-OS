package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.HoldTimerTick
import com.example.ui.theme.LockoutColors
import com.example.ui.theme.TabularNumberStyle

@Composable
fun ObsidianCard(
    modifier: Modifier = Modifier,
    accentBorderColor: Color? = null,
    elevatedSurface: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val palette = LockoutColors.current
    val bg = if (elevatedSurface) palette.surface2 else palette.surface1
    val border = BorderStroke(1.dp, accentBorderColor ?: palette.border)
    val shape = RoundedCornerShape(16.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = bg,
            border = border
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = bg,
            border = border
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val palette = LockoutColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = palette.volt
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.textPrimary
            )
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RestTimerBanner(
    visible: Boolean,
    secondsLeft: Int,
    totalSeconds: Int,
    exerciseName: String,
    onAddSeconds: (Int) -> Unit,
    onStop: () -> Unit
) {
    val palette = LockoutColors.current
    val progress = if (totalSeconds > 0) (secondsLeft.toFloat() / totalSeconds).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = 300f),
        label = "rest_progress"
    )
    val mins = secondsLeft / 60
    val secs = secondsLeft % 60
    val formatted = if (mins > 0) "%d:%02d".format(mins, secs) else "${secs}s"

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("rest_timer_banner"),
            shape = RoundedCornerShape(16.dp),
            color = palette.surface2,
            border = BorderStroke(1.dp, palette.amber.copy(alpha = 0.65f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(palette.amber.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = stringResource(R.string.cd_rest_timer),
                                tint = palette.amber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "REST • ${exerciseName.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.amber,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatted,
                                    style = TabularNumberStyle.copy(fontSize = 22.sp),
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = palette.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Music-Safe",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { onAddSeconds(15) },
                            modifier = Modifier.testTag("rest_timer_plus_15")
                        ) {
                            Text("+15s", color = palette.textPrimary, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onStop,
                            modifier = Modifier.testTag("rest_timer_skip")
                        ) {
                            Text(
                                text = stringResource(R.string.label_skip),
                                color = palette.amber,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.amber,
                    trackColor = palette.canvas
                )
            }
        }
    }
}

@Composable
fun IsometricHoldBanner(
    visible: Boolean,
    exerciseName: String,
    tick: HoldTimerTick?,
    onStop: () -> Unit
) {
    val palette = LockoutColors.current
    AnimatedVisibility(
        visible = visible && tick != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        if (tick != null) {
            val isPrep = tick.countdownBeforeStart > 0
            val accent = when {
                isPrep -> palette.amber
                tick.isComplete -> palette.volt
                else -> palette.cyan
            }
            val progress = if (!isPrep && tick.targetSeconds > 0) {
                (tick.elapsedSeconds.toFloat() / tick.targetSeconds).coerceIn(0f, 1f)
            } else 0f

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("isometric_hold_banner"),
                shape = RoundedCornerShape(16.dp),
                color = palette.surface2,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPrep) "GET SET • AUDIO CUE" else if (tick.isComplete) "TARGET LOCKED • HOLD!" else "ISOMETRIC HOLD • $exerciseName",
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPrep) "STARTING IN ${tick.countdownBeforeStart}..."
                                else "${tick.elapsedSeconds}s / ${tick.targetSeconds}s",
                                style = TabularNumberStyle.copy(fontSize = 24.sp),
                                color = palette.textPrimary
                            )
                        }
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier.testTag("hold_timer_stop")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_close),
                                tint = palette.textPrimary
                            )
                        }
                    }
                    if (!isPrep) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accent,
                            trackColor = palette.canvas
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReadinessRingCanvas(
    score: Int,
    ringSize: Dp = 92.dp,
    modifier: Modifier = Modifier
) {
    val palette = LockoutColors.current
    val sweepFraction by animateFloatAsState(
        targetValue = (score / 100f).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = 180f),
        label = "readiness_sweep"
    )
    val ringColor = when {
        score >= 80 -> palette.volt
        score >= 60 -> palette.cyan
        else -> palette.amber
    }

    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokePx = 9.dp.toPx()
            drawArc(
                color = palette.border,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(palette.cyan, ringColor, ringColor)
                ),
                startAngle = -90f,
                sweepAngle = 360f * sweepFraction,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                style = TabularNumberStyle.copy(fontSize = 24.sp),
                color = palette.textPrimary
            )
            Text(
                text = "READY",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = palette.textSecondary
            )
        }
    }
}
