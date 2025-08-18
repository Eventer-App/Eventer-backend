package com.Eventer.Eventer.user.model.repository

import com.Eventer.Eventer.user.model.entity.Verification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface VerificationUserRepository : JpaRepository<Verification, UUID> {
}