package com.example.lifelab.feature.notifications.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_messages ORDER BY sort_order ASC")
    fun observeMessages(): Flow<List<NotificationMessageEntity>>

    @Query("SELECT * FROM notification_messages ORDER BY sort_order ASC")
    suspend fun getMessages(): List<NotificationMessageEntity>

    @Query("SELECT * FROM notification_messages WHERE id = :id")
    suspend fun getMessage(id: String): NotificationMessageEntity?

    @Upsert
    suspend fun upsertMessage(message: NotificationMessageEntity)

    @Query("SELECT * FROM notification_settings WHERE account_id = :accountId")
    fun observeSettings(accountId: String): Flow<NotificationSettingsEntity?>

    @Query("SELECT * FROM notification_settings WHERE account_id = :accountId")
    suspend fun getSettings(accountId: String): NotificationSettingsEntity?

    @Upsert
    suspend fun upsertSettings(settings: NotificationSettingsEntity)

    @Query("SELECT COUNT(*) FROM notification_messages")
    suspend fun countMessages(): Int
}
