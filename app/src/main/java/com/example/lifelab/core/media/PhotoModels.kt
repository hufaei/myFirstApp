package com.example.lifelab.core.media

data class PhotoOwner(
    val type: PhotoOwnerType,
    val id: String,
)

enum class PhotoOwnerType(
    val storageSegment: String,
) {
    Task("task"),
    Habit("habit"),
    Note("note"),
    Journal("journal"),
}

data class PhotoRecord(
    val id: String,
    val owner: PhotoOwner,
    val localUri: String,
    val source: PhotoSource,
    val sortOrder: Int,
    val createdAtMillis: Long,
)

enum class PhotoSource {
    Picker,
    Camera,
}
