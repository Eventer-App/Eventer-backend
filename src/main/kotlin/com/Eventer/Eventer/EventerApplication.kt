package com.Eventer.Eventer

import com.Eventer.Eventer.security.config.TokenProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(TokenProperties::class)
class EventerApplication

fun main(args: Array<String>) {
	runApplication<EventerApplication>(*args)
}
