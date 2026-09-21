package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.NotificationType

object AppNotificationManager {

    const val CHANNEL_NETWORKS = "channel_networks"
    const val CHANNEL_WALLET = "channel_wallet"
    const val CHANNEL_SYSTEM = "channel_system"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val netChannel = NotificationChannel(
                CHANNEL_NETWORKS,
                "تنبيهات الشبكات والإنترنت",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات قبول الانضمام وشحن سقف رصيد الشبكات"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val walletChannel = NotificationChannel(
                CHANNEL_WALLET,
                "تنبيهات المحفظة المالية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات شحن وتحديثات رصيد المحفظة"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val sysChannel = NotificationChannel(
                CHANNEL_SYSTEM,
                "تنبيهات النظام والعروض",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات تحديثات النظام وتنبيهات الأمان"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(netChannel)
            notificationManager.createNotificationChannel(walletChannel)
            notificationManager.createNotificationChannel(sysChannel)
        }
    }

    fun showSystemNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        type: NotificationType
    ) {
        createNotificationChannels(context)

        val channelId = when (type) {
            NotificationType.NETWORK_JOIN_APPROVED,
            NotificationType.NETWORK_CREDIT_GRANTED,
            NotificationType.NETWORK_LOW_BALANCE -> CHANNEL_NETWORKS

            NotificationType.WALLET_TOPUP_SUCCESS,
            NotificationType.WALLET_LOW_BALANCE -> CHANNEL_WALLET

            NotificationType.SYSTEM_ANNOUNCEMENT -> CHANNEL_SYSTEM
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = try {
            R.drawable.ic_app_logo
        } catch (e: Exception) {
            R.mipmap.ic_launcher
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notificationId, builder.build())
            } else {
                val systemService = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                systemService?.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
