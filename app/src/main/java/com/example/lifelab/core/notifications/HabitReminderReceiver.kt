package com.example.lifelab.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifelab.feature.habits.domain.model.HabitReminderPriority
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class HabitReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: HabitReminderNotificationScheduler

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID).orEmpty()
        val habitName = intent.getStringExtra(EXTRA_HABIT_NAME).orEmpty()
        if (habitId.isBlank() || habitName.isBlank()) {
            return
        }
        val reminderTime = LocalTime.ofSecondOfDay(
            intent.getIntExtra(EXTRA_REMINDER_SECOND_OF_DAY, 0).toLong(),
        )
        val priority = intent.getStringExtra(EXTRA_PRIORITY).toHabitReminderPriority()
        scheduler.showReminderNotification(
            habitId = habitId,
            habitName = habitName,
            priority = priority,
        )
        if (intent.getBooleanExtra(EXTRA_RESCHEDULE_AFTER_DELIVERY, true)) {
            scheduler.scheduleNextDay(
                habitId = habitId,
                habitName = habitName,
                reminderTime = reminderTime,
                priority = priority,
                alarmClockEnabled = intent.getBooleanExtra(EXTRA_ALARM_CLOCK_ENABLED, false),
            )
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val EXTRA_HABIT_NAME = "habit_name"
        const val EXTRA_REMINDER_SECOND_OF_DAY = "reminder_second_of_day"
        const val EXTRA_PRIORITY = "priority"
        const val EXTRA_ALARM_CLOCK_ENABLED = "alarm_clock_enabled"
        const val EXTRA_RESCHEDULE_AFTER_DELIVERY = "reschedule_after_delivery"
    }
}

private fun String?.toHabitReminderPriority(): HabitReminderPriority =
    enumValues<HabitReminderPriority>().firstOrNull { priority -> priority.name == this }
        ?: HabitReminderPriority.Normal
