package com.example.lifelab.feature.notifications.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus

@Entity(tableName = "notification_messages")
data class NotificationMessageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val category: String,
    @ColumnInfo(name = "created_at_label") val createdAtLabel: String,
    val status: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "account_id")
    val accountId: String,
    @ColumnInfo(name = "in_app_messages_enabled")
    val inAppMessagesEnabled: Boolean,
    @ColumnInfo(name = "system_notifications_enabled")
    val systemNotificationsEnabled: Boolean,
)

fun NotificationMessageEntity.toDomain(): NotificationMessage =
    NotificationMessage(
        id = id,
        title = title,
        body = body,
        category = category,
        createdAtLabel = createdAtLabel,
        status = NotificationStatus.valueOf(status),
    )

fun NotificationMessage.toEntity(sortOrder: Int): NotificationMessageEntity =
    NotificationMessageEntity(
        id = id,
        title = title,
        body = body,
        category = category,
        createdAtLabel = createdAtLabel,
        status = status.name,
        sortOrder = sortOrder,
    )

fun NotificationSettingsEntity.toDomain(): NotificationSettings =
    NotificationSettings(
        accountId = accountId,
        inAppMessagesEnabled = inAppMessagesEnabled,
        systemNotificationsEnabled = systemNotificationsEnabled,
    )

fun NotificationSettings.toEntity(): NotificationSettingsEntity =
    NotificationSettingsEntity(
        accountId = accountId,
        inAppMessagesEnabled = inAppMessagesEnabled,
        systemNotificationsEnabled = systemNotificationsEnabled,
    )
