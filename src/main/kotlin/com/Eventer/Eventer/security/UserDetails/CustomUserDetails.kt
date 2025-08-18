package com.Eventer.Eventer.security.UserDetails

import com.Eventer.Eventer.security.AuthType
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val username: String,
    val authType: AuthType,
) : UserDetails {

    override fun getAuthorities() = emptyList<Nothing>()

    override fun getPassword() = null

    override fun getUsername() = username

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = true

    override fun isCredentialsNonExpired() = true

    override fun isEnabled() = true
}