package com.example.lifelab.core.media

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun File.toLifeLabFileProviderUri(context: Context): Uri =
    FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        this,
    )
