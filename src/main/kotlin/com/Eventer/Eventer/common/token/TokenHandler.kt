package com.Eventer.Eventer.common.token

import com.Eventer.Eventer.exception.GeneralException
import com.Eventer.Eventer.security.AuthType
import com.Eventer.Eventer.security.config.TokenProperties
import com.Eventer.Eventer.user.model.entity.OwnUser
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.time.Instant
import java.util.*

sealed class TokenHandler(
    secret: String,
    private val ttlMillis: Long,
) {

    protected val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    protected fun generateToken(subject: String, claims: Map<String, Any>): String {
        val now = Date.from(
            Instant.now()
        )
        val expiryDate = Date.from(
            Instant.now().plusMillis(ttlMillis)
        )
        return Jwts.builder()
            .setClaims(claims)
            .setAudience("[api]")
            .setSubject(subject)
            .setNotBefore(now)
            .setExpiration(expiryDate)
            .setIssuedAt(now)
            .setId(UUID.randomUUID().toString())
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    abstract fun generateToken(userDetails: UserDetails, authType: AuthType): String

    abstract fun generateOwnToken(user: OwnUser) : String

    abstract fun claimsJws(token: String): Jws<Claims>

    abstract fun getTTL(): Int

    private fun isTokenExpired(token: String): Boolean =
        claimsJws(token).body.expiration.before(Date())

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean =
        extractUsername(token) == userDetails.username && !isTokenExpired(token)

    fun extractUsername(token: String) : String = claimsJws(token).body.subject

    fun extractAuthType(token: String): AuthType {
        val claims = claimsJws(token).body
        val rawAuthType = claims["authType"] as? String
            ?: throw GeneralException("authType не найден в токене", HttpStatus.UNAUTHORIZED)

        return try {
            AuthType.valueOf(rawAuthType)
        } catch (e: IllegalArgumentException) {
            throw GeneralException("Недопустимый тип авторизации: $rawAuthType", HttpStatus.UNAUTHORIZED)
        }
    }


}

@Component
class AccessTokenHandler(
    val tokenProperties: TokenProperties
) : TokenHandler(
    tokenProperties.access.secret,
    tokenProperties.access.ttl.toMillis()
){

    override fun getTTL(): Int = Math.toIntExact(tokenProperties.access.ttl.toSeconds())

    override fun generateToken(userDetails: UserDetails, authType: AuthType): String {
        val claims = mapOf(
            "token_type" to "JWT_ACCESS",
            "authType" to authType.name
        )
        return generateToken(userDetails.username, claims)
    }

    override fun generateOwnToken(user: OwnUser) : String {
        val claims = mapOf(
            "token_type" to "JWT_ACCESS",
            "authType" to AuthType.EMAIL.name
        )
        return generateToken(user.email, claims)
    }

    override fun claimsJws(token: String): Jws<Claims> {
        try {
            return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
        } catch (e: SignatureException) {
            throw GeneralException("Некорректная подпись токена.", HttpStatus.UNAUTHORIZED)
        } catch (e: MalformedJwtException) {
            throw GeneralException("Некорректный токен.", HttpStatus.UNAUTHORIZED)
        } catch (e: ExpiredJwtException) {
            throw GeneralException("Срок действия истёк.", HttpStatus.UNAUTHORIZED)
        } catch (e: UnsupportedJwtException) {
            throw GeneralException(
                "JWT token is unsupported.",
                HttpStatus.UNAUTHORIZED
            )
        } catch (e: IllegalArgumentException) {
            throw GeneralException(
                "JWT claims string is empty.",
                HttpStatus.UNAUTHORIZED
            )
        }
    }
}


@Component
class RefreshTokenHandler(
    val tokenProperties: TokenProperties
) : TokenHandler(
    tokenProperties.refresh.secret,
    tokenProperties.refresh.ttl.toMillis()
) {

    override fun getTTL(): Int = Math.toIntExact(tokenProperties.refresh.ttl.toSeconds())

    override fun generateToken(userDetails: UserDetails, authType: AuthType): String {
        val claims = mapOf(
            "token_type" to "JWT_REFRESH",
            "authType" to authType.name
        )
        return generateToken(userDetails.username, claims)
    }

    override fun generateOwnToken(user: OwnUser) : String {
        val claims = mapOf(
            "token_type" to "JWT_REFRESH",
            "authType" to AuthType.EMAIL.name
        )
        return generateToken(user.email, claims)
    }

    override fun claimsJws(token: String): Jws<Claims> {
        try {
            return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
        } catch (e: SignatureException) {
            throw GeneralException("Некорректная подпись токена.", HttpStatus.BAD_REQUEST)
        } catch (e: MalformedJwtException) {
            throw GeneralException("Некорректный токен.", HttpStatus.BAD_REQUEST)
        } catch (e: ExpiredJwtException) {
            throw GeneralException("Срок действия истёк.", HttpStatus.BAD_REQUEST)
        } catch (e: UnsupportedJwtException) {
            throw GeneralException(
                "JWT token is unsupported.",
                HttpStatus.BAD_REQUEST
            )
        } catch (e: IllegalArgumentException) {
            throw GeneralException(
                "JWT claims string is empty.",
                HttpStatus.BAD_REQUEST
            )
        }
    }
}