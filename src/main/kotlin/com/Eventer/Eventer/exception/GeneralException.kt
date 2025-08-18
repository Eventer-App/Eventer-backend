package com.Eventer.Eventer.exception

import org.springframework.http.HttpStatus

open class GeneralException(
    message: String,
    var status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException(message)

open class RegisterException(
    var status: HttpStatus = HttpStatus.BAD_REQUEST,
) : RuntimeException()
