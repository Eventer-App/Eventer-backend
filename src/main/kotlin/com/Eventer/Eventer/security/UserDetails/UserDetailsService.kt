package com.Eventer.Eventer.security.UserDetails

import com.Eventer.Eventer.user.service.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userService: UserService
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        userService.findByEmail(username)
            ?: throw UsernameNotFoundException("Пользователь не найден: $username")

}
