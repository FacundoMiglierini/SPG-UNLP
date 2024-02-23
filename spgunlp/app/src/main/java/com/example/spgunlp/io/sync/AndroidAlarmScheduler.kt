package com.example.spgunlp.io.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule() {
        val intent = Intent(context, AlarmReceiver::class.java)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, 5000
            , 1000 * 60 * 30, // 30min,
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

    override fun cancel() {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

}