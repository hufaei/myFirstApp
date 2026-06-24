package com.example.lifelab.feature.profile.data

import com.example.lifelab.feature.profile.domain.DefaultTaskFilter
import com.example.lifelab.feature.profile.domain.ProfileRepository
import com.example.lifelab.feature.profile.domain.ProfileSession
import com.example.lifelab.feature.profile.domain.ProfileState
import com.example.lifelab.feature.profile.domain.ThemeMode
import com.example.lifelab.feature.profile.domain.UserPreference

class InMemoryProfileRepository(
    initialSession: ProfileSession = ProfileSession.Guest,
    initialPreference: UserPreference = UserPreference(),
) : ProfileRepository {

    private var state = ProfileState(
        session = initialSession,
        preference = initialPreference.snapshot(),
    )

    override fun getProfileState(): ProfileState = state.snapshot()

    override fun updateThemeMode(themeMode: ThemeMode): ProfileState =
        updatePreference { preference ->
            preference.copy(themeMode = themeMode)
        }

    override fun updateNotificationEnabled(enabled: Boolean): ProfileState =
        updatePreference { preference ->
            preference.copy(notificationEnabled = enabled)
        }

    override fun updateDefaultTaskFilter(defaultTaskFilter: DefaultTaskFilter): ProfileState =
        updatePreference { preference ->
            preference.copy(defaultTaskFilter = defaultTaskFilter)
        }

    override fun updateContentInterestTags(tags: List<String>): ProfileState =
        updatePreference { preference ->
            preference.copy(contentInterestTags = tags.toList())
        }

    private fun updatePreference(
        transform: (UserPreference) -> UserPreference,
    ): ProfileState {
        state = state.copy(
            preference = transform(state.preference).snapshot(),
        )
        return state.snapshot()
    }
}

private fun ProfileState.snapshot(): ProfileState =
    copy(preference = preference.snapshot())

private fun UserPreference.snapshot(): UserPreference =
    copy(contentInterestTags = contentInterestTags.toList())
