package com.Eventer.Eventer.common.enviroment

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class EnvironmentConfig {
    @Autowired
    private lateinit var environment: Environment

    fun getActiveProfile() = environment.activeProfiles.firstOrNull() ?: "default"

}