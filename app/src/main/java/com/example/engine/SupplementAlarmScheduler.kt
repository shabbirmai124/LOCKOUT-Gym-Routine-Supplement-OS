package com.example.engine

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.SupplementEntity
import java.util.Calendar

class SupplementAlarmScheduler(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "lockout_supplement_alarms"
        const val EXTRA_SUPP_ID = "extra_supp_id"
        const val EXTRA_SUPP_NAME = "extra_supp_name"
        const val EXTRA_SUPP_DOSAGE = "extra_supp_dosage"
        const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
        const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return
                val existing = manager.getNotificationChannel(CHANNEL_ID)
                if (existing != null) return

                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Supplement Dose Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Scheduled daily alerts for your LOCKOUT supplement stack"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0L, 220L, 140L, 280L)
                    setSound(soundUri, audioAttrs)
                }
                manager.createNotificationChannel(channel)
            }
        }

        fun postDoseAlarmNotification(
            context: Context,
            supplementId: Int,
            supplementName: String,
            dosage: String
        ) {
            ensureNotificationChannel(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                supplementId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("LOCKOUT Dose Alarm: $supplementName")
                .setContentText("Take $dosage now to maintain your stack streak.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Scheduled dose reminder: $supplementName ($dosage). Open LOCKOUT to log today's dose and keep your streak alive.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .build()

            runCatching {
                NotificationManagerCompat.from(context).notify(4000 + supplementId, notification)
            }
        }
    }

    fun scheduleDailyReminder(supplement: SupplementEntity) {
        ensureNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerAtMillis = computeNextTriggerMillis(supplement.reminderHour, supplement.reminderMinute)
        val intent = buildAlarmIntent(
            supplementId = supplement.id,
            name = supplement.name,
            dosage = supplement.dosage,
            hour = supplement.reminderHour,
            minute = supplement.reminderMinute
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(supplement.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun scheduleNextDayReminder(
        supplementId: Int,
        name: String,
        dosage: String,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val intent = buildAlarmIntent(supplementId, name, dosage, hour, minute)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(supplementId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        runCatching {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(supplementId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SupplementAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(supplementId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        runCatching { alarmManager.cancel(pendingIntent) }
    }

    fun triggerImmediateTestAlarm(supplement: SupplementEntity) {
        playMusicDuckingAlert(isDoubleChime = true)
        postDoseAlarmNotification(
            context = context,
            supplementId = supplement.id,
            supplementName = supplement.name,
            dosage = supplement.dosage
        )
    }

    fun playDoseTakenChime() {
        playMusicDuckingAlert(isDoubleChime = false)
    }

    private fun buildAlarmIntent(
        supplementId: Int,
        name: String,
        dosage: String,
        hour: Int,
        minute: Int
    ): Intent = Intent(context, SupplementAlarmReceiver::class.java).apply {
        putExtra(EXTRA_SUPP_ID, supplementId)
        putExtra(EXTRA_SUPP_NAME, name)
        putExtra(EXTRA_SUPP_DOSAGE, dosage)
        putExtra(EXTRA_ALARM_HOUR, hour)
        putExtra(EXTRA_ALARM_MINUTE, minute)
    }

    private fun requestCodeFor(supplementId: Int): Int = 8100 + supplementId

    private fun computeNextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.timeInMillis <= now.timeInMillis + 1500L) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun playMusicDuckingAlert(isDoubleChime: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        var focusRequest: AudioFocusRequest? = null

        runCatching {
            if (audioManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val attrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(attrs)
                        .build()
                    audioManager.requestAudioFocus(focusRequest!!)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(
                        null,
                        AudioManager.STREAM_NOTIFICATION,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                    )
                }
            }

            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            toneGen.startTone(
                if (isDoubleChime) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_ACK,
                if (isDoubleChime) 320 else 150
            )
            triggerHapticPulse(if (isDoubleChime) 240L else 60L)

            if (audioManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                audioManager.abandonAudioFocusRequest(focusRequest!!)
            }
        }
    }

    private fun triggerHapticPulse(durationMs: Long) {
        runCatching {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        }
    }
}
