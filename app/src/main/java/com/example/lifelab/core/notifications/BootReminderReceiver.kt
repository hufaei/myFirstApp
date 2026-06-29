package com.example.lifelab.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var habitRepository: HabitRepository

    @Inject
    lateinit var scheduler: HabitReminderNotificationScheduler

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action !in supportedActions) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                scheduler.rescheduleAll(habitRepository.observeHabits().first())
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val supportedActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
