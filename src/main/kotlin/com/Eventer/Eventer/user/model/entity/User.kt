package com.Eventer.Eventer.user.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
class User (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id : Int? = 0,
    val avatarUrl : String? = null,
    var username: String? = null,
)

@Entity(name = "google_users")
class GoogleUser(
    val googleId: String,
    val googleMail: String,
    avatarUrl: String? = null,
    username: String? = null,
) : User (
    avatarUrl = avatarUrl,
    username = username,
)

@Entity(name = "own_users")
class OwnUser(
    val email: String,
    val passwordHash: String,
    var verified: Boolean = false,
    avatarUrl: String? = null,
    username: String? = null
) : User (
    avatarUrl = avatarUrl,
    username = username,
)
