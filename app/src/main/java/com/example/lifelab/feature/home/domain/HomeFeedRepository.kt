package com.example.lifelab.feature.home.domain

import com.example.lifelab.core.common.AppResult

interface HomeFeedRepository {
    suspend fun loadHomeFeedSeed(): AppResult<HomeFeedSeed>
}
