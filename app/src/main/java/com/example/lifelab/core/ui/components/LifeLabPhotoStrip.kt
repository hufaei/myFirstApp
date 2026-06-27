package com.example.lifelab.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lifelab.R
import com.example.lifelab.core.media.PhotoAttachmentActions
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoFileStorage
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.core.media.copyPhotosToAppStorage
import com.example.lifelab.core.media.toLifeLabFileProviderUri

@Composable
fun LifeLabPhotoStrip(
    owner: PhotoOwner,
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visiblePhotos = photos.take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER)
    val policy = remember { PhotoAttachmentPolicy() }
    val context = LocalContext.current
    val remainingSlots = policy.remainingSlots(owner, visiblePhotos)
    val cameraCaptureUri = remember(owner, remainingSlots) {
        if (remainingSlots <= 0) {
            null
        } else {
            PhotoFileStorage(
                filesDir = context.filesDir,
                cacheDir = context.cacheDir,
            ).createCameraCaptureFile(
                owner = owner,
                createdAtMillis = System.currentTimeMillis(),
            ).toLifeLabFileProviderUri(context)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.photo_section_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.photo_remaining_count, remainingSlots),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PhotoAttachmentActions(
                remainingSlots = remainingSlots,
                cameraCaptureUri = cameraCaptureUri,
                pickerLabel = stringResource(R.string.photo_add_from_album),
                cameraLabel = stringResource(R.string.photo_take_photo),
                onPickerPhotosSelected = { uris ->
                    val storedUris = context.copyPhotosToAppStorage(
                        owner = owner,
                        uris = uris.take(remainingSlots),
                        startSequence = visiblePhotos.size,
                        createdAtMillis = System.currentTimeMillis(),
                    )
                    onAttachPhotos(storedUris.map { it.toString() }, PhotoSource.Picker)
                },
                onCameraPhotoCaptured = { uri ->
                    onAttachPhotos(listOf(uri.toString()), PhotoSource.Camera)
                },
            )
        }

        if (visiblePhotos.isEmpty()) {
            Text(
                text = stringResource(R.string.photo_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                visiblePhotos.forEach { photo ->
                    AsyncImage(
                        model = photo.localUri,
                        contentDescription = stringResource(R.string.photo_preview_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}
