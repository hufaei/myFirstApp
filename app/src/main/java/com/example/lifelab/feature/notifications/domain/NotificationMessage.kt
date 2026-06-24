package com.example.lifelab.feature.notifications.domain

data class NotificationMessage(
    val id: String,
    val title: String,
    val body: String,
    val category: String,
    val createdAtLabel: String,
    val status: NotificationStatus,
)

enum class NotificationStatus {
    Unread,
    Read,
    Archived,
}
