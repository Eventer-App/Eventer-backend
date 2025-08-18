package com.Eventer.Eventer.user.service

import com.Eventer.Eventer.common.enviroment.EnvironmentConfig
import com.Eventer.Eventer.common.token.AccessTokenHandler
import com.Eventer.Eventer.common.token.RefreshTokenHandler
import com.Eventer.Eventer.exception.GeneralException
import com.Eventer.Eventer.exception.RegisterException
import com.Eventer.Eventer.security.AuthType
import com.Eventer.Eventer.security.UserDetails.CustomUserDetails
import com.Eventer.Eventer.user.model.dto.UserCreateRequest
import com.Eventer.Eventer.user.model.dto.UserLoginRequest
import com.Eventer.Eventer.user.model.dto.VerifyRequest
import com.Eventer.Eventer.user.model.entity.GoogleUser
import com.Eventer.Eventer.user.model.entity.OwnUser
import com.Eventer.Eventer.user.model.entity.User
import com.Eventer.Eventer.user.model.entity.Verification
import com.Eventer.Eventer.user.model.repository.GoogleUserRepository
import com.Eventer.Eventer.user.model.repository.OwnUserRepository
import com.Eventer.Eventer.user.model.repository.VerificationUserRepository
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val accessTokenHandler: AccessTokenHandler,
    private val refreshTokenHandler: RefreshTokenHandler,

    private val environmentConfig: EnvironmentConfig,

    private val googleUserRepository: GoogleUserRepository,
    private val ownUserRepository: OwnUserRepository,
    private val verificationUserRepository: VerificationUserRepository,
) {
    companion object {
        private val passwordEncoder = BCryptPasswordEncoder()
        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
        private val usersCash : MutableList<User> = mutableListOf()
        private val verificationCash : MutableMap<User, Verification> = mutableMapOf()
    }

    fun registerUser(userCreateRequest: UserCreateRequest, response: HttpServletResponse) {
        val registeredOwnUser = ownUserRepository.findByEmailIgnoreCase(userCreateRequest.email)
        if (registeredOwnUser != null) {
            setVerificationCode(registeredOwnUser, response)
            throw RegisterException(HttpStatus.OK)
        }

        val user = OwnUser(
            email = userCreateRequest.email,
            passwordHash = passwordEncoder.encode(userCreateRequest.password),
        )
        val saved = ownUserRepository.save(user)
        usersCash.add(saved)
        setVerificationCode(saved, response)
    }

    fun verifyOwnUser(verifyRequest: VerifyRequest, request: HttpServletRequest, response: HttpServletResponse) {
        val verificationId = request.cookies
            ?.firstOrNull { it.name == "sign_method_id" }
            ?.value
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: throw GeneralException("Verification cookie not found or invalid format")

        val verification = verificationCash.values.find { it.id == verificationId }
            ?: verificationUserRepository.findById(verificationId)
                .orElseThrow { GeneralException("Verification not found") }

        if (verification.code != verifyRequest.code) {
            throw GeneralException("Invalid verification code")
        }

        val user = verification.user
        if (!user.verified) {
            user.verified = true
            ownUserRepository.save(user)
        }

        generateAndSetTokens(user, response)

        response.addCookie(createDeletionCookie("sign_method_id"))

        verificationUserRepository.delete(verification)
    }

    fun logIn(userLoginRequest: UserLoginRequest, response: HttpServletResponse) {
        val user = ownUserRepository.findByEmailIgnoreCase(userLoginRequest.email) ?: throw GeneralException("User not found")

    }

    fun findByEmail(email: String): UserDetails? {
        val user = usersCash.find { it.username.equals(email, true) }
        user?.let { return convertUserToUserDetails(it) }

        val ownUser = ownUserRepository.findByEmailIgnoreCase(email)
        if (ownUser != null) return CustomUserDetails(ownUser.email, AuthType.EMAIL)

        val googleUser = googleUserRepository.findByGoogleMailIgnoreCase(email)
        if (googleUser != null) return CustomUserDetails(googleUser.googleMail, AuthType.GOOGLE)

        return null
    }

    fun findByEmailAndAuthType(email: String, authType: AuthType): UserDetails? {
        val user = when (authType) {
            AuthType.GOOGLE -> googleUserRepository.findByGoogleMailIgnoreCase(email)
            AuthType.EMAIL -> ownUserRepository.findByEmailIgnoreCase(email)
        }

        user?.let { usersCash.add(it) }
        return convertUserToUserDetails(user)

    }

    private fun convertUserToUserDetails(user: User?): UserDetails? = when (user) {
            is OwnUser -> CustomUserDetails(user.email, AuthType.EMAIL)
            is GoogleUser -> CustomUserDetails(user.googleMail, AuthType.GOOGLE)
            else -> null
    }

    private fun generateAndSetTokens(user: OwnUser, response: HttpServletResponse) {
        setTokensInResponse(response, accessTokenHandler.generateOwnToken(user), refreshTokenHandler.generateOwnToken(user))
    }

    private fun setVerificationCode(user: OwnUser, response: HttpServletResponse) {
        val code = if (environmentConfig.getActiveProfile() == "LOCAL") "0000"
        else TODO()
        val verification = Verification(
            code = code,
            user = user
        )
        val savedVerification = verificationUserRepository.save(verification)
        verificationCash[user] = savedVerification
        setVerificationInResponse(savedVerification, response)
    }

    private fun setVerificationInResponse(verification: Verification, response: HttpServletResponse) {
        val cookie = createCookie("sign_method_id", verification.id.toString(), verification.ttl)
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun setTokensInResponse(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        val accessCookie =
            createCookie("access_token", accessToken, accessTokenHandler.getTTL())
        val refreshCookie =
            createCookie("refresh_token", refreshToken, refreshTokenHandler.getTTL())

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())
    }

    private fun createDeletionCookie(cookieName: String) = Cookie(cookieName, null).apply {
        path = "/"
        maxAge = 0
        isHttpOnly = true
        secure = true
    }

    private fun createCookie(name: String, value: String, maxAge: Int): ResponseCookie {
        val cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(maxAge.toLong())
            .build()
        return cookie
    }
}