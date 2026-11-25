package com.example.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SupplementAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(SupplementAlarmScheduler.EXTRA_SUPP_ID, 0)
        val name = intent.getStringExtra(SupplementAlarmScheduler.EXTRA_SUPP_NAME) ?: "Supplement"
        val dosage = intent.getStringExtra(SupplementAlarmScheduler.EXTRA_SUPP_DOSAGE) ?: "1 serving"
        val hour = intent.getIntExtra(SupplementAlarmScheduler.EXTRA_ALARM_HOUR, 8)
        val minute = intent.getIntExtra(SupplementAlarmScheduler.EXTRA_ALARM_MINUTE, 0)

        SupplementAlarmScheduler.postDoseAlarmNotification(
            context = context,
            supplementId = id,
            supplementName = name,
            dosage = dosage
        )

        // Automatically reschedule for tomorrow at the exact same time
        if (id > 0) {
            SupplementAlarmScheduler(context).scheduleNextDayReminder(
                supplementId = id,
                name = name,
                dosage = dosage,
                hour = hour,
                minute = minute
            )
        }
    }
}
