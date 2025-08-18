package com.Eventer.Eventer.user.model.repository

import com.Eventer.Eventer.user.model.entity.GoogleUser
import com.Eventer.Eventer.user.model.entity.OwnUser
import com.Eventer.Eventer.user.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}

interface OwnUserRepository : JpaRepository<OwnUser, Long> {
    fun findByEmailIgnoreCase(email: String): OwnUser?
}

interface GoogleUserRepository : JpaRepository<GoogleUser, Long> {
    fun findByGoogleMailIgnoreCase(googleMail: String): GoogleUser?
}