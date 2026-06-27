package com.example.lifelab.feature.habits.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoRecordRepository
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.feature.habits.data.InMemoryHabitRepository
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitCheckInResult
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import com.example.lifelab.feature.habits.domain.usecase.CalculateHabitStatsUseCase
import com.example.lifelab.feature.habits.domain.usecase.CheckInHabitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class HabitsViewModel(
    private val repository: HabitRepository = InMemoryHabitRepository(),
    private val checkInHabit: CheckInHabitUseCase = CheckInHabitUseCase(repository),
    private val calculateStats: CalculateHabitStatsUseCase = CalculateHabitStatsUseCase(),
    private val today: () -> LocalDate = { LocalDate.now() },
    private val photoRecordRepository: PhotoRecordRepository? = null,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    @Inject
    constructor(
        repository: HabitRepository,
        photoRecordRepository: PhotoRecordRepository,
    ) : this(
        repository = repository,
        checkInHabit = CheckInHabitUseCase(repository),
        calculateStats = CalculateHabitStatsUseCase(),
        photoRecordRepository = photoRecordRepository,
    )

    private val _uiState = MutableStateFlow(HabitsUiState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()
    private val photoPolicy = PhotoAttachmentPolicy()

    init {
        viewModelScope.launch {
            repository.observeHabits()
                .catch {
                    _uiState.value = _uiState.value.copy(
                        status = HabitsStatus.Error,
                        errorMessage = "无法加载习惯。",
                    )
                }
                .collect { habits ->
                    _uiState.value = _uiState.value
                        .forHabits(habits)
                        .copy(habitPhotos = loadHabitPhotos(habits))
                }
        }
    }

    fun checkIn(habitId: String) {
        viewModelScope.launch {
            when (val result = checkInHabit(habitId = habitId, checkInDate = today())) {
                is HabitCheckInResult.CheckedIn -> {
                    showMessage("${result.habit.name}已打卡。")
                }

                is HabitCheckInResult.AlreadyCheckedIn -> {
                    showMessage("${result.habit.name}今天已经打过卡。")
                }

                HabitCheckInResult.HabitMissing -> {
                    showMessage("没有找到这个习惯。")
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

    fun attachHabitPhotos(
        habitId: String,
        localUris: List<String>,
        source: PhotoSource,
    ) {
        _uiState.value = _uiState.value.let { state ->
            state.copy(
                habitPhotos = state.habitPhotos.attachPhotos(
                    owner = habitPhotoOwner(habitId),
                    localUris = localUris,
                    source = source,
                ),
            )
        }
        persistHabitPhotos(
            owner = habitPhotoOwner(habitId),
            localUris = localUris,
            source = source,
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
                    "没有找到这个习惯。"
                } else {
                    "${updatedHabit.name}的提醒已更新。"
                },
            )
        }
    }

    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private suspend fun loadHabitPhotos(habits: List<Habit>): Map<String, List<PhotoRecord>> {
        val repository = photoRecordRepository ?: return _uiState.value.habitPhotos
        return habits.associate { habit ->
            habit.id to repository.observePhotoRecords(habitPhotoOwner(habit.id)).first()
        }
    }

    private fun persistHabitPhotos(
        owner: PhotoOwner,
        localUris: List<String>,
        source: PhotoSource,
    ) {
        val repository = photoRecordRepository ?: return
        viewModelScope.launch {
            localUris.filter { it.isNotBlank() }.forEach { localUri ->
                repository.addPhotoRecord(
                    owner = owner,
                    localUri = localUri,
                    source = source,
                    createdAtMillis = nowMillis(),
                )
            }
        }
    }

    private fun Map<String, List<PhotoRecord>>.attachPhotos(
        owner: PhotoOwner,
        localUris: List<String>,
        source: PhotoSource,
    ): Map<String, List<PhotoRecord>> {
        val existingPhotos = this[owner.id].orEmpty()
        val trimmedUris = photoPolicy.trimToAvailableSlots(
            owner = owner,
            existingRecords = existingPhotos,
            candidates = localUris.filter { it.isNotBlank() },
        )
        if (trimmedUris.isEmpty()) {
            return this
        }
        val nextOrder = existingPhotos.size
        val newPhotos = trimmedUris.mapIndexed { index, localUri ->
            val createdAtMillis = nowMillis()
            PhotoRecord(
                id = "photo-${owner.type.storageSegment}-${owner.id}-$createdAtMillis-${nextOrder + index}",
                owner = owner,
                localUri = localUri,
                source = source,
                sortOrder = nextOrder + index,
                createdAtMillis = createdAtMillis,
            )
        }
        return this + (owner.id to (existingPhotos + newPhotos))
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
