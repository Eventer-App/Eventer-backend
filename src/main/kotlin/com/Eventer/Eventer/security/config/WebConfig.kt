package com.Eventer.Eventer.security.config

import com.Eventer.Eventer.common.token.AccessTokenHandler
import com.Eventer.Eventer.security.UserDetails.UserDetailsServiceImpl
import com.Eventer.Eventer.security.filter.AuthenticationFilter
import com.Eventer.Eventer.user.service.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
@Configuration
class WebConfig(
    private val accessTokenHandler: AccessTokenHandler,
    private val userDetailsService: UserDetailsServiceImpl,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val userService: UserService,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .exceptionHandling { it.authenticationEntryPoint(jwtAuthenticationEntryPoint) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers(
                        "/auth/**",
                        "/api/v1/users/register/**",
                        "/api/v1/users/verify",
                        "/api/v1/users/send-code",
                        "/api/v1/users/refresh",
                        "/api/v1/users/logIn",
                    ).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                AuthenticationFilter(accessTokenHandler, userDetailsService, userService),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf(
            "https://local.eventer.ru:*",
            "http://localhost:*"
        )

        configuration.allowCredentials = true

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

}