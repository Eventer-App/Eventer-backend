package com.Eventer.Eventer.user.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Запрос верификации")
interface VerifyRequestDoc {
    @get:Schema(description = "Код верификации", required = true, example = "1234", minLength = 4, maxLength = 4)
    val code: String
}

data class VerifyRequest (
    @NotBlank(message = "Code cannot be empty")
    @Size(min = 4, max = 4, message = "The code cannot be empty")
    override val code: String
) : VerifyRequestDoc