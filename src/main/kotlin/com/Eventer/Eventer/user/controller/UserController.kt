package com.Eventer.Eventer.user.controller

import com.Eventer.Eventer.user.model.dto.UserAccountResponse
import com.Eventer.Eventer.user.model.dto.UserCreateRequest
import com.Eventer.Eventer.user.model.dto.UserLoginRequest
import com.Eventer.Eventer.user.model.dto.VerifyRequest
import com.Eventer.Eventer.user.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    val userService: UserService
) : UserControllerDoc {
    @PostMapping("/register")
    override fun registerUser(
        @RequestBody user: UserCreateRequest,
        response: HttpServletResponse
    ): ResponseEntity<Nothing?> {
        userService.registerUser(user,response)
        return ResponseEntity.status(HttpStatus.CREATED).body(null)
    }

    @PostMapping("/verify")
    override fun verifyUser(
        @RequestBody verifyRequest: VerifyRequest,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) = ResponseEntity.ok(userService.verifyOwnUser(verifyRequest,request,response))

    @GetMapping("/send-code")
    override fun sendCode(
        request: HttpServletRequest,
    ) : ResponseEntity<String> {
       return ResponseEntity.ok("")
    }

    @PostMapping("/logIn")
    override fun logIn(
        @RequestBody logInRequest: UserLoginRequest,
        response: HttpServletResponse
    ) = ResponseEntity.ok(
        userService.logIn(
        userLoginRequest = logInRequest,
        response = response
        )
    )

    @PostMapping("/refresh")
    override fun refresh(request: HttpServletRequest, response: HttpServletResponse) {
        userService.refreshToken(request, response)
    }

    @GetMapping("/account")
    override fun getAccount() : ResponseEntity<UserAccountResponse> = ResponseEntity.ok(
        UserAccountResponse(
            avatar = "avatar",
            name = "name",
            email = "email"
        )
    )

}