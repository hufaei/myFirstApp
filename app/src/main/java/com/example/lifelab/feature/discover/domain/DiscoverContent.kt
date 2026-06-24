package com.example.lifelab.feature.discover.domain

sealed interface DiscoverContent {
    val id: String
    val title: String
    val summary: String
    val tag: String

    data class Article(
        override val id: String,
        override val title: String,
        override val summary: String,
        val author: String,
        override val tag: String,
    ) : DiscoverContent

    data class Course(
        override val id: String,
        override val title: String,
        override val summary: String,
        val instructor: String,
        val duration: String,
        override val tag: String,
    ) : DiscoverContent

    sealed interface Offer : DiscoverContent {
        val priceLabel: String

        data class Product(
            override val id: String,
            override val title: String,
            override val summary: String,
            override val priceLabel: String,
            override val tag: String,
        ) : Offer

        data class Membership(
            override val id: String,
            override val title: String,
            override val summary: String,
            override val priceLabel: String,
            override val tag: String,
        ) : Offer
    }
}
