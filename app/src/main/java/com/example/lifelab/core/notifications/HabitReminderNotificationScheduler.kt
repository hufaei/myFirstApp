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

open class NotificationSelfTestScheduler {
    private val scheduler: HabitReminderNotificationScheduler?

    @Inject
    constructor(scheduler: HabitReminderNotificationScheduler) {
        this.scheduler = scheduler
    }

    protected constructor() {
        scheduler = null
    }

    open fun showTestNotification() {
        requireNotNull(scheduler).showTestNotification()
    }

    open fun scheduleTestReminderOneMinuteFromNow() {
        requireNotNull(scheduler).scheduleTestReminderOneMinuteFromNow()
    }
}

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
                alarmClockEnabled = habit.reminder.alarmClockEnabled,
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
        alarmClockEnabled: Boolean,
    ) {
        schedule(
            habitId = habitId,
            habitName = habitName,
            reminderTime = reminderTime,
            priority = priority,
            triggerAtMillis = nextTriggerAtMillis(reminderTime, minimumDelay = Duration.ofHours(12)),
            alarmClockEnabled = alarmClockEnabled,
        )
    }

    fun cancel(habitId: String) {
        alarmManager.cancel(
            existingReminderPendingIntent(habitId) ?: return,
        )
    }

    @SuppressLint("MissingPermission")
    fun showTestNotification() {
        if (!context.androidNotificationPermissionStatus().canPostNotifications) {
            return
        }
        ensureChannels()
        showNotification(
            notificationId = TEST_NOTIFICATION_ID.hashCode(),
            priority = HabitReminderPriority.High,
            title = context.getString(R.string.habit_reminder_test_notification_title),
            body = context.getString(R.string.habit_reminder_test_notification_body),
        )
    }

    fun scheduleTestReminderOneMinuteFromNow() {
        val reminderTime = LocalTime.now().plusMinutes(1)
        schedule(
            habitId = TEST_REMINDER_ID,
            habitName = context.getString(R.string.habit_reminder_test_scheduled_title),
            reminderTime = reminderTime,
            priority = HabitReminderPriority.High,
            triggerAtMillis = System.currentTimeMillis() + Duration.ofMinutes(1).toMillis(),
            alarmClockEnabled = false,
            rescheduleAfterDelivery = false,
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
        showNotification(
            notificationId = habitId.hashCode(),
            priority = priority,
            title = context.getString(R.string.habit_reminder_notification_title, habitName),
            body = context.getString(R.string.habit_reminder_notification_body),
        )
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(
        notificationId: Int,
        priority: HabitReminderPriority,
        title: String,
        body: String,
    ) {
        val notification = NotificationCompat.Builder(context, priority.channelId)
            .setSmallIcon(R.drawable.ic_stat_lifelab)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(openAppPendingIntent(notificationId))
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(priority.notificationCompatPriority)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun schedule(
        habitId: String,
        habitName: String,
        reminderTime: LocalTime,
        priority: HabitReminderPriority,
        triggerAtMillis: Long,
        alarmClockEnabled: Boolean,
        rescheduleAfterDelivery: Boolean = true,
    ) {
        ensureChannels()
        val reminderPendingIntent = reminderPendingIntent(
            habitId = habitId,
            flags = PendingIntent.FLAG_UPDATE_CURRENT,
            habitName = habitName,
            reminderTime = reminderTime,
            priority = priority,
            alarmClockEnabled = alarmClockEnabled,
            rescheduleAfterDelivery = rescheduleAfterDelivery,
        )
        if (alarmClockEnabled) {
            val alarmInfo = AlarmManager.AlarmClockInfo(
                triggerAtMillis,
                openAppPendingIntent(habitId.hashCode()),
            )
            alarmManager.setAlarmClock(alarmInfo, reminderPendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                reminderPendingIntent,
            )
        }
    }

    private fun reminderPendingIntent(
        habitId: String,
        flags: Int,
        habitName: String = "",
        reminderTime: LocalTime = LocalTime.MIDNIGHT,
        priority: HabitReminderPriority = HabitReminderPriority.Normal,
        alarmClockEnabled: Boolean = false,
        rescheduleAfterDelivery: Boolean = true,
    ): PendingIntent {
        val intent = Intent(context, HabitReminderReceiver::class.java)
            .putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
            .putExtra(HabitReminderReceiver.EXTRA_HABIT_NAME, habitName)
            .putExtra(HabitReminderReceiver.EXTRA_REMINDER_SECOND_OF_DAY, reminderTime.toSecondOfDay())
            .putExtra(HabitReminderReceiver.EXTRA_PRIORITY, priority.name)
            .putExtra(HabitReminderReceiver.EXTRA_ALARM_CLOCK_ENABLED, alarmClockEnabled)
            .putExtra(HabitReminderReceiver.EXTRA_RESCHEDULE_AFTER_DELIVERY, rescheduleAfterDelivery)
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

    private fun openAppPendingIntent(requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
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

    private companion object {
        const val TEST_NOTIFICATION_ID = "lifelab_test_notification"
        const val TEST_REMINDER_ID = "lifelab_test_reminder"
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
