package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.ConditioningMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class HoldTimerTick(
    val countdownBeforeStart: Int,
    val elapsedSeconds: Int,
    val targetSeconds: Int,
    val isComplete: Boolean
)

data class ConditioningTick(
    val mode: ConditioningMode,
    val phaseLabel: String, // "WORK", "REST", "MINUTE", "AMRAP", "ZONE 2"
    val isWorkPhase: Boolean,
    val currentRound: Int,
    val totalRounds: Int,
    val secondsRemainingInPhase: Int,
    val totalSecondsRemaining: Int,
    val isFinished: Boolean
)

class RestTimerEngine(private val context: Context) {

    // Timestamp-based countdown: immune to screen lock or background coroutine suspension
    fun startCountdown(durationSeconds: Int): Flow<Int> = flow {
        val endTimeMillis = System.currentTimeMillis() + (durationSeconds * 1000L)
        while (true) {
            val remainingMs = endTimeMillis - System.currentTimeMillis()
            if (remainingMs <= 0L) {
                emit(0)
                playMusicSafeChime(isFinal = true)
                vibrateDoublePulse()
                break
            }
            emit(((remainingMs + 999L) / 1000L).toInt())
            delay(200L)
        }
    }

    // Audio-guided Isometric Hold Timer: 3-2-1 countdown beeps -> live timestamp hold stopwatch
    fun startIsometricHold(targetSeconds: Int): Flow<HoldTimerTick> = flow {
        val prepEndMillis = System.currentTimeMillis() + 3000L
        var lastBeep = 4
        while (true) {
            val prepRemainingMs = prepEndMillis - System.currentTimeMillis()
            if (prepRemainingMs <= 0L) break
            val sec = ((prepRemainingMs + 999L) / 1000L).toInt()
            if (sec < lastBeep) {
                lastBeep = sec
                playMusicSafeChime(isFinal = false)
            }
            emit(HoldTimerTick(countdownBeforeStart = sec, elapsedSeconds = 0, targetSeconds = targetSeconds, isComplete = false))
            delay(150L)
        }

        playMusicSafeChime(isFinal = true)
        vibrateSingleTap()

        val startMillis = System.currentTimeMillis()
        var targetChimed = false
        while (true) {
            val elapsedSec = ((System.currentTimeMillis() - startMillis) / 1000L).toInt()
            val hitTarget = targetSeconds > 0 && elapsedSec >= targetSeconds
            if (hitTarget && !targetChimed) {
                targetChimed = true
                playMusicSafeChime(isFinal = true)
                vibrateDoublePulse()
            }
            emit(
                HoldTimerTick(
                    countdownBeforeStart = 0,
                    elapsedSeconds = elapsedSec,
                    targetSeconds = targetSeconds,
                    isComplete = hitTarget
                )
            )
            delay(200L)
        }
    }

    // Timestamp-based Conditioning Engine: EMOM, Tabata 20/10, AMRAP, Zone 2
    fun startConditioning(mode: ConditioningMode): Flow<ConditioningTick> = flow {
        val totalDurationSec = when (mode) {
            ConditioningMode.EMOM -> 10 * 60   // 10 rounds x 60s
            ConditioningMode.TABATA -> 8 * 30  // 8 rounds x (20s work + 10s rest)
            ConditioningMode.AMRAP -> 12 * 60  // 12-min countdown
            ConditioningMode.ZONE2 -> 30 * 60  // 30-min steady Zone 2
        }
        val totalRounds = when (mode) {
            ConditioningMode.EMOM -> 10
            ConditioningMode.TABATA -> 8
            ConditioningMode.AMRAP -> 1
            ConditioningMode.ZONE2 -> 1
        }

        val startMillis = System.currentTimeMillis()
        val endMillis = startMillis + (totalDurationSec * 1000L)
        var lastPhaseKey = ""

        playMusicSafeChime(isFinal = true)
        vibrateSingleTap()

        while (true) {
            val now = System.currentTimeMillis()
            val totalRemainingMs = endMillis - now
            if (totalRemainingMs <= 0L) {
                emit(
                    ConditioningTick(
                        mode = mode,
                        phaseLabel = "COMPLETE",
                        isWorkPhase = false,
                        currentRound = totalRounds,
                        totalRounds = totalRounds,
                        secondsRemainingInPhase = 0,
                        totalSecondsRemaining = 0,
                        isFinished = true
                    )
                )
                playMusicSafeChime(isFinal = true)
                vibrateDoublePulse()
                break
            }

            val elapsedSec = ((now - startMillis) / 1000L).toInt()
            val totalRemSec = ((totalRemainingMs + 999L) / 1000L).toInt()

            val tick = when (mode) {
                ConditioningMode.EMOM -> {
                    val round = (elapsedSec / 60) + 1
                    val secInMin = 60 - (elapsedSec % 60)
                    ConditioningTick(mode, "MINUTE $round", true, round.coerceAtMost(totalRounds), totalRounds, secInMin, totalRemSec, false)
                }
                ConditioningMode.TABATA -> {
                    val round = (elapsedSec / 30) + 1
                    val secInBlock = elapsedSec % 30
                    val isWork = secInBlock < 20
                    val phaseRem = if (isWork) 20 - secInBlock else 30 - secInBlock
                    ConditioningTick(mode, if (isWork) "MAX EFFORT" else "REST", isWork, round.coerceAtMost(totalRounds), totalRounds, phaseRem, totalRemSec, false)
                }
                ConditioningMode.AMRAP -> {
                    ConditioningTick(mode, "AMRAP", true, 1, 1, totalRemSec, totalRemSec, false)
                }
                ConditioningMode.ZONE2 -> {
                    ConditioningTick(mode, "ZONE 2 AEROBIC", true, 1, 1, totalRemSec, totalRemSec, false)
                }
            }

            val phaseKey = "${tick.currentRound}_${tick.phaseLabel}"
            if (lastPhaseKey.isNotEmpty() && phaseKey != lastPhaseKey) {
                playMusicSafeChime(isFinal = tick.isWorkPhase)
                vibrateSingleTap()
            }
            lastPhaseKey = phaseKey

            emit(tick)
            delay(200L)
        }
    }

    // Music-Safe Audio Ducking: dips Spotify/YouTube Music gently instead of pausing playback
    private fun playMusicSafeChime(isFinal: Boolean) {
        runCatching {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val toneType = if (isFinal) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_BEEP
            val toneDurationMs = if (isFinal) 350 else 150

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val focusRequest = AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                ).setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                ).build()

                audioManager.requestAudioFocus(focusRequest)
                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                tone.startTone(toneType, toneDurationMs)
                Handler(Looper.getMainLooper()).postDelayed({
                    runCatching {
                        tone.release()
                        audioManager.abandonAudioFocusRequest(focusRequest)
                    }
                }, toneDurationMs + 150L)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                tone.startTone(toneType, toneDurationMs)
                Handler(Looper.getMainLooper()).postDelayed({
                    runCatching {
                        tone.release()
                        @Suppress("DEPRECATION")
                        audioManager.abandonAudioFocus(null)
                    }
                }, toneDurationMs + 150L)
            }
        }
    }

    private fun vibrateDoublePulse() {
        runCatching {
            val vibrator = getVibrator() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 90, 120), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 80, 90, 120), -1)
            }
        }
    }

    private fun vibrateSingleTap() {
        runCatching {
            val vibrator = getVibrator() ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(60)
            }
        }
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
