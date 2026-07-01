package com.example.lifelab.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.example.lifelab.core.database.LifeLabDatabase
import com.example.lifelab.core.datastore.AppPreferencesRepository
import com.example.lifelab.core.datastore.DataStoreAppPreferencesRepository
import com.example.lifelab.core.datastore.appPreferencesDataStore
import com.example.lifelab.core.media.PhotoRecordRepository
import com.example.lifelab.core.media.RoomPhotoRecordRepository
import com.example.lifelab.feature.discover.data.RoomDiscoverRepository
import com.example.lifelab.feature.discover.domain.DiscoverRepository
import com.example.lifelab.feature.habits.data.RoomHabitRepository
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import com.example.lifelab.feature.notifications.data.RoomNotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.search.data.RoomSearchRepository
import com.example.lifelab.feature.search.domain.SearchRepository
import com.example.lifelab.feature.tasks.data.RoomTaskRepository
import com.example.lifelab.feature.tasks.domain.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLifeLabDatabase(
        @ApplicationContext context: Context,
    ): LifeLabDatabase =
        Room.databaseBuilder(
            context,
            LifeLabDatabase::class.java,
            "lifelab.db",
        ).addMigrations(
            LifeLabDatabase.MIGRATION_1_2,
            LifeLabDatabase.MIGRATION_2_3,
            LifeLabDatabase.MIGRATION_3_4,
        ).build()

    @Provides
    @Singleton
    fun provideAppPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.appPreferencesDataStore

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(
        dataStore: DataStore<Preferences>,
    ): AppPreferencesRepository = DataStoreAppPreferencesRepository(dataStore)

    @Provides
    @Singleton
    fun provideTaskRepository(database: LifeLabDatabase): TaskRepository =
        RoomTaskRepository(database.taskDao())

    @Provides
    @Singleton
    fun provideHabitRepository(database: LifeLabDatabase): HabitRepository =
        RoomHabitRepository(database.habitDao())

    @Provides
    @Singleton
    fun provideNotificationRepository(database: LifeLabDatabase): NotificationRepository =
        RoomNotificationRepository(database.notificationDao())

    @Provides
    @Singleton
    fun provideSearchRepository(database: LifeLabDatabase): SearchRepository =
        RoomSearchRepository(
            searchDao = database.searchDao(),
            taskDao = database.taskDao(),
            habitDao = database.habitDao(),
            notificationDao = database.notificationDao(),
            discoverDao = database.discoverDao(),
            seedLocalData = {
                RoomTaskRepository(database.taskDao()).seedIfEmpty()
                RoomHabitRepository(database.habitDao()).seedIfEmpty()
                RoomNotificationRepository(database.notificationDao()).seedIfEmpty()
                RoomDiscoverRepository(database.discoverDao()).seedIfEmpty()
            },
        )

    @Provides
    @Singleton
    fun provideDiscoverRepository(database: LifeLabDatabase): DiscoverRepository =
        RoomDiscoverRepository(database.discoverDao())

    @Provides
    @Singleton
    fun providePhotoRecordRepository(database: LifeLabDatabase): PhotoRecordRepository =
        RoomPhotoRecordRepository(database.photoRecordDao())
}
