package com.example.lifelab.feature.habits.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY name ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY name ASC")
    suspend fun getHabits(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabit(id: String): HabitEntity?

    @Upsert
    suspend fun upsertHabit(habit: HabitEntity)

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun countHabits(): Int
}
