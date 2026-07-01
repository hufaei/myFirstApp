package com.example.lifelab.core.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.lifelab.R
import com.example.lifelab.app.MainActivity
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitReminderPriority
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitReminderNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleOrCancel(habit: Habit) {
        val reminderTime = habit.reminder.time
        if (habit.reminder.enabled && reminderTime != null) {
            schedule(
                habitId = habit.id,
                habitName = habit.name,
                reminderTime = reminderTime,
                priority = habit.reminder.priority,
                triggerAtMillis = nextTriggerAtMillis(reminderTime),
            )
        } else {
            cancel(habit.id)
        }
    }

    fun rescheduleAll(habits: List<Habit>) {
        habits.forEach(::scheduleOrCancel)
    }

    fun scheduleNextDay(
        habitId: String,
        habitName: String,
        reminderTime: LocalTime,
        priority: HabitReminderPriority,
    ) {
        schedule(
            habitId = habitId,
            habitName = habitName,
            reminderTime = reminderTime,
            priority = priority,
            triggerAtMillis = nextTriggerAtMillis(reminderTime, minimumDelay = Duration.ofHours(12)),
        )
    }

    fun cancel(habitId: String) {
        alarmManager.cancel(
            existingReminderPendingIntent(habitId) ?: return,
        )
    }

    @SuppressLint("MissingPermission")
    fun showReminderNotification(
        habitId: String,
        habitName: String,
        priority: HabitReminderPriority,
    ) {
        if (!context.androidNotificationPermissionStatus().canPostNotifications) {
            return
        }
        ensureChannels()
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            habitId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, priority.channelId)
            .setSmallIcon(R.drawable.ic_stat_lifelab)
            .setContentTitle(context.getString(R.string.habit_reminder_notification_title, habitName))
            .setContentText(context.getString(R.string.habit_reminder_notification_body))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setPriority(priority.notificationCompatPriority)
            .build()
        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }

    private fun schedule(
        habitId: String,
        habitName: String,
        reminderTime: LocalTime,
        priority: HabitReminderPriority,
        triggerAtMillis: Long,
    ) {
        ensureChannels()
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            reminderPendingIntent(
                habitId = habitId,
                flags = PendingIntent.FLAG_UPDATE_CURRENT,
                habitName = habitName,
                reminderTime = reminderTime,
                priority = priority,
            ),
        )
    }

    private fun reminderPendingIntent(
        habitId: String,
        flags: Int,
        habitName: String = "",
        reminderTime: LocalTime = LocalTime.MIDNIGHT,
        priority: HabitReminderPriority = HabitReminderPriority.Normal,
    ): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java)
            .putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            .putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
            .putExtra(HabitReminderReceiver.EXTRA_REMINDER_SECOND_OF_DAY, reminderTime.toSecondOfDay())
            .putExtra(HabitReminderReceiver.EXTRA_PRIORITY, priority.name)
        return PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun existingReminderPendingIntent(habitId: String): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            habitId.hashCode(),
            Intent(context, HabitReminderReceiver::class.java)
                .putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun nextTriggerAtMillis(
        reminderTime: LocalTime,
        minimumDelay: Duration = Duration.ZERO,
    ): Long {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(reminderTime)
        if (!next.isAfter(now.plus(minimumDelay))) {
            next = next.plusDays(1)
        }
        return next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        HabitReminderPriority.entries.forEach { priority ->
            val channel = NotificationChannel(
                priority.channelId,
                context.getString(priority.channelNameRes),
                priority.notificationImportance,
            ).apply {
                description = context.getString(R.string.habit_reminder_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}

private val HabitReminderPriority.channelId: String
    get() = when (this) {
        HabitReminderPriority.High -> "habit_reminders_high"
        HabitReminderPriority.Normal -> "habit_reminders_normal"
        HabitReminderPriority.Low -> "habit_reminders_low"
    }

private val HabitReminderPriority.channelNameRes: Int
    get() = when (this) {
        HabitReminderPriority.High -> R.string.habit_reminder_channel_high
        HabitReminderPriority.Normal -> R.string.habit_reminder_channel_normal
        HabitReminderPriority.Low -> R.string.habit_reminder_channel_low
    }

private val HabitReminderPriority.notificationImportance: Int
    get() = when (this) {
        HabitReminderPriority.High -> NotificationManager.IMPORTANCE_HIGH
        HabitReminderPriority.Normal -> NotificationManager.IMPORTANCE_DEFAULT
        HabitReminderPriority.Low -> NotificationManager.IMPORTANCE_LOW
    }

private val HabitReminderPriority.notificationCompatPriority: Int
    get() = when (this) {
        HabitReminderPriority.High -> NotificationCompat.PRIORITY_HIGH
        HabitReminderPriority.Normal -> NotificationCompat.PRIORITY_DEFAULT
        HabitReminderPriority.Low -> NotificationCompat.PRIORITY_LOW
    }
