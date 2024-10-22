package com.felipejdias.rinhabackend2024q1.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Field invalid")
class InvalidParameterException: RuntimeException() {
}