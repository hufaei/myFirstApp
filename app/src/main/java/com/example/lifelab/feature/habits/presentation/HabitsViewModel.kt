package com.example.lifelab.feature.habits.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.notifications.HabitReminderNotificationScheduler
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoRecordRepository
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.feature.habits.data.InMemoryHabitRepository
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitCheckInResult
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.model.HabitReminderPriority
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
    private val reminderScheduler: HabitReminderNotificationScheduler? = null,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    @Inject
    constructor(
        repository: HabitRepository,
        photoRecordRepository: PhotoRecordRepository,
        reminderScheduler: HabitReminderNotificationScheduler,
    ) : this(
        repository = repository,
        checkInHabit = CheckInHabitUseCase(repository),
        calculateStats = CalculateHabitStatsUseCase(),
        photoRecordRepository = photoRecordRepository,
        reminderScheduler = reminderScheduler,
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
                        errorMessage = HabitUiMessage.LoadError,
                    )
                }
                .collect { habits ->
                    reminderScheduler?.rescheduleAll(habits)
                    _uiState.value = _uiState.value
                        .forHabits(habits.sortedForDisplay())
                        .copy(habitPhotos = loadHabitPhotos(habits))
                }
        }
    }

    fun checkIn(habitId: String) {
        viewModelScope.launch {
            when (val result = checkInHabit(habitId = habitId, checkInDate = today())) {
                is HabitCheckInResult.CheckedIn -> {
                    showMessage(HabitUiMessage.CheckedIn(result.habit.name))
                }

                is HabitCheckInResult.AlreadyCheckedIn -> {
                    showMessage(HabitUiMessage.AlreadyCheckedIn(result.habit.name))
                }

                HabitCheckInResult.HabitMissing -> {
                    showMessage(HabitUiMessage.Missing)
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
            time = if (enabled) habit.reminder.time ?: DefaultHabitReminderTime else habit.reminder.time,
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

    fun updateReminderPriority(
        habitId: String,
        priority: HabitReminderPriority,
    ) {
        val habit = uiState.value.habits.firstOrNull { it.id == habitId } ?: return
        updateReminder(
            habitId = habitId,
            reminder = habit.reminder.copy(
                priority = priority,
                alarmClockEnabled = habit.reminder.alarmClockEnabled && priority == HabitReminderPriority.High,
            ),
        )
    }

    fun setReminderAlarmClockEnabled(
        habitId: String,
        enabled: Boolean,
    ) {
        val habit = uiState.value.habits.firstOrNull { it.id == habitId } ?: return
        updateReminder(
            habitId = habitId,
            reminder = habit.reminder.copy(
                alarmClockEnabled = enabled && habit.reminder.priority == HabitReminderPriority.High,
            ),
        )
    }

    fun startCreateHabit() {
        _uiState.value = _uiState.value.copy(
            editor = HabitEditorState(mode = HabitEditorMode.Create),
            message = null,
        )
    }

    fun startEditHabit(habitId: String) {
        val habit = uiState.value.habits.firstOrNull { it.id == habitId }
        if (habit == null) {
            showMessage(HabitUiMessage.Missing)
            return
        }

        _uiState.value = _uiState.value.copy(
            editor = HabitEditorState(
                mode = HabitEditorMode.Edit,
                habitId = habit.id,
                name = habit.name,
                reminderEnabled = habit.reminder.enabled,
                reminderTime = habit.reminder.time ?: DefaultHabitReminderTime,
                reminderPriority = habit.reminder.priority,
                reminderAlarmClockEnabled = habit.reminder.alarmClockEnabled &&
                    habit.reminder.priority == HabitReminderPriority.High,
            ),
            message = null,
        )
    }

    fun updateEditorName(name: String) {
        updateEditor { editor -> editor.copy(name = name) }
    }

    fun setEditorReminderEnabled(enabled: Boolean) {
        updateEditor { editor -> editor.copy(reminderEnabled = enabled) }
    }

    fun updateEditorReminderTime(time: LocalTime) {
        updateEditor { editor -> editor.copy(reminderTime = time) }
    }

    fun updateEditorReminderPriority(priority: HabitReminderPriority) {
        updateEditor { editor ->
            editor.copy(
                reminderPriority = priority,
                reminderAlarmClockEnabled = editor.reminderAlarmClockEnabled &&
                    priority == HabitReminderPriority.High,
            )
        }
    }

    fun setEditorAlarmClockEnabled(enabled: Boolean) {
        updateEditor { editor ->
            editor.copy(
                reminderAlarmClockEnabled = enabled && editor.reminderPriority == HabitReminderPriority.High,
            )
        }
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(editor = null)
    }

    fun saveEditor() {
        val editor = uiState.value.editor ?: return
        val habitName = editor.name.trim()
        if (habitName.isBlank()) {
            return
        }

        viewModelScope.launch {
            val habit = when (editor.mode) {
                HabitEditorMode.Create -> editor.toNewHabit(habitName)
                HabitEditorMode.Edit -> {
                    val habitId = editor.habitId
                    val existingHabit = habitId?.let { repository.getHabit(it) }
                    existingHabit?.copy(
                        name = habitName,
                        reminder = editor.toReminder(),
                    )
                }
            }

            if (habit == null) {
                showMessage(HabitUiMessage.Missing)
                return@launch
            }

            val savedHabit = repository.saveHabit(habit)
            reminderScheduler?.scheduleOrCancel(savedHabit)
            _uiState.value = _uiState.value.copy(
                editor = null,
                message = HabitUiMessage.HabitSaved(savedHabit.name),
            )
        }
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
            val habit = repository.getHabit(habitId)
            val updatedHabit = habit?.let { repository.saveHabit(it.copy(reminder = reminder)) }
            showMessage(
                if (updatedHabit == null) {
                    HabitUiMessage.Missing
                } else {
                    reminderScheduler?.scheduleOrCancel(updatedHabit)
                    HabitUiMessage.ReminderUpdated(updatedHabit.name)
                },
            )
        }
    }

    private fun showMessage(message: HabitUiMessage) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    private fun updateEditor(transform: (HabitEditorState) -> HabitEditorState) {
        val editor = uiState.value.editor ?: return
        _uiState.value = _uiState.value.copy(editor = transform(editor))
    }

    private fun HabitEditorState.toNewHabit(habitName: String): Habit =
        Habit(
            id = buildHabitId(habitName),
            name = habitName,
            frequency = HabitFrequency.Daily,
            streakCount = 0,
            lastCheckInDate = null,
            reminder = toReminder(),
        )

    private fun HabitEditorState.toReminder(): HabitReminder =
        HabitReminder(
            enabled = reminderEnabled,
            time = if (reminderEnabled) reminderTime else null,
            priority = reminderPriority,
            alarmClockEnabled = reminderAlarmClockEnabled && reminderPriority == HabitReminderPriority.High,
        )

    private fun buildHabitId(name: String): String {
        val baseId = "habit-${name.toHabitIdSlug()}-${nowMillis()}"
        val existingIds = uiState.value.habits.map { habit -> habit.id }.toSet()
        if (baseId !in existingIds) {
            return baseId
        }

        var suffix = 2
        while ("$baseId-$suffix" in existingIds) {
            suffix += 1
        }
        return "$baseId-$suffix"
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

    private fun HabitsUiState.forHabits(habits: List<Habit>): HabitsUiState {
        val currentDate = today()
        return copy(
            status = if (habits.isEmpty()) HabitsStatus.Empty else HabitsStatus.Content,
            habits = habits,
            stats = calculateStats(habits, currentDate),
            today = currentDate,
            errorMessage = null,
        )
    }

}

private val RepeatedDashRegex = Regex("-+")

private fun String.toHabitIdSlug(): String {
    val slug = trim()
        .lowercase()
        .map { character ->
            when (character) {
                in 'a'..'z', in '0'..'9' -> character
                else -> '-'
            }
        }
        .joinToString("")
        .replace(RepeatedDashRegex, "-")
        .trim('-')

    return slug.ifBlank { "custom" }
}

private fun List<Habit>.sortedForDisplay(): List<Habit> =
    sortedWith(
        compareBy<Habit> { habit -> if (habit.reminder.enabled) 0 else 1 }
            .thenBy { habit -> habit.reminder.time?.toSecondOfDay() ?: Int.MAX_VALUE }
            .thenBy { habit -> habit.name },
    )
