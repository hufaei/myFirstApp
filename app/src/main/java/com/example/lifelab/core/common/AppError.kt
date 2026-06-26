package com.example.lifelab.core.common

sealed interface AppError {
    val message: String
    val cause: Throwable?

    data class Network(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    data class Storage(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    data class Validation(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    data class Unknown(
        override val message: String = "发生未知错误",
        override val cause: Throwable? = null,
    ) : AppError
}
