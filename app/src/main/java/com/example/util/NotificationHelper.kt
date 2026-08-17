package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.model.PrayerType

object NotificationHelper {

    const val CHANNEL_ID_PRAYER = "namaz_vakti_ezan_channel"
    const val CHANNEL_ID_REMINDER = "namaz_vakti_hatirlatici_channel"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val prayerChannel = NotificationChannel(
                CHANNEL_ID_PRAYER,
                "Ezan Vakti Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Namaz vakti girdiğinde ezan ve sesli bildirim uyarısı"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDER,
                "Vakit Öncesi Hatırlatıcı",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Namaz vaktine 15 dakika kala hatırlatıcı bildirimler"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun showPrayerNotification(
        context: Context,
        prayerType: PrayerType,
        cityName: String,
        timeFormatted: String,
        isEarlyReminder: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            prayerType.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (isEarlyReminder) CHANNEL_ID_REMINDER else CHANNEL_ID_PRAYER
        val title = if (isEarlyReminder) {
            "⏳ ${prayerType.titleTr} Vaktine 15 Dakika Kaldı"
        } else {
            "🕌 ${prayerType.titleTr} Vakti Girdi ($timeFormatted)"
        }

        val content = if (isEarlyReminder) {
            "$cityName için ${prayerType.titleTr} vaktine az kaldı. Abdestinizi ve hazırlığınızı yapabilirsiniz."
        } else {
            "Haydin Namaza! $cityName için ${prayerType.titleTr} namazı vakti başladı."
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(if (isEarlyReminder) NotificationCompat.PRIORITY_DEFAULT else NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))

        val notificationId = if (isEarlyReminder) 100 + prayerType.ordinal else 10 + prayerType.ordinal
        notificationManager.notify(notificationId, builder.build())
    }

    fun showTestNotification(context: Context, cityName: String) {
        showPrayerNotification(
            context = context,
            prayerType = PrayerType.DHUHR,
            cityName = cityName,
            timeFormatted = "13:15",
            isEarlyReminder = false
        )
    }
}
