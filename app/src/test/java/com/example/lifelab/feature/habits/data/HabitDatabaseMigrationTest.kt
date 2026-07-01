package com.example.lifelab.feature.habits.data

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lifelab.core.database.LifeLabDatabase
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitDatabaseMigrationTest {

    @Test
    fun migrationThreeToFourAddsAlarmClockColumn() {
        val executedSql = mutableListOf<String>()
        val database = Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java),
        ) { _, method, args ->
            if (method.name == "execSQL" && args?.firstOrNull() is String) {
                executedSql += args.first() as String
            }
            null
        } as SupportSQLiteDatabase

        LifeLabDatabase.MIGRATION_3_4.migrate(database)

        assertEquals(
            listOf(
                "ALTER TABLE habits ADD COLUMN reminder_alarm_clock_enabled INTEGER NOT NULL DEFAULT 0",
            ),
            executedSql,
        )
    }

    @Test
    fun databaseVersionFourMigrationIsRegistered() {
        val database = readMainText("java/com/example/lifelab/core/database/LifeLabDatabase.kt")
        val module = readMainText("java/com/example/lifelab/app/di/AppModule.kt")

        assertTrue(database.contains("version = 4"), "Room version must include habit alarm mode migration.")
        assertTrue(database.contains("MIGRATION_3_4"), "Habit alarm mode migration must stay in the database.")
        assertTrue(module.contains("LifeLabDatabase.MIGRATION_3_4"), "Room builder must register the habit alarm mode migration.")
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
}
