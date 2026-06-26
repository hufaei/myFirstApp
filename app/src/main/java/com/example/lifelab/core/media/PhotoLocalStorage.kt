package com.example.lifelab.core.media

import android.content.Context
import android.net.Uri

fun Context.copyPhotosToAppStorage(
    owner: PhotoOwner,
    uris: List<Uri>,
    startSequence: Int,
    createdAtMillis: Long,
): List<Uri> {
    val storage = PhotoFileStorage(
        filesDir = filesDir,
        cacheDir = cacheDir,
    )
    return uris.mapIndexedNotNull { index, uri ->
        val target = storage.createPhotoFile(
            owner = owner,
            createdAtMillis = createdAtMillis,
            sequence = startSequence + index,
        )
        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@mapIndexedNotNull null
            target.toLifeLabFileProviderUri(this)
        }.getOrNull()
    }
}
