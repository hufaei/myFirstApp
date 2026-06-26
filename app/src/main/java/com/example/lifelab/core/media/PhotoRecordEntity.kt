package com.example.lifelab.core.media

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_records")
data class PhotoRecordEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "owner_type") val ownerType: String,
    @ColumnInfo(name = "owner_id") val ownerId: String,
    @ColumnInfo(name = "local_uri") val localUri: String,
    val source: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at_millis") val createdAtMillis: Long,
)

fun PhotoRecordEntity.toDomain(): PhotoRecord =
    PhotoRecord(
        id = id,
        owner = PhotoOwner(
            type = PhotoOwnerType.valueOf(ownerType),
            id = ownerId,
        ),
        localUri = localUri,
        source = PhotoSource.valueOf(source),
        sortOrder = sortOrder,
        createdAtMillis = createdAtMillis,
    )

fun PhotoRecord.toEntity(): PhotoRecordEntity =
    PhotoRecordEntity(
        id = id,
        ownerType = owner.type.name,
        ownerId = owner.id,
        localUri = localUri,
        source = source.name,
        sortOrder = sortOrder,
        createdAtMillis = createdAtMillis,
    )
