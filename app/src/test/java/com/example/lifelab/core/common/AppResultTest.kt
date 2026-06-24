package com.example.lifelab.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class AppResultTest {

    @Test
    fun successCarriesExpectedValue() {
        val result = AppResult.Success("ready")

        assertEquals("ready", result.value)
    }

    @Test
    fun validationErrorCarriesExpectedMessage() {
        val error = AppError.Validation(message = "Title is required")
        val result = AppResult.Failure(error)

        assertEquals("Title is required", result.error.message)
    }
}
