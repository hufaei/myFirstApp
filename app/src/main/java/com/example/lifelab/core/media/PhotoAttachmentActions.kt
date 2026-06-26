package com.example.lifelab.core.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PhotoAttachmentActions(
    remainingSlots: Int,
    cameraCaptureUri: Uri?,
    pickerLabel: String,
    cameraLabel: String,
    onPickerPhotosSelected: (List<Uri>) -> Unit,
    onCameraPhotoCaptured: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER,
        ),
        onResult = { uris -> onPickerPhotosSelected(uris.take(remainingSlots)) },
    )
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { captured ->
            if (captured && cameraCaptureUri != null) {
                onCameraPhotoCaptured(cameraCaptureUri)
            }
        },
    )
    val canAttach = remainingSlots > 0

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PhotoActionButton(
            tooltip = pickerLabel,
            enabled = canAttach,
            onClick = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            },
        ) {
            Icon(
                imageVector = Icons.Filled.AddPhotoAlternate,
                contentDescription = pickerLabel,
            )
        }
        PhotoActionButton(
            tooltip = cameraLabel,
            enabled = canAttach && cameraCaptureUri != null,
            onClick = {
                cameraCaptureUri?.let(cameraLauncher::launch)
            },
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = cameraLabel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoActionButton(
    tooltip: String,
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(text = tooltip)
            }
        },
        state = rememberTooltipState(),
    ) {
        IconButton(
            enabled = enabled,
            onClick = onClick,
        ) {
            icon()
        }
    }
}
