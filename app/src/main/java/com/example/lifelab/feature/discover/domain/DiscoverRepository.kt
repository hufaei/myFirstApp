package com.example.lifelab.feature.discover.domain

import com.example.lifelab.core.common.AppResult

interface DiscoverRepository {
    suspend fun getContent(): AppResult<List<DiscoverContent>>
}
