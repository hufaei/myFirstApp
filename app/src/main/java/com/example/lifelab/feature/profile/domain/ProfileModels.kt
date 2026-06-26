package com.example.lifelab.feature.profile.domain

import com.example.lifelab.core.datastore.ThemeMode

data class ProfileUser(
    val id: String,
    val displayName: String,
    val email: String?,
    val membershipLabel: String,
    val avatarInitial: String,
)

data class UserPreference(
    val themeMode: ThemeMode = ThemeMode.System,
    val notificationEnabled: Boolean = true,
    val defaultTaskFilter: DefaultTaskFilter = DefaultTaskFilter.All,
    val contentInterestTags: List<String> = emptyList(),
)

enum class DefaultTaskFilter {
    All,
    Active,
    Completed,
}

sealed interface ProfileSession {
    data object Guest : ProfileSession

    data class SignedIn(
        val user: ProfileUser,
    ) : ProfileSession
}

data class ProfileOverview(
    val displayName: String,
    val description: String,
    val email: String?,
    val membershipLabel: String?,
    val avatarInitial: String,
)

data class ProfileState(
    val session: ProfileSession,
    val preference: UserPreference,
)

fun ProfileSession.toOverview(): ProfileOverview =
    when (this) {
        ProfileSession.Guest -> ProfileOverview(
            displayName = "访客",
            description = "登录后可同步你的 LifeLab 资料。",
            email = null,
            membershipLabel = null,
            avatarInitial = "访",
        )

        is ProfileSession.SignedIn -> ProfileOverview(
            displayName = user.displayName,
            description = user.membershipLabel,
            email = user.email,
            membershipLabel = user.membershipLabel,
            avatarInitial = user.avatarInitial,
        )
    }
