package com.example.lifelab.feature.habits.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.feature.habits.data.InMemoryHabitRepository
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitCheckInResult
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import com.example.lifelab.feature.habits.domain.usecase.CalculateHabitStatsUseCase
import com.example.lifelab.feature.habits.domain.usecase.CheckInHabitUseCase
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val repository: HabitRepository = InMemoryHabitRepository(),
    private val checkInHabit: CheckInHabitUseCase = CheckInHabitUseCase(repository),
    private val calculateStats: CalculateHabitStatsUseCase = CalculateHabitStatsUseCase(),
    private val today: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeHabits()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        status = HabitsStatus.Error,
                        errorMessage = "Unable to load habits.",
                    )
                }
                .collect { habits ->
                    _uiState.value = _uiState.value.forHabits(habits)
                }
        }
    }

    fun checkIn(habitId: String) {
        viewModelScope.launch {
            when (val result = checkInHabit(habitId = habitId, checkInDate = today())) {
                is HabitCheckInResult.CheckedIn -> {
                    showMessage("Checked in ${result.habit.name}.")
                }

                is HabitCheckInResult.AlreadyCheckedIn -> {
                    showMessage("${result.habit.name} is already checked in today.")
                }

                HabitCheckInResult.HabitMissing -> {
                    showMessage("Habit was not found.")
                }
            }
        }
    }

    fun setReminderEnabled(
        habitId: String,
        enabled: Boolean,
    ) {
        val habit = uiState.value.habits.firstOrNull { it.id == habitId } ?: return
        val reminder = habit.reminder.copy(
            enabled = enabled,
            time = if (enabled) habit.reminder.time ?: DefaultReminderTime else habit.reminder.time,
        )

        updateReminder(habitId = habitId, reminder = reminder)
    }

    fun updateReminderTime(
        habitId: String,
        time: LocalTime,
    ) {
        val habit = uiState.value.habits.firstOrNull { it.id == habitId } ?: return
        updateReminder(
            habitId = habitId,
            reminder = habit.reminder.copy(enabled = true, time = time),
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun updateReminder(
        habitId: String,
        reminder: HabitReminder,
    ) {
        viewModelScope.launch {
            val updatedHabit = repository.updateReminder(habitId = habitId, reminder = reminder)
            showMessage(
                if (updatedHabit == null) {
                    "Habit was not found."
                } else {
                    "Reminder updated for ${updatedHabit.name}."
                },
            )
        }
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private fun HabitsUiState.forHabits(habits: List<Habit>): HabitsUiState = copy(
        status = if (habits.isEmpty()) HabitsStatus.Empty else HabitsStatus.Content,
        habits = habits,
        stats = calculateStats(habits, today()),
        errorMessage = null,
    )

    private companion object {
        val DefaultReminderTime: LocalTime = LocalTime.of(9, 0)
    }
}
