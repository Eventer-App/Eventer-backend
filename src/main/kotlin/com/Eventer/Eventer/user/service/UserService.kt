package com.Eventer.Eventer.user.service

import com.Eventer.Eventer.common.enviroment.EnvironmentConfig
import com.Eventer.Eventer.common.token.AccessTokenHandler
import com.Eventer.Eventer.common.token.RefreshTokenHandler
import com.Eventer.Eventer.exception.AuthException
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
import java.time.LocalDateTime
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
        private val usersCash: MutableList<User> = mutableListOf()
        private val verificationCash: MutableMap<User, Verification> = mutableMapOf()
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

        val expired =
            verification.createdAt.plusSeconds((verification.ttl / 1000).toLong()).isBefore(LocalDateTime.now())
        if (expired) {
            verificationUserRepository.delete(verification)
            verificationCash.remove(verification.user)
            throw GeneralException("Verification code has expired", HttpStatus.BAD_REQUEST)
        }

        if (verification.code != verifyRequest.code) {
            throw GeneralException("Invalid verification code", HttpStatus.BAD_REQUEST)
        }

        val user = verification.user
        if (!user.verified) {
            user.verified = true
            ownUserRepository.save(user)
        }

        generateAndSetTokens(user, response)

        response.addCookie(createDeletionCookie("sign_method_id"))

        verificationUserRepository.delete(verification)
        verificationCash.remove(user)
    }


    fun logIn(userLoginRequest: UserLoginRequest, response: HttpServletResponse) {
        val user = usersCash.find { (it as OwnUser).email == userLoginRequest.email }
            ?: ownUserRepository.findByEmailIgnoreCase(userLoginRequest.email)
            ?: throw RegisterException(HttpStatus.UNAUTHORIZED)

        if (!passwordEncoder.matches(userLoginRequest.password, (user as OwnUser).passwordHash)) {
            throw RegisterException(HttpStatus.UNAUTHORIZED)
        }

        if (!user.verified) {
            setVerificationCode(user, response)
            throw GeneralException("User is not verified, verification code resent", HttpStatus.FORBIDDEN)
        }

        generateAndSetTokens(user, response)
    }

    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse) {
        val refreshToken = request.cookies
            ?.firstOrNull { it.name == "refresh_token" }
            ?.value
            ?: throw AuthException(HttpStatus.UNAUTHORIZED)

        val username = refreshTokenHandler.extractUsername(refreshToken)
        val authType = refreshTokenHandler.extractAuthType(refreshToken)

        val user = findByEmailAndAuthType(username, authType) as? OwnUser
            ?: throw AuthException(HttpStatus.UNAUTHORIZED)

        if (!refreshTokenHandler.isTokenValid(refreshToken, convertUserToUserDetails(user))) {
            throw AuthException(HttpStatus.UNAUTHORIZED)
        }

        generateAndSetTokens(user, response)
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

    private fun convertUserToUserDetails(user: User?): UserDetails = when (user) {
        is OwnUser -> CustomUserDetails(user.email, AuthType.EMAIL)
        is GoogleUser -> CustomUserDetails(user.googleMail, AuthType.GOOGLE)
        else -> TODO()
    }

    private fun generateAndSetTokens(user: OwnUser, response: HttpServletResponse) {
        setTokensInResponse(
            response,
            accessTokenHandler.generateOwnToken(user),
            refreshTokenHandler.generateOwnToken(user)
        )
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
        val cookie = createCookie("sign_method_id", verification.id.toString(), verification.ttl / 1000)
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    private fun setTokensInResponse(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        val accessCookie =
            createCookie("access_token", accessToken, accessTokenHandler.getTTL() / 1000)
        val refreshCookie =
            createCookie("refresh_token", refreshToken, refreshTokenHandler.getTTL() / 1000)

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