package com.example.lifelab.core.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

enum class AndroidNotificationPermissionStatus {
    NotRequired,
    Granted,
    Blocked,
    DisabledInSystemSettings,
}

val AndroidNotificationPermissionStatus.canPostNotifications: Boolean
    get() = this == AndroidNotificationPermissionStatus.NotRequired ||
        this == AndroidNotificationPermissionStatus.Granted

class AndroidNotificationPermissionStatusReader {
    private val context: Context?
    private val fixedStatus: AndroidNotificationPermissionStatus?

    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        this.context = context
        this.fixedStatus = null
    }

    constructor(fixedStatus: AndroidNotificationPermissionStatus) {
        this.context = null
        this.fixedStatus = fixedStatus
    }

    fun currentStatus(): AndroidNotificationPermissionStatus =
        fixedStatus ?: requireNotNull(context).androidNotificationPermissionStatus()
}

fun Context.androidNotificationPermissionStatus(): AndroidNotificationPermissionStatus =
    if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        AndroidNotificationPermissionStatus.Blocked
    } else if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
        AndroidNotificationPermissionStatus.DisabledInSystemSettings
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        AndroidNotificationPermissionStatus.NotRequired
    } else {
        AndroidNotificationPermissionStatus.Granted
    }
