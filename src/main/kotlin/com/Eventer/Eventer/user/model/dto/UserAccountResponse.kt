package com.Eventer.Eventer.user.model.dto

import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Данные аккаунта пользователя")
interface UserAccountResponseDoc {
    @get:Schema(description = "Ссылка на аватар пользователя", nullable = true)
    val avatar: String?

    @get:Schema(description = "Имя пользователя", nullable = true)
    val name: String?

    @get:Schema(description = "Email пользователя", required = true)
    val email: String
}

data class UserAccountResponse(
    override val avatar: String?,
    override val name: String?,
    override val email: String
) : UserAccountResponseDoc