package com.example.lifelab.feature.notifications.domain

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.notifications.data.InMemoryNotificationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ChangeMessageStatusUseCaseTest {

    @Test
    fun unreadMessageCanBeMarkedRead() = runBlocking {
        val repository = InMemoryNotificationRepository(messages = listOf(message(status = NotificationStatus.Unread)))
        val useCase = ChangeMessageStatusUseCase(repository)

        val result = useCase(messageId = "message-1", targetStatus = NotificationStatus.Read)

        val updated = assertIs<AppResult.Success<NotificationMessage>>(result).value
        assertEquals(NotificationStatus.Read, updated.status)
        assertEquals(NotificationStatus.Read, repository.messages.first().single().status)
    }

    @Test
    fun readMessageCanBeMarkedReadAgainWithoutChangingState() = runBlocking {
        val repository = InMemoryNotificationRepository(messages = listOf(message(status = NotificationStatus.Read)))
        val useCase = ChangeMessageStatusUseCase(repository)

        val result = useCase(messageId = "message-1", targetStatus = NotificationStatus.Read)

        val updated = assertIs<AppResult.Success<NotificationMessage>>(result).value
        assertEquals(NotificationStatus.Read, updated.status)
        assertEquals(message(status = NotificationStatus.Read), updated)
    }

    @Test
    fun unreadMessageCanBeArchived() = runBlocking {
        val repository = InMemoryNotificationRepository(messages = listOf(message(status = NotificationStatus.Unread)))
        val useCase = ChangeMessageStatusUseCase(repository)

        val result = useCase(messageId = "message-1", targetStatus = NotificationStatus.Archived)

        val updated = assertIs<AppResult.Success<NotificationMessage>>(result).value
        assertEquals(NotificationStatus.Archived, updated.status)
        assertEquals(NotificationStatus.Archived, repository.messages.first().single().status)
    }

    @Test
    fun readMessageCanBeArchived() = runBlocking {
        val repository = InMemoryNotificationRepository(messages = listOf(message(status = NotificationStatus.Read)))
        val useCase = ChangeMessageStatusUseCase(repository)

        val result = useCase(messageId = "message-1", targetStatus = NotificationStatus.Archived)

        val updated = assertIs<AppResult.Success<NotificationMessage>>(result).value
        assertEquals(NotificationStatus.Archived, updated.status)
        assertEquals(NotificationStatus.Archived, repository.messages.first().single().status)
    }

    @Test
    fun archivedMessageCannotBeMarkedRead() = runBlocking {
        val repository = InMemoryNotificationRepository(messages = listOf(message(status = NotificationStatus.Archived)))
        val useCase = ChangeMessageStatusUseCase(repository)

        val result = useCase(messageId = "message-1", targetStatus = NotificationStatus.Read)

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertEquals(NotificationStatus.Archived, repository.messages.first().single().status)
    }

    @Test
    fun unknownMessageIdReturnsValidationFailure() = runBlocking {
        val repository = InMemoryNotificationRepository(messages = listOf(message(status = NotificationStatus.Unread)))
        val useCase = ChangeMessageStatusUseCase(repository)

        val result = useCase(messageId = "unknown-message", targetStatus = NotificationStatus.Read)

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
        assertEquals(NotificationStatus.Unread, repository.messages.first().single().status)
    }

    private fun message(status: NotificationStatus) = NotificationMessage(
        id = "message-1",
        title = "Lab result ready",
        body = "Your latest LifeLab result is available.",
        category = "Results",
        createdAtLabel = "Today",
        status = status,
    )
}
