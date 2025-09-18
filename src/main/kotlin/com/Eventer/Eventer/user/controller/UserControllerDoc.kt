package com.Eventer.Eventer.user.controller

import com.Eventer.Eventer.user.model.dto.UserAccountResponse
import com.Eventer.Eventer.user.model.dto.UserCreateRequest
import com.Eventer.Eventer.user.model.dto.UserLoginRequest
import com.Eventer.Eventer.user.model.dto.VerifyRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "User API", description = "API для работы с пользователем. В основном всю информацию бэк тянет из секьюрных куков")
interface UserControllerDoc {

    @Operation(
        summary = "Регистрация пользователя",
        description = "Создает нового пользователя и отправляет код верификации на email"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Пользователь успешно создан, код верификации отправлен", content = [Content()]),
            ApiResponse(responseCode = "200", description = "Пользователь уже существует, код верификации отправлен повторно", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Неверные входные данные", content = [Content()]),
        ]
    )
    fun registerUser(
        @RequestBody user: UserCreateRequest,
        response: HttpServletResponse
    ): ResponseEntity<Nothing?>

    @Operation(
        summary = "Верификация пользователя",
        description = "Подтверждение email пользователя с помощью кода верификации"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Пользователь успешно верифицирован", content = [Content()]),
            ApiResponse(responseCode = "400", description = "Неверный код верификации или истек срок действия", content = [Content()]),
            ApiResponse(responseCode = "404", description = "Сессия верификации не найдена", content = [Content()]),
        ]
    )
    fun verifyUser(
        @RequestBody verifyRequest: VerifyRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<Unit>

    @Operation(
        summary = "Отправка кода верификации",
        description = "Ещё в разработке"
    )
    @ApiResponse(responseCode = "200", description = "Код верификации отправлен")
    fun sendCode(
        request: HttpServletRequest,
    ) : ResponseEntity<String> {
        return ResponseEntity.ok("")
    }

    @Operation(
        summary = "Аутентификация пользователя",
        description = "Вход в систему с email и паролем"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Успешная аутентификация", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = [Content()]),
            ApiResponse(responseCode = "403", description = "Пользователь не верифицирован", content = [Content()]),
        ]
    )
    fun logIn(
        @RequestBody logInRequest: UserLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<Unit>

    @Operation(
        summary = "Обновление токена",
        description = "Обновление access token с помощью refresh token"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Токены успешно обновлены", content = [Content()]),
            ApiResponse(responseCode = "401", description = "Недействительный refresh token", content = [Content()]),
        ]
    )
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse
    )

    @Operation(
        summary = "Получение данных аккаунта",
        description = "Ещё в разработке",
    )
    @ApiResponse(responseCode = "200", description = "Данные аккаунта получены")
    fun getAccount(): ResponseEntity<UserAccountResponse>

}