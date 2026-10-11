package com.example.pulsenotes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Load settings to check if reminders are enabled and sound preference
        val file = java.io.File(context.filesDir, "settings_data.json")
        var remindersEnabled = true
        var defaultAlarmSound = true
        if (file.exists()) {
            try {
                val contentString = file.readText()
                if (contentString.isNotBlank()) {
                    val root = org.json.JSONObject(contentString)
                    remindersEnabled = root.optBoolean("remindersEnabled", true)
                    defaultAlarmSound = root.optBoolean("defaultAlarmSound", true)
                }
            } catch (_: java.lang.Exception) {}
        }

        if (!remindersEnabled) return

        val noteId = intent.getStringExtra("noteId") ?: ""
        val title = intent.getStringExtra("title") ?: "Reminder"
        val content = intent.getStringExtra("content") ?: "Your note reminder is here!"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pulse_notes_reminders"

        val soundUri = if (defaultAlarmSound) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pulse Notes Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification channel for Pulse Notes note reminders"
                enableVibration(true)
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create launch intent to reopen MainActivity when notification is clicked
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("noteId", noteId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(noteId.hashCode(), notification)
    }
}
