package com.Eventer.Eventer.user.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Запрос на создание пользователя")
interface UserCreateRequestDoc {
    @get:Schema(description = "Email адрес", required = true, example = "user@example.com")
    val email: String

    @get:Schema(description = "Пароль", required = true, example = "password123", minLength = 8)
    val password: String
}


data class UserCreateRequest(
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Incorrect email address")
    @Size(max = 50, message = "The email length cannot be more than 50 characters")
    override val email: String,
    @NotBlank(message = "password cannot be empty")
    @Size(min = 8, message = "Password is too short")
    override val password: String,
) : UserCreateRequestDoc