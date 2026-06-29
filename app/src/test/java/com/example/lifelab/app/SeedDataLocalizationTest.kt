package com.example.lifelab.app

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeedDataLocalizationTest {

    @Test
    fun bundledSeedDataUsesChinesePrimaryCopy() {
        val source = seedSourceFiles.joinToString(separator = "\n") { path ->
            readMainText(path)
        }

        disallowedEnglishSeedPhrases.forEach { phrase ->
            assertFalse(
                source.contains(phrase),
                "Default seed/demo copy must use Chinese primary text, found: $phrase",
            )
        }
    }

    @Test
    fun databaseRegistersSeedLocalizationMigration() {
        val database = readMainText("java/com/example/lifelab/core/database/LifeLabDatabase.kt")
        val module = readMainText("java/com/example/lifelab/app/di/AppModule.kt")

        assertTrue(database.contains("version = 3"), "Room version must include seed localization and reminder migrations.")
        assertTrue(database.contains("MIGRATION_1_2"), "Seed data localization migration must stay registered in the database.")
        assertTrue(database.contains("MIGRATION_2_3"), "Reminder priority migration must stay registered in the database.")
        assertTrue(module.contains("LifeLabDatabase.MIGRATION_1_2"), "Room builder must register the localization migration.")
        assertTrue(module.contains("LifeLabDatabase.MIGRATION_2_3"), "Room builder must register the reminder priority migration.")
    }

    @Test
    fun bundledUserVisibleMessagesAvoidEnglishFallbackCopy() {
        val source = userVisibleMessageFiles.joinToString(separator = "\n") { path ->
            readMainText(path)
        }

        disallowedEnglishFallbackPhrases.forEach { phrase ->
            assertFalse(
                source.contains(phrase),
                "User-visible fallback copy must use Chinese primary text, found: $phrase",
            )
        }
    }

    private fun readMainText(path: String): String =
        String(Files.readAllBytes(mainSourcePath(path)), Charsets.UTF_8)

    private fun mainSourcePath(path: String): Path {
        val cwd = Path.of("").toAbsolutePath()
        val appRoot = generateSequence(cwd) { current -> current.parent }
            .first { current ->
                current.name == "app" && current.resolve("src/main/AndroidManifest.xml").exists() ||
                    current.resolve("app/src/main/AndroidManifest.xml").exists()
            }

        val mainRoot = if (appRoot.name == "app") appRoot.resolve("src/main") else appRoot.resolve("app/src/main")
        return mainRoot.resolve(path)
    }

    private companion object {
        val seedSourceFiles = listOf(
            "java/com/example/lifelab/feature/home/data/SeedHomeFeedRepository.kt",
            "java/com/example/lifelab/feature/profile/domain/ProfileModels.kt",
            "java/com/example/lifelab/feature/tasks/data/RoomTaskRepository.kt",
            "java/com/example/lifelab/feature/tasks/data/InMemoryTaskRepository.kt",
            "java/com/example/lifelab/feature/habits/data/RoomHabitRepository.kt",
            "java/com/example/lifelab/feature/habits/data/InMemoryHabitRepository.kt",
            "java/com/example/lifelab/feature/discover/data/RoomDiscoverRepository.kt",
            "java/com/example/lifelab/feature/discover/data/InMemoryDiscoverRepository.kt",
            "java/com/example/lifelab/feature/search/data/RoomSearchRepository.kt",
            "java/com/example/lifelab/feature/search/data/InMemorySearchRepository.kt",
            "java/com/example/lifelab/feature/notifications/data/RoomNotificationRepository.kt",
            "java/com/example/lifelab/feature/notifications/data/InMemoryNotificationRepository.kt",
        )

        val disallowedEnglishSeedPhrases = listOf(
            "Plan today's focus",
            "Plan the weekly reset",
            "Drink water",
            "Design a better morning",
            "Deep Focus Reset",
            "Welcome to LifeLab",
            "Weekly health summary",
            "Sign in to sync your LifeLab profile.",
        )

        val userVisibleMessageFiles = listOf(
            "java/com/example/lifelab/core/common/AppError.kt",
            "java/com/example/lifelab/core/media/PhotoRecordRepository.kt",
            "java/com/example/lifelab/feature/notifications/domain/ChangeMessageStatusUseCase.kt",
            "java/com/example/lifelab/feature/notifications/presentation/NotificationsUiState.kt",
            "java/com/example/lifelab/feature/notifications/presentation/NotificationsViewModel.kt",
        )

        val disallowedEnglishFallbackPhrases = listOf(
            "Unexpected error",
            "Each item can keep up to three photos.",
            "System notifications enabled",
            "System notifications disabled",
            "Notification messages cannot be moved back to unread.",
        )
    }
}
