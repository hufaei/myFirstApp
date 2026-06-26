package com.example.lifelab.feature.discover.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.domain.DiscoverContent
import com.example.lifelab.feature.discover.domain.DiscoverRepository

class RoomDiscoverRepository(
    private val discoverDao: DiscoverDao,
) : DiscoverRepository {

    override suspend fun getContent(): AppResult<List<DiscoverContent>> {
        seedIfEmpty()
        return AppResult.Success(discoverDao.getContent().map(DiscoverContentEntity::toDomain))
    }

    suspend fun seedIfEmpty() {
        if (discoverDao.countContent() == 0) {
            discoverDao.insertContent(
                seedContent().mapIndexed { index, content ->
                    content.toEntity(sortOrder = index)
                },
            )
        }
    }

    private companion object {
        fun seedContent(): List<DiscoverContent> =
            listOf(
                DiscoverContent.Article(
                    id = "article-morning-systems",
                    title = "设计更好的早晨",
                    summary = "把日常流程变成可衡量成长实验的实用指南。",
                    author = "LifeLab 编辑部",
                    tag = "专注",
                ),
                DiscoverContent.Course(
                    id = "course-focus-reset",
                    title = "专注重启冲刺",
                    summary = "重建注意力区块、保护深度工作时间的紧凑课程。",
                    instructor = "陈明雅",
                    duration = "42 分钟",
                    tag = "效率",
                ),
                DiscoverContent.Offer.Product(
                    id = "product-habit-journal",
                    title = "习惯实验手账",
                    summary = "一本用于规划每周实验、回顾进展的引导式笔记本。",
                    priceLabel = "¥18",
                    tag = "工具",
                ),
                DiscoverContent.Offer.Membership(
                    id = "membership-lifelab-plus",
                    title = "LifeLab 会员",
                    summary = "每月实验室、专家场和高级模板，帮助你打磨个人系统。",
                    priceLabel = "¥9/月",
                    tag = "会员",
                ),
            )
    }
}
