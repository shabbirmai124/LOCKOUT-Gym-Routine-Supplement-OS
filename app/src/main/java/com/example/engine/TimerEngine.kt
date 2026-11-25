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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class HoldTimerTick(
    val countdownBeforeStart: Int, // 3, 2, 1, then 0 when active
    val elapsedSeconds: Int,
    val targetSeconds: Int,
    val isComplete: Boolean
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

        // Go cue!
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
