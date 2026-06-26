package com.example.lifelab.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lifelab.core.media.PhotoRecordDao
import com.example.lifelab.core.media.PhotoRecordEntity
import com.example.lifelab.feature.discover.data.DiscoverContentEntity
import com.example.lifelab.feature.discover.data.DiscoverDao
import com.example.lifelab.feature.habits.data.HabitDao
import com.example.lifelab.feature.habits.data.HabitEntity
import com.example.lifelab.feature.notifications.data.NotificationDao
import com.example.lifelab.feature.notifications.data.NotificationMessageEntity
import com.example.lifelab.feature.notifications.data.NotificationSettingsEntity
import com.example.lifelab.feature.search.data.HotKeywordEntity
import com.example.lifelab.feature.search.data.SearchDao
import com.example.lifelab.feature.search.data.SearchHistoryEntity
import com.example.lifelab.feature.search.data.SearchResultEntity
import com.example.lifelab.feature.tasks.data.TaskDao
import com.example.lifelab.feature.tasks.data.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        NotificationMessageEntity::class,
        NotificationSettingsEntity::class,
        SearchHistoryEntity::class,
        HotKeywordEntity::class,
        SearchResultEntity::class,
        DiscoverContentEntity::class,
        PhotoRecordEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class LifeLabDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun notificationDao(): NotificationDao
    abstract fun searchDao(): SearchDao
    abstract fun discoverDao(): DiscoverDao
    abstract fun photoRecordDao(): PhotoRecordDao
}
