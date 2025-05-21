package com.example.notes.user

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional()
    fun createUser(
        username: String,
        password: String,
        name: String,
    ) {
        if (userRepository.findByUsername(username) == null) {
            userRepository.save(
                User(
                    password = passwordEncoder.encode(password),
                    username = username,
                    name = name,
                ),
            )
        }
    }

    @Transactional(readOnly = true)
    fun findUserByUsername(username: String): User? = userRepository.findByUsername(username)
}
