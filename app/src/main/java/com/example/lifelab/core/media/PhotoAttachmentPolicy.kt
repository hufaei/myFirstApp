package com.example.lifelab.core.media

class PhotoAttachmentPolicy(
    private val maxPhotosPerOwner: Int = MAX_PHOTOS_PER_OWNER,
) {
    fun canAttach(
        owner: PhotoOwner,
        existingRecords: List<PhotoRecord>,
    ): Boolean = remainingSlots(owner, existingRecords) > 0

    fun remainingSlots(
        owner: PhotoOwner,
        existingRecords: List<PhotoRecord>,
    ): Int {
        val currentCount = existingRecords.count { it.owner == owner }
        return (maxPhotosPerOwner - currentCount).coerceAtLeast(0)
    }

    fun <T> trimToAvailableSlots(
        owner: PhotoOwner,
        existingRecords: List<PhotoRecord>,
        candidates: List<T>,
    ): List<T> = candidates.take(remainingSlots(owner, existingRecords))

    companion object {
        const val MAX_PHOTOS_PER_OWNER = 3
    }
}
