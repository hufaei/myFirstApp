package com.example.lifelab.core.media

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.rules.TemporaryFolder
import org.junit.Rule

class PhotoFileStorageTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun createsStableAppSpecificPhotoFilePathWithSanitizedOwnerValues() {
        val storage = PhotoFileStorage(
            filesDir = temporaryFolder.newFolder("files"),
            cacheDir = temporaryFolder.newFolder("cache"),
        )
        val owner = PhotoOwner(type = PhotoOwnerType.Task, id = "Task 42/Today")

        val file = storage.createPhotoFile(
            owner = owner,
            createdAtMillis = 1_720_000_000_123,
            sequence = 2,
        )

        assertEquals(
            File(
                storage.filesDir,
                "photos/task/task-42-today/lifelab_task_task-42-today_1720000000123_02.jpg",
            ).canonicalPath,
            file.canonicalPath,
        )
        assertTrue(file.parentFile?.exists() == true)
    }

    @Test
    fun createsCameraCaptureFileInPhotoStorage() {
        val storage = PhotoFileStorage(
            filesDir = temporaryFolder.newFolder("files"),
            cacheDir = temporaryFolder.newFolder("cache"),
        )
        val owner = PhotoOwner(type = PhotoOwnerType.Journal, id = "morning")

        val file = storage.createCameraCaptureFile(
            owner = owner,
            createdAtMillis = 1_720_000_000_456,
        )

        assertEquals(
            File(
                storage.filesDir,
                "photos/journal/morning/lifelab_journal_morning_1720000000456_camera.jpg",
            ).canonicalPath,
            file.canonicalPath,
        )
        assertTrue(file.parentFile?.exists() == true)
    }
}
