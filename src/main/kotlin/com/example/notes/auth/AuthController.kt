package com.example.notes.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

private val log: Logger =
    LoggerFactory.getLogger(AuthController::class.java)

@RestController
@SecurityScheme(
    type = SecuritySchemeType.HTTP,
    name = "basicAuth",
    scheme = "basic",
)
class AuthController(
    private val tokenService: TokenService,
    private val passwordEncoder: PasswordEncoder,
    private val jpaUserDetailsService: JpaUserDetailsService,
) {
    @PostMapping("/token")
    @Operation(
        summary = "Update registration detail",
    )
    @SecurityRequirement(name = "basicAuth")
    fun token(authentication: Authentication): String? {
        log.debug("Token requested for user: '{}'", authentication.name)
        val token = tokenService.generateToken(authentication)
        log.debug("Token granted: {}", token)
        return token
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @RequestBody newUser: NewUser,
    ): HttpStatus {
        jpaUserDetailsService.createUser(
            username = newUser.username,
            password = passwordEncoder.encode(newUser.password),
            name = newUser.name,
        )
        return HttpStatus.CREATED
    }
}

data class NewUser(
    val username: String,
    val password: String,
    val name: String,
)
