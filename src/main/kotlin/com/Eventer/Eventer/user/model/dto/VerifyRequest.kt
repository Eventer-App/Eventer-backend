package com.Eventer.Eventer.user.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class VerifyRequest (
    @NotBlank(message = "Code cannot be empty")
    @Size(min = 4, max = 4, message = "The code cannot be empty")
    val code: String
)