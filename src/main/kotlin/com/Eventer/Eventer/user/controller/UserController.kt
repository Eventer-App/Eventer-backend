package com.Eventer.Eventer.user.controller

import com.Eventer.Eventer.user.model.dto.UserAccountResponse
import com.Eventer.Eventer.user.model.dto.UserCreateRequest
import com.Eventer.Eventer.user.model.dto.UserLoginRequest
import com.Eventer.Eventer.user.model.dto.VerifyRequest
import com.Eventer.Eventer.user.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    val userService: UserService
) {
    @PostMapping("/register")
    fun registerUser(
        @RequestBody user: UserCreateRequest,
        response: HttpServletResponse
    ) = ResponseEntity.ok(userService.registerUser(user,response))

    @PostMapping("/verify")
    fun verifyUser(
        @RequestBody verifyRequest: VerifyRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) = ResponseEntity.ok(userService.verifyOwnUser(verifyRequest,request,response))

    @GetMapping("/send-code")
    fun sendCode(
        request: HttpServletRequest,
    ) : ResponseEntity<String> {
       return ResponseEntity.ok("")
    }


    @PostMapping("/logIn")
    fun logIn(
        @RequestBody logInRequest: UserLoginRequest,
        response: HttpServletResponse
    ) = ResponseEntity.ok(userService.logIn(
        userLoginRequest = logInRequest,
        response = response
    ))

    @GetMapping("/account")
    fun getAccount() : ResponseEntity<UserAccountResponse> = ResponseEntity.ok(
        UserAccountResponse(
            avatar = "avatar",
            name = "name",
            email = "email"
        )
    )

}