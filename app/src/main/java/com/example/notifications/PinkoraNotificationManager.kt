package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.ReminderSetting
import java.util.Calendar

class PinkoraReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_REMINDER_TYPE) ?: "mood_checkin"
        val discreet = intent.getBooleanExtra(EXTRA_DISCREET, true)
        val lang = intent.getStringExtra(EXTRA_LANG) ?: "en"
        PinkoraNotificationManager.showReminderNotification(context, type, discreet, lang)
    }

    companion object {
        const val EXTRA_REMINDER_TYPE = "extra_reminder_type"
        const val EXTRA_DISCREET = "extra_discreet"
        const val EXTRA_LANG = "extra_lang"
    }
}

object PinkoraNotificationManager {
    private const val CHANNEL_ID = "pinkora_wellness_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showReminderNotification(
        context: Context,
        type: String,
        discreet: Boolean,
        lang: String,
    ) {
        if (!hasNotificationPermission(context)) return
        ensureChannel(context)

        val isHi = lang == "hi"
        val (title, body) = when {
            discreet -> {
                "PINKORA" to if (isHi) {
                    "PINKORA: आपके दैनिक चेक-इन का समय हो गया है"
                } else {
                    "PINKORA: Time for your wellness check-in"
                }
            }
            type == "water" -> {
                "PINKORA" to if (isHi) "एक गिलास पानी पीने का समय" else "Time to drink a glass of water"
            }
            type == "upcoming_period" -> {
                "PINKORA" to if (isHi) "आपका आगामी चक्र निकट हो सकता है" else "Your estimated cycle start may be approaching"
            }
            type == "self_care" -> {
                "PINKORA" to if (isHi) "थोड़ा रुकें और शांत श्वास लें" else "Pause for a mindful breathing & self-care moment"
            }
            else -> {
                "PINKORA" to if (isHi) "अपना दैनिक मूड और स्वास्थ्य दर्ज करें" else "Log your daily mood & wellness note"
            }
        }

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            context,
            type.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(
                if (discreet) NotificationCompat.VISIBILITY_PRIVATE else NotificationCompat.VISIBILITY_PUBLIC
            )
            .setAutoCancel(true)
            .setContentIntent(contentPending)
            .build()

        NotificationManagerCompat.from(context).notify(type.hashCode() and 0x7FFF, notification)
    }

    fun syncReminderSchedule(context: Context, setting: ReminderSetting, lang: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, PinkoraReminderReceiver::class.java).apply {
            putExtra(PinkoraReminderReceiver.EXTRA_REMINDER_TYPE, setting.type)
            putExtra(PinkoraReminderReceiver.EXTRA_DISCREET, setting.discreetText)
            putExtra(PinkoraReminderReceiver.EXTRA_LANG, lang)
        }
        val requestCode = setting.type.hashCode() and 0x7FFF
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.cancel(pendingIntent)
        if (!setting.enabled) return

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, setting.hour.toInt().coerceIn(0, 23))
            set(Calendar.MINUTE, setting.minute.toInt().coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val intervalMs = if (setting.type == "water" && setting.intervalHours > 0) {
            setting.intervalHours * AlarmManager.INTERVAL_HOUR
        } else {
            AlarmManager.INTERVAL_DAY
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerCal.timeInMillis,
            intervalMs,
            pendingIntent,
        )
    }
}
