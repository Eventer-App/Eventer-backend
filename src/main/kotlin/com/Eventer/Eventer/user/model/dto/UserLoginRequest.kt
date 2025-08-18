package com.Eventer.Eventer.user.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserLoginRequest (
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "Incorrect email address")
    @Size(max = 50, message = "The email length cannot be more than 50 characters")
    val email: String,
    @NotBlank(message = "password cannot be empty")
    @Size(min = 8, message = "Password is too short")
    val password: String,
    )