package com.example.lifelab.feature.discover.domain

sealed interface DiscoverCategory {
    data object All : DiscoverCategory
    data object Articles : DiscoverCategory
    data object Courses : DiscoverCategory
    data object Offers : DiscoverCategory
    data object Membership : DiscoverCategory
}
