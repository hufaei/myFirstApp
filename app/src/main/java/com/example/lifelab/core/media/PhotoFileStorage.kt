package com.example.lifelab.core.media

import java.io.File
import java.util.Locale

class PhotoFileStorage(
    val filesDir: File,
    val cacheDir: File,
) {
    fun createPhotoFile(
        owner: PhotoOwner,
        createdAtMillis: Long,
        sequence: Int,
    ): File {
        val ownerPath = owner.storagePath()
        val directory = File(filesDir, "photos/$ownerPath")
        directory.mkdirs()
        val fileName = buildFileName(
            owner = owner,
            createdAtMillis = createdAtMillis,
            suffix = sequence.toString().padStart(2, '0'),
        )
        return File(directory, fileName)
    }

    fun createCameraCaptureFile(
        owner: PhotoOwner,
        createdAtMillis: Long,
    ): File {
        val ownerPath = owner.storagePath()
        val directory = File(filesDir, "photos/$ownerPath")
        directory.mkdirs()
        return File(
            directory,
            buildFileName(
                owner = owner,
                createdAtMillis = createdAtMillis,
                suffix = "camera",
            ),
        )
    }

    private fun buildFileName(
        owner: PhotoOwner,
        createdAtMillis: Long,
        suffix: String,
    ): String {
        val ownerId = owner.id.toStorageSegment()
        return "lifelab_${owner.type.storageSegment}_${ownerId}_${createdAtMillis}_$suffix.jpg"
    }

    private fun PhotoOwner.storagePath(): String =
        "${type.storageSegment}/${id.toStorageSegment()}"
}

private fun String.toStorageSegment(): String {
    val normalized = trim()
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
    return normalized.ifBlank { "unknown" }
}
