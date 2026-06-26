package com.example.lifelab.feature.discover.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lifelab.feature.discover.domain.DiscoverContent

@Entity(tableName = "discover_content")
data class DiscoverContentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val title: String,
    val summary: String,
    val tag: String,
    val author: String?,
    val instructor: String?,
    val duration: String?,
    @ColumnInfo(name = "price_label") val priceLabel: String?,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
)

enum class DiscoverContentType {
    Article,
    Course,
    Product,
    Membership,
}

fun DiscoverContentEntity.toDomain(): DiscoverContent =
    when (DiscoverContentType.valueOf(type)) {
        DiscoverContentType.Article -> DiscoverContent.Article(
            id = id,
            title = title,
            summary = summary,
            author = author.orEmpty(),
            tag = tag,
        )

        DiscoverContentType.Course -> DiscoverContent.Course(
            id = id,
            title = title,
            summary = summary,
            instructor = instructor.orEmpty(),
            duration = duration.orEmpty(),
            tag = tag,
        )

        DiscoverContentType.Product -> DiscoverContent.Offer.Product(
            id = id,
            title = title,
            summary = summary,
            priceLabel = priceLabel.orEmpty(),
            tag = tag,
        )

        DiscoverContentType.Membership -> DiscoverContent.Offer.Membership(
            id = id,
            title = title,
            summary = summary,
            priceLabel = priceLabel.orEmpty(),
            tag = tag,
        )
    }

fun DiscoverContent.toEntity(sortOrder: Int): DiscoverContentEntity =
    when (this) {
        is DiscoverContent.Article -> DiscoverContentEntity(
            id = id,
            type = DiscoverContentType.Article.name,
            title = title,
            summary = summary,
            tag = tag,
            author = author,
            instructor = null,
            duration = null,
            priceLabel = null,
            sortOrder = sortOrder,
        )

        is DiscoverContent.Course -> DiscoverContentEntity(
            id = id,
            type = DiscoverContentType.Course.name,
            title = title,
            summary = summary,
            tag = tag,
            author = null,
            instructor = instructor,
            duration = duration,
            priceLabel = null,
            sortOrder = sortOrder,
        )

        is DiscoverContent.Offer.Product -> DiscoverContentEntity(
            id = id,
            type = DiscoverContentType.Product.name,
            title = title,
            summary = summary,
            tag = tag,
            author = null,
            instructor = null,
            duration = null,
            priceLabel = priceLabel,
            sortOrder = sortOrder,
        )

        is DiscoverContent.Offer.Membership -> DiscoverContentEntity(
            id = id,
            type = DiscoverContentType.Membership.name,
            title = title,
            summary = summary,
            tag = tag,
            author = null,
            instructor = null,
            duration = null,
            priceLabel = priceLabel,
            sortOrder = sortOrder,
        )
    }
