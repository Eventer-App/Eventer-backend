package com.Eventer.Eventer.security.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "security.jwt")
data class TokenProperties(
    val access: TokenConfig,
    val refresh: TokenConfig
) {
    data class TokenConfig(
        val secret: String,
        val ttl: Duration
    )
}
