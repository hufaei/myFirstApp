package com.example.lifelab.core.media

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoRecordDao {
    @Query(
        """
        SELECT * FROM photo_records
        WHERE owner_type = :ownerType AND owner_id = :ownerId
        ORDER BY sort_order ASC, created_at_millis ASC
        """,
    )
    fun observePhotoRecords(
        ownerType: String,
        ownerId: String,
    ): Flow<List<PhotoRecordEntity>>

    @Query(
        """
        SELECT * FROM photo_records
        WHERE owner_type = :ownerType AND owner_id = :ownerId
        ORDER BY sort_order ASC, created_at_millis ASC
        """,
    )
    suspend fun getPhotoRecords(
        ownerType: String,
        ownerId: String,
    ): List<PhotoRecordEntity>

    @Upsert
    suspend fun upsertPhotoRecord(record: PhotoRecordEntity)

    @Query("DELETE FROM photo_records WHERE id = :id")
    suspend fun deletePhotoRecord(id: String)

    @Query("DELETE FROM photo_records WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun deletePhotoRecordsForOwner(
        ownerType: String,
        ownerId: String,
    )
}
