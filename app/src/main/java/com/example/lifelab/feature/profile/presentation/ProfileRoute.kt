package com.example.lifelab.feature.profile.presentation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifelab.R
import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.AppPreferencesRepository
import com.example.lifelab.core.datastore.DataStoreAppPreferencesRepository
import com.example.lifelab.core.datastore.LanguageMode
import com.example.lifelab.core.datastore.ThemeMode
import com.example.lifelab.core.datastore.appPreferencesDataStore
import com.example.lifelab.core.ui.components.LifeLabPrimaryActionRow
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.feature.profile.domain.DefaultTaskFilter
import com.example.lifelab.feature.profile.domain.ProfileOverview
import com.example.lifelab.feature.profile.domain.UserPreference

@Composable
fun ProfileRoute(
    contentPadding: PaddingValues,
    onOpenNotifications: () -> Unit = {},
    onOpenWebLab: () -> Unit = {},
    viewModel: ProfileViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: rememberProfileViewModel()
    val uiState by resolvedViewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onEvent = resolvedViewModel::onEvent,
        onOpenNotifications = onOpenNotifications,
        onOpenWebLab = onOpenWebLab,
    )
}

@Composable
private fun rememberProfileViewModel(): ProfileViewModel {
    val application = LocalContext.current.applicationContext as Application
    val appPreferencesRepository = remember(application) {
        DataStoreAppPreferencesRepository(application.appPreferencesDataStore)
    }
    val factory = remember(appPreferencesRepository) {
        ProfileViewModelFactory(appPreferencesRepository)
    }
    return viewModel(factory = factory)
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    contentPadding: PaddingValues,
    onEvent: (ProfileUiEvent) -> Unit,
    onOpenNotifications: () -> Unit = {},
    onOpenWebLab: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LifeLabScreenHeader(
            title = stringResource(R.string.profile_title),
            subtitle = stringResource(R.string.profile_subtitle),
        )

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        AccountHeader(overview = uiState.overview)
        PreferencesCard(
            preference = uiState.preference,
            appPreferences = uiState.appPreferences,
            onEvent = onEvent,
            onOpenNotifications = onOpenNotifications,
            onOpenWebLab = onOpenWebLab,
        )
    }
}

@Composable
private fun AccountHeader(
    overview: ProfileOverview,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = overview.avatarInitial.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = overview.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = overview.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                overview.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferencesCard(
    preference: UserPreference,
    appPreferences: AppPreferences,
    onEvent: (ProfileUiEvent) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenWebLab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SettingsSectionCard(title = stringResource(R.string.profile_section_appearance)) {
            SettingGroup(title = stringResource(R.string.profile_theme_mode)) {
                ChoiceChipFlow {
                    ThemeMode.entries.forEach { themeMode ->
                        FilterChip(
                            modifier = Modifier.defaultMinSize(
                                minWidth = SettingChipMinWidth,
                                minHeight = SettingChipMinHeight,
                            ),
                            selected = appPreferences.themeMode == themeMode,
                            onClick = {
                                onEvent(ProfileUiEvent.ThemeModeSelected(themeMode))
                            },
                            label = {
                                Text(
                                    text = themeMode.label(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }

        SettingsSectionCard(title = stringResource(R.string.profile_section_language)) {
            SettingGroup(title = stringResource(R.string.profile_language_mode)) {
                ChoiceChipFlow {
                    LanguageMode.entries.forEach { languageMode ->
                        FilterChip(
                            modifier = Modifier.defaultMinSize(
                                minWidth = SettingChipMinWidth,
                                minHeight = SettingChipMinHeight,
                            ),
                            selected = appPreferences.languageMode == languageMode,
                            onClick = {
                                onEvent(ProfileUiEvent.LanguageModeSelected(languageMode))
                            },
                            label = {
                                Text(
                                    text = languageMode.label(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }
        }

        SettingsSectionCard(title = stringResource(R.string.profile_section_notifications)) {
            SettingSwitchRow(
                title = stringResource(R.string.profile_notifications),
                description = stringResource(R.string.profile_notifications_description),
                checked = preference.notificationEnabled,
                onCheckedChange = {
                    onEvent(ProfileUiEvent.NotificationEnabledChanged(it))
                },
            )

            SettingInfoRow(
                title = stringResource(R.string.profile_notification_settings),
                description = stringResource(R.string.profile_notification_settings_description),
            )
            LifeLabPrimaryActionRow(
                primaryLabel = stringResource(R.string.profile_open_notifications),
                onPrimaryClick = onOpenNotifications,
                primaryIcon = Icons.Filled.Notifications,
            )
        }

        SettingsSectionCard(title = stringResource(R.string.profile_section_task_preferences)) {
            SettingGroup(title = stringResource(R.string.profile_default_task_filter)) {
                ChoiceChipFlow {
                    DefaultTaskFilter.entries.forEach { defaultTaskFilter ->
                        FilterChip(
                            modifier = Modifier.defaultMinSize(
                                minWidth = SettingChipMinWidth,
                                minHeight = SettingChipMinHeight,
                            ),
                            selected = preference.defaultTaskFilter == defaultTaskFilter,
                            onClick = {
                                onEvent(
                                    ProfileUiEvent.DefaultTaskFilterSelected(defaultTaskFilter),
                                )
                            },
                            label = {
                                Text(
                                    text = defaultTaskFilter.label(),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
            }

            SettingInfoRow(
                title = stringResource(R.string.profile_interest_tags),
                description = if (preference.contentInterestTags.isEmpty()) {
                    stringResource(R.string.profile_interest_tags_empty)
                } else {
                    preference.contentInterestTags.joinToString(separator = ", ")
                },
            )
        }

        SettingsSectionCard(title = stringResource(R.string.profile_lab_title)) {
            Text(
                text = stringResource(R.string.profile_lab_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LifeLabPrimaryActionRow(
                primaryLabel = stringResource(R.string.profile_open_lab),
                onPrimaryClick = onOpenWebLab,
                primaryIcon = Icons.Filled.Science,
            )
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun SettingInfoRow(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingInfoRow(
            title = title,
            description = description,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceChipFlow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun ThemeMode.label(): String =
    when (this) {
        ThemeMode.System -> stringResource(R.string.profile_theme_system)
        ThemeMode.Light -> stringResource(R.string.profile_theme_light)
        ThemeMode.Dark -> stringResource(R.string.profile_theme_dark)
    }

@Composable
private fun LanguageMode.label(): String =
    when (this) {
        LanguageMode.System -> stringResource(R.string.profile_language_system)
        LanguageMode.Zh -> stringResource(R.string.profile_language_zh)
        LanguageMode.En -> stringResource(R.string.profile_language_en)
    }

@Composable
private fun DefaultTaskFilter.label(): String =
    when (this) {
        DefaultTaskFilter.All -> stringResource(R.string.profile_task_filter_all)
        DefaultTaskFilter.Active -> stringResource(R.string.profile_task_filter_active)
        DefaultTaskFilter.Completed -> stringResource(R.string.profile_task_filter_completed)
    }

private class ProfileViewModelFactory(
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                appPreferencesRepository = appPreferencesRepository,
            ) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}

private val SettingChipMinWidth = 112.dp
private val SettingChipMinHeight = 40.dp
