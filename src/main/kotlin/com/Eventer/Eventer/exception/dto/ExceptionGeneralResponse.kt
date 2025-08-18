package com.Eventer.Eventer.exception.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class ExceptionGeneralResponse(
    val message: String?,
)