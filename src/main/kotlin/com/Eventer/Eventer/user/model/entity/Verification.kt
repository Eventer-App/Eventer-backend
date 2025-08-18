package com.Eventer.Eventer.user.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "verifications")
class Verification(
    @Id
    val id: UUID = UUID.randomUUID(),

    val code: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: OwnUser,

    val createdAt: LocalDateTime = LocalDateTime.now(),

    val ttl: Int = 600000
)
