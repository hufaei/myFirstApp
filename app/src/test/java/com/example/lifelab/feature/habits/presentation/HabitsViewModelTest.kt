package com.example.lifelab.feature.habits.presentation

import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.core.media.PhotoOwnerType
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.feature.habits.data.InMemoryHabitRepository
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.model.HabitReminderPriority
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class HabitsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadEmitsContentWithHabitsAndStats() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
                ),
                sampleHabit(
                    id = "walk",
                    reminder = HabitReminder(enabled = false, time = null),
                ),
            ),
        )

        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        val state = viewModel.uiState.value

        assertEquals(HabitsStatus.Content, state.status)
        assertEquals(listOf("hydrate", "walk"), state.habits.map { it.id })
        assertEquals(2, state.stats.totalHabits)
        assertEquals(0, state.stats.checkedInToday)
        assertEquals(1, state.stats.activeReminders)
        assertEquals(0, state.stats.longestStreak)
    }

    @Test
    fun checkInEventUpdatesHabitStreakAndCheckedInTodayStats() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
                ),
            ),
        )
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        viewModel.checkIn("hydrate")

        val state = viewModel.uiState.value
        assertEquals(1, state.habits.single().streakCount)
        assertEquals(today, state.habits.single().lastCheckInDate)
        assertEquals(1, state.stats.checkedInToday)
        assertEquals(1, state.stats.longestStreak)
        assertEquals(HabitUiMessage.CheckedIn("喝水"), state.message)
    }

    @Test
    fun duplicateCheckInExposesMessageWithoutIncreasingStreak() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
                ),
            ),
        )
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        viewModel.checkIn("hydrate")
        viewModel.clearMessage()
        viewModel.checkIn("hydrate")

        val state = viewModel.uiState.value
        assertEquals(1, state.habits.single().streakCount)
        assertEquals(1, state.stats.checkedInToday)
        assertEquals(HabitUiMessage.AlreadyCheckedIn("喝水"), state.message)
    }

    @Test
    fun habitsAreSortedByReminderEnabledTimeThenNameWithoutPriorityWeight() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "later-normal",
                    name = "Later normal",
                    reminder = HabitReminder(
                        enabled = true,
                        time = LocalTime.of(18, 0),
                        priority = HabitReminderPriority.Normal,
                    ),
                ),
                sampleHabit(
                    id = "off-high",
                    name = "Off high",
                    reminder = HabitReminder(
                        enabled = false,
                        time = LocalTime.of(8, 0),
                        priority = HabitReminderPriority.High,
                    ),
                ),
                sampleHabit(
                    id = "urgent",
                    name = "Urgent",
                    reminder = HabitReminder(
                        enabled = true,
                        time = LocalTime.of(12, 0),
                        priority = HabitReminderPriority.High,
                    ),
                ),
                sampleHabit(
                    id = "early-normal",
                    name = "Early normal",
                    reminder = HabitReminder(
                        enabled = true,
                        time = LocalTime.of(8, 0),
                        priority = HabitReminderPriority.Normal,
                    ),
                ),
            ),
        )

        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        assertEquals(
            listOf("early-normal", "urgent", "later-normal", "off-high"),
            viewModel.uiState.value.habits.map { it.id },
        )
    }

    @Test
    fun changingPriorityAwayFromHighClearsAlarmClockMode() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(
                        enabled = true,
                        time = LocalTime.of(9, 0),
                        priority = HabitReminderPriority.High,
                        alarmClockEnabled = true,
                    ),
                ),
            ),
        )
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        viewModel.updateReminderPriority("hydrate", HabitReminderPriority.Normal)

        assertEquals(
            HabitReminder(
                enabled = true,
                time = LocalTime.of(9, 0),
                priority = HabitReminderPriority.Normal,
                alarmClockEnabled = false,
            ),
            viewModel.uiState.value.habits.single().reminder,
        )
    }

    @Test
    fun reminderUpdateChangesTargetHabitReminderAndActiveReminderCount() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(enabled = false, time = null),
                ),
                sampleHabit(
                    id = "walk",
                    reminder = HabitReminder(enabled = true, time = LocalTime.of(18, 30)),
                ),
            ),
        )
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        viewModel.setReminderEnabled("hydrate", true)
        viewModel.updateReminderTime("hydrate", LocalTime.of(8, 15))

        val state = viewModel.uiState.value
        val hydrate = state.habits.first { it.id == "hydrate" }
        val walk = state.habits.first { it.id == "walk" }
        assertEquals(HabitReminder(enabled = true, time = LocalTime.of(8, 15)), hydrate.reminder)
        assertEquals(HabitReminder(enabled = true, time = LocalTime.of(18, 30)), walk.reminder)
        assertEquals(2, state.stats.activeReminders)
        assertEquals(HabitUiMessage.ReminderUpdated("喝水"), state.message)
    }

    @Test
    fun savingCreateEditorAddsHabitWithReminderAndUpdatesActiveReminderCount() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(enabled = false, time = null),
                ),
            ),
        )
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
            nowMillis = { 42_000L },
        )

        viewModel.startCreateHabit()
        viewModel.updateEditorName("Morning Yoga")
        viewModel.setEditorReminderEnabled(true)
        viewModel.updateEditorReminderTime(LocalTime.of(7, 30))
        viewModel.updateEditorReminderPriority(HabitReminderPriority.High)
        viewModel.setEditorAlarmClockEnabled(true)
        viewModel.saveEditor()

        val state = viewModel.uiState.value
        val created = state.habits.first { habit -> habit.id == "habit-morning-yoga-42000" }
        assertEquals("Morning Yoga", created.name)
        assertEquals(
            HabitReminder(
                enabled = true,
                time = LocalTime.of(7, 30),
                priority = HabitReminderPriority.High,
                alarmClockEnabled = true,
            ),
            created.reminder,
        )
        assertEquals(2, state.stats.totalHabits)
        assertEquals(1, state.stats.activeReminders)
        assertEquals(null, state.editor)
        assertEquals(HabitUiMessage.HabitSaved("Morning Yoga"), state.message)
    }

    @Test
    fun savingEditEditorUpdatesNameReminderAndActiveReminderCount() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "read",
                    name = "Read",
                    reminder = HabitReminder(enabled = false, time = null),
                ),
            ),
        )
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
        )

        viewModel.startEditHabit("read")
        viewModel.updateEditorName("Deep reading")
        viewModel.setEditorReminderEnabled(true)
        viewModel.updateEditorReminderTime(LocalTime.of(21, 0))
        viewModel.updateEditorReminderPriority(HabitReminderPriority.Low)
        viewModel.saveEditor()

        val state = viewModel.uiState.value
        val edited = state.habits.single()
        assertEquals("read", edited.id)
        assertEquals("Deep reading", edited.name)
        assertEquals(
            HabitReminder(
                enabled = true,
                time = LocalTime.of(21, 0),
                priority = HabitReminderPriority.Low,
            ),
            edited.reminder,
        )
        assertEquals(1, state.stats.totalHabits)
        assertEquals(1, state.stats.activeReminders)
        assertEquals(null, state.editor)
        assertEquals(HabitUiMessage.HabitSaved("Deep reading"), state.message)
    }

    @Test
    fun attachingHabitPhotosKeepsOnlyThreePhotosForThatHabit() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    id = "hydrate",
                    reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
                ),
                sampleHabit(
                    id = "walk",
                    reminder = HabitReminder(enabled = false, time = null),
                ),
            ),
        )
        var clock = 2_000L
        val viewModel = HabitsViewModel(
            repository = repository,
            today = { today },
            nowMillis = { clock++ },
        )

        viewModel.attachHabitPhotos(
            habitId = "hydrate",
            localUris = listOf("content://habit/1", "content://habit/2", "content://habit/3", "content://habit/4"),
            source = PhotoSource.Picker,
        )

        val state = viewModel.uiState.value
        val photos = state.photosForHabit("hydrate")
        assertEquals(3, photos.size)
        assertEquals(listOf("content://habit/1", "content://habit/2", "content://habit/3"), photos.map { it.localUri })
        assertEquals(PhotoOwnerType.Habit, photos.first().owner.type)
        assertEquals("hydrate", photos.first().owner.id)
        assertEquals(emptyList(), state.photosForHabit("walk"))
    }

    private companion object {
        val today: LocalDate = LocalDate.of(2026, 6, 24)

        fun sampleHabit(
            id: String,
            reminder: HabitReminder,
            name: String = when (id) {
                "hydrate" -> "喝水"
                "walk" -> "晚间散步"
                else -> id
            },
            streakCount: Int = 0,
            lastCheckInDate: LocalDate? = null,
            checkInDates: Set<LocalDate> = emptySet(),
        ) = Habit(
            id = id,
            name = name,
            frequency = HabitFrequency.Daily,
            streakCount = streakCount,
            lastCheckInDate = lastCheckInDate,
            reminder = reminder,
            checkInDates = checkInDates,
        )
    }
}
