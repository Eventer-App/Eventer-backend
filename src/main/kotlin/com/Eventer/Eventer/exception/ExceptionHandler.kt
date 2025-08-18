package com.Eventer.Eventer.exception

import com.Eventer.Eventer.exception.dto.ExceptionGeneralResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException


@ControllerAdvice
class ExceptionHandler {

    // Обрабатывает ошибки неверного типа параметра (например, count="abc")
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException): ResponseEntity<Map<String, String>> {
        return ResponseEntity(
            mapOf("error" to "Invalid parameter: ${ex.name} must be a number"),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ExceptionGeneralResponse> {
        val message = ex.constraintViolations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
        return ResponseEntity(ExceptionGeneralResponse(message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ExceptionGeneralResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.joinToString("; ") {
            "${it.field}: ${it.defaultMessage}"
        }

        val globalErrors = ex.bindingResult.globalErrors.joinToString("; ") {
            "${it.objectName}: ${it.defaultMessage}"
        }

        val message = listOf(fieldErrors, globalErrors)
            .filter { it.isNotBlank() }
            .joinToString("; ")

        return ResponseEntity(ExceptionGeneralResponse(message), HttpStatus.BAD_REQUEST)
    }

    // Ошибка пустого JSON (или некорректного JSON)
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleEmptyRequestBody(ex: HttpMessageNotReadableException): ResponseEntity<ExceptionGeneralResponse> {
        return ResponseEntity(ExceptionGeneralResponse("Request body is missing or invalid"), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(GeneralException::class)
    fun handleGeneralError(ex: GeneralException): ResponseEntity<ExceptionGeneralResponse> {
        return ResponseEntity(ExceptionGeneralResponse(ex.message), ex.status)
    }

    @ExceptionHandler(RegisterException::class)
    fun handleRegisterException(ex: RegisterException) : ResponseEntity<ExceptionGeneralResponse> {
        return ResponseEntity(ExceptionGeneralResponse(null), ex.status)
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(ex: NoResourceFoundException): ResponseEntity<ExceptionGeneralResponse> {
        return ResponseEntity(ExceptionGeneralResponse(ex.message), HttpStatus.NOT_FOUND)
    }

//    todo: disable it in prod
    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(ex: Exception): ResponseEntity<ExceptionGeneralResponse> {
        return ResponseEntity(ExceptionGeneralResponse("Unexpected error: ${ex.message}"), HttpStatus.INTERNAL_SERVER_ERROR)
    }

}