package com.felipejdias.rinhabackend2024q1.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

data class ErrorResponse(val message: String, val field: String? = null)

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RequestValidationExceptions::class)
    fun handleRequestValidationException(ex: RequestValidationExceptions): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(message = ex.message, field = ex.field)
        return ResponseEntity(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY)
    }
}
