package com.example.lifelab.core.media

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PhotoRecordRepository {
    fun observePhotoRecords(owner: PhotoOwner): Flow<List<PhotoRecord>>

    suspend fun addPhotoRecord(
        owner: PhotoOwner,
        localUri: String,
        source: PhotoSource,
        createdAtMillis: Long,
    ): AppResult<PhotoRecord>

    suspend fun deletePhotoRecord(id: String)

    suspend fun deletePhotoRecordsForOwner(owner: PhotoOwner)
}

class RoomPhotoRecordRepository(
    private val photoRecordDao: PhotoRecordDao,
    private val policy: PhotoAttachmentPolicy = PhotoAttachmentPolicy(),
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) : PhotoRecordRepository {

    override fun observePhotoRecords(owner: PhotoOwner): Flow<List<PhotoRecord>> =
        photoRecordDao.observePhotoRecords(
            ownerType = owner.type.name,
            ownerId = owner.id,
        ).map { records -> records.map(PhotoRecordEntity::toDomain) }

    override suspend fun addPhotoRecord(
        owner: PhotoOwner,
        localUri: String,
        source: PhotoSource,
        createdAtMillis: Long,
    ): AppResult<PhotoRecord> {
        val existingRecords = photoRecordDao.getPhotoRecords(
            ownerType = owner.type.name,
            ownerId = owner.id,
        ).map(PhotoRecordEntity::toDomain)
        if (!policy.canAttach(owner, existingRecords)) {
            return AppResult.Failure(
                AppError.Validation("每个项目最多保留 3 张照片。"),
            )
        }

        val record = PhotoRecord(
            id = idGenerator(),
            owner = owner,
            localUri = localUri,
            source = source,
            sortOrder = existingRecords.size,
            createdAtMillis = createdAtMillis,
        )
        photoRecordDao.upsertPhotoRecord(record.toEntity())
        return AppResult.Success(record)
    }

    override suspend fun deletePhotoRecord(id: String) {
        photoRecordDao.deletePhotoRecord(id)
    }

    override suspend fun deletePhotoRecordsForOwner(owner: PhotoOwner) {
        photoRecordDao.deletePhotoRecordsForOwner(
            ownerType = owner.type.name,
            ownerId = owner.id,
        )
    }
}
