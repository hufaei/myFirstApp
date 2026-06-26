package com.example.lifelab.core.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PhotoAttachmentPolicyTest {

    @Test
    fun ownerCanAttachUpToThreePhotos() {
        val policy = PhotoAttachmentPolicy()
        val owner = PhotoOwner(type = PhotoOwnerType.Task, id = "task-42")

        assertTrue(policy.canAttach(owner, emptyList()))
        assertEquals(3, policy.remainingSlots(owner, emptyList()))

        val existing = listOf(
            record(owner = owner, sortOrder = 0),
            record(owner = owner, sortOrder = 1),
            record(owner = owner, sortOrder = 2),
        )

        assertFalse(policy.canAttach(owner, existing))
        assertEquals(0, policy.remainingSlots(owner, existing))
    }

    @Test
    fun remainingSlotsOnlyCountsPhotosForTheSameOwner() {
        val policy = PhotoAttachmentPolicy()
        val targetOwner = PhotoOwner(type = PhotoOwnerType.Habit, id = "habit-1")
        val otherOwner = PhotoOwner(type = PhotoOwnerType.Habit, id = "habit-2")
        val existing = listOf(
            record(owner = targetOwner, sortOrder = 0),
            record(owner = otherOwner, sortOrder = 0),
            record(owner = otherOwner, sortOrder = 1),
        )

        assertEquals(2, policy.remainingSlots(targetOwner, existing))
        assertTrue(policy.canAttach(targetOwner, existing))
    }

    @Test
    fun trimToAvailableSlotsKeepsInputOrderAndDoesNotOverfillOwner() {
        val policy = PhotoAttachmentPolicy()
        val owner = PhotoOwner(type = PhotoOwnerType.Note, id = "note-1")
        val candidates = listOf("uri-a", "uri-b", "uri-c")
        val existing = listOf(record(owner = owner, sortOrder = 0))

        assertEquals(listOf("uri-a", "uri-b"), policy.trimToAvailableSlots(owner, existing, candidates))
    }

    private fun record(
        owner: PhotoOwner,
        sortOrder: Int,
    ) = PhotoRecord(
        id = "photo-${owner.id}-$sortOrder",
        owner = owner,
        localUri = "content://lifelab/photo-${owner.id}-$sortOrder",
        source = PhotoSource.Picker,
        sortOrder = sortOrder,
        createdAtMillis = 1_720_000_000_000,
    )
}
