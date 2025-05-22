package com.example.notes.auth

import com.example.notes.user.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
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
    private val userService: UserService,
) {
    @PostMapping("/token")
    @Operation(
        summary = "Provides JWT token",
    )
    @SecurityRequirement(name = "basicAuth")
    fun token(authentication: Authentication): String? = tokenService.generateToken(authentication)

    @PostMapping("/register")
    @Operation(
        summary = "Registers a new user",
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun register(
        @RequestBody @ValidPassword newUser: NewUser,
    ) {
        userService.createUser(
            username = newUser.username,
            password = newUser.password,
            name = newUser.name,
        )
    }
}

data class NewUser(
    val username: String,
    val password: String,
    val name: String,
)
