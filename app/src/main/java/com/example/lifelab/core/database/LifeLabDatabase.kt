package com.example.lifelab.core.database

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lifelab.core.media.PhotoRecordDao
import com.example.lifelab.core.media.PhotoRecordEntity
import com.example.lifelab.feature.discover.data.DiscoverContentEntity
import com.example.lifelab.feature.discover.data.DiscoverDao
import com.example.lifelab.feature.habits.data.HabitDao
import com.example.lifelab.feature.habits.data.HabitEntity
import com.example.lifelab.feature.notifications.data.NotificationDao
import com.example.lifelab.feature.notifications.data.NotificationMessageEntity
import com.example.lifelab.feature.notifications.data.NotificationSettingsEntity
import com.example.lifelab.feature.search.data.HotKeywordEntity
import com.example.lifelab.feature.search.data.SearchDao
import com.example.lifelab.feature.search.data.SearchHistoryEntity
import com.example.lifelab.feature.search.data.SearchResultEntity
import com.example.lifelab.feature.tasks.data.TaskDao
import com.example.lifelab.feature.tasks.data.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        NotificationMessageEntity::class,
        NotificationSettingsEntity::class,
        SearchHistoryEntity::class,
        HotKeywordEntity::class,
        SearchResultEntity::class,
        DiscoverContentEntity::class,
        PhotoRecordEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class LifeLabDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun notificationDao(): NotificationDao
    abstract fun searchDao(): SearchDao
    abstract fun discoverDao(): DiscoverDao
    abstract fun photoRecordDao(): PhotoRecordDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                localizeTaskSeeds(database)
                localizeHabitSeeds(database)
                localizeDiscoverSeeds(database)
                localizeSearchSeeds(database)
                localizeNotificationSeeds(database)
            }
        }

        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE habits ADD COLUMN reminder_priority TEXT NOT NULL DEFAULT 'Normal'",
                )
            }
        }
    }
}

private fun localizeTaskSeeds(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        UPDATE tasks
        SET title = '规划每周复盘',
            description = '选出本周最重要的三个 LifeLab 优先事项。',
            tags = '计划' || char(31) || '居家'
        WHERE id = 'task-1' AND title = 'Plan the weekly reset'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE tasks
        SET title = '预约健康检查',
            description = '确认可预约时间和所需材料。',
            tags = '健康'
        WHERE id = 'task-2' AND title = 'Book health checkup'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE tasks
        SET title = '归档已完成票据',
            description = '把扫描票据整理到财务文件夹。',
            tags = '财务' || char(31) || '整理'
        WHERE id = 'task-3' AND title = 'Archive completed receipts'
        """.trimIndent(),
    )
}

private fun localizeHabitSeeds(database: SupportSQLiteDatabase) {
    database.execSQL("UPDATE habits SET name = '喝水' WHERE id = 'hydrate' AND name = 'Drink water'")
    database.execSQL("UPDATE habits SET name = '晚间散步' WHERE id = 'walk' AND name = 'Evening walk'")
}

private fun localizeDiscoverSeeds(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        UPDATE discover_content
        SET title = '设计更好的早晨',
            summary = '把日常流程变成可衡量成长实验的实用指南。',
            author = 'LifeLab 编辑部',
            tag = '专注'
        WHERE id = 'article-morning-systems' AND title = 'Design a better morning'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE discover_content
        SET title = '专注重启冲刺',
            summary = '重建注意力区块、保护深度工作时间的紧凑课程。',
            instructor = '陈明雅',
            duration = '42 分钟',
            tag = '效率'
        WHERE id = 'course-focus-reset' AND title = 'Focus Reset Sprint'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE discover_content
        SET title = '习惯实验手账',
            summary = '一本用于规划每周实验、回顾进展的引导式笔记本。',
            price_label = '¥18',
            tag = '工具'
        WHERE id = 'product-habit-journal' AND title = 'Habit Experiment Journal'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE discover_content
        SET title = 'LifeLab 会员',
            summary = '每月实验室、专家场和高级模板，帮助你打磨个人系统。',
            price_label = '¥9/月',
            tag = '会员'
        WHERE id = 'membership-lifelab-plus' AND title = 'LifeLab Plus'
        """.trimIndent(),
    )
}

private fun localizeSearchSeeds(database: SupportSQLiteDatabase) {
    database.execSQL("UPDATE hot_keywords SET keyword = '专注' WHERE keyword = 'focus'")
    database.execSQL("UPDATE hot_keywords SET keyword = '每周计划' WHERE keyword = 'weekly plan'")
    database.execSQL("UPDATE hot_keywords SET keyword = '习惯连续' WHERE keyword = 'habit streak'")
    database.execSQL("UPDATE hot_keywords SET keyword = '学习权益' WHERE keyword = 'learning offer'")
    database.execSQL(
        """
        UPDATE search_results
        SET title = '深度专注重启',
            summary = '关于在高价值学习时段保护注意力的文章。'
        WHERE id = 'article-deep-focus' AND title = 'Deep Focus Reset'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE search_results
        SET title = '学习冲刺权益',
            summary = '一套引导式内容包，帮助你规划 7 天专注技能冲刺。'
        WHERE id = 'offer-learning-sprint' AND title = 'Learning Sprint Offer'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE search_results
        SET title = '每周计划任务',
            summary = '把目标拆成清晰计划，并设置复盘节点。'
        WHERE id = 'task-weekly-plan' AND title = 'Weekly Planning Task'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE search_results
        SET title = '晚间复盘习惯',
            summary = '通过回顾专注、任务和精力，建立连续习惯。'
        WHERE id = 'habit-evening-review' AND title = 'Evening Review Habit'
        """.trimIndent(),
    )
}

private fun localizeNotificationSeeds(database: SupportSQLiteDatabase) {
    database.execSQL(
        """
        UPDATE notification_messages
        SET title = '欢迎使用 LifeLab',
            body = '你的通知中心已准备就绪。',
            category = '账号',
            created_at_label = '今天'
        WHERE id = 'welcome' AND title = 'Welcome to LifeLab'
        """.trimIndent(),
    )
    database.execSQL(
        """
        UPDATE notification_messages
        SET title = '每周健康摘要',
            body = '你的 LifeLab 每周摘要已经生成。',
            category = '摘要',
            created_at_label = '昨天'
        WHERE id = 'weekly-summary' AND title = 'Weekly health summary'
        """.trimIndent(),
    )
}
