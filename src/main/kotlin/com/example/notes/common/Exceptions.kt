package com.example.notes.common

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.server.ResponseStatusException

@ResponseStatus(HttpStatus.NOT_FOUND)
class HttpNotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND)

@ResponseStatus(HttpStatus.FORBIDDEN)
class HttpForbiddenException : ResponseStatusException(HttpStatus.FORBIDDEN)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class HttpUnauthorizedException : ResponseStatusException(HttpStatus.UNAUTHORIZED)
