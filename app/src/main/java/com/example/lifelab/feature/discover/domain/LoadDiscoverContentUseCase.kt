package com.example.lifelab.feature.discover.domain

import com.example.lifelab.core.common.AppResult
import javax.inject.Inject

class LoadDiscoverContentUseCase @Inject constructor(
    private val repository: DiscoverRepository,
) {
    suspend operator fun invoke(
        category: DiscoverCategory = DiscoverCategory.All,
    ): AppResult<List<DiscoverContent>> =
        when (val result = repository.getContent()) {
            is AppResult.Failure -> result
            is AppResult.Success -> AppResult.Success(
                result.value.filter { content -> category.matches(content) },
            )
        }

    private fun DiscoverCategory.matches(content: DiscoverContent): Boolean =
        when (this) {
            DiscoverCategory.All -> true
            DiscoverCategory.Articles -> content is DiscoverContent.Article
            DiscoverCategory.Courses -> content is DiscoverContent.Course
            DiscoverCategory.Offers -> content is DiscoverContent.Offer
            DiscoverCategory.Membership -> content is DiscoverContent.Offer.Membership
        }
}
