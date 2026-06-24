package com.example.lifelab.feature.habits.domain.model

import java.time.LocalTime

data class HabitReminder(
    val enabled: Boolean,
    val time: LocalTime?,
)
