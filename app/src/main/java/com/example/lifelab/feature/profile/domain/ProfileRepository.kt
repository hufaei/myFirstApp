package com.example.lifelab.feature.profile.domain

import com.example.lifelab.core.datastore.ThemeMode

interface ProfileRepository {

    fun getProfileState(): ProfileState

    fun updateThemeMode(themeMode: ThemeMode): ProfileState

    fun updateNotificationEnabled(enabled: Boolean): ProfileState

    fun updateDefaultTaskFilter(defaultTaskFilter: DefaultTaskFilter): ProfileState

    fun updateContentInterestTags(tags: List<String>): ProfileState
}
