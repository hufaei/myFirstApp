package com.example.lifelab.feature.profile.domain

interface ProfileRepository {

    fun getProfileState(): ProfileState

    fun updateThemeMode(themeMode: ThemeMode): ProfileState

    fun updateNotificationEnabled(enabled: Boolean): ProfileState

    fun updateDefaultTaskFilter(defaultTaskFilter: DefaultTaskFilter): ProfileState

    fun updateContentInterestTags(tags: List<String>): ProfileState
}
