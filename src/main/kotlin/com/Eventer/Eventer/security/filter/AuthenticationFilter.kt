package com.Eventer.Eventer.security.filter

import com.Eventer.Eventer.common.token.TokenHandler
import com.Eventer.Eventer.security.UserDetails.UserDetailsServiceImpl
import com.Eventer.Eventer.user.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter


class AuthenticationFilter(
    private val tokenHandler: TokenHandler,
    private val userDetailsService: UserDetailsServiceImpl,
    private val userService: UserService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwtToken: String? = when (request.requestURI) {
                "/refresh" -> getTokenFromRequest(request, "refresh_token")
                else -> getTokenFromRequest(request, "access_token")
            }

            if (!jwtToken.isNullOrBlank()) {
                val username = tokenHandler.extractUsername(jwtToken)
                val authType = tokenHandler.extractAuthType(jwtToken)
                userService.findByEmailAndAuthType(username, authType)
                val userDetails = userDetailsService.loadUserByUsername(username)

                if (!tokenHandler.isTokenValid(jwtToken, userDetails)) {
                    throw BadCredentialsException("Invalid or expired JWT token")
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: AuthenticationException) {
            SecurityContextHolder.clearContext()
            throw ex
        }

        filterChain.doFilter(request, response)
    }

    private fun getTokenFromRequest(request: HttpServletRequest, cookieName: String): String? {
        return request.cookies?.firstOrNull { it.name == cookieName }?.value
    }
}