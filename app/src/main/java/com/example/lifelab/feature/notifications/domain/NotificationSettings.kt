package com.example.lifelab.feature.notifications.domain

data class NotificationSettings(
    val accountId: String,
    val inAppMessagesEnabled: Boolean,
    val systemNotificationsEnabled: Boolean,
)
