package com.example.notes.auth

import com.example.notes.user.User
import com.example.notes.user.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class JpaUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails? {
        return userRepository
            .findByUsername(username.orEmpty())
            ?.let { return UserDetailsImpl(it.username, it.password) }
    }

    fun createUser(
        username: String,
        password: String,
        name: String,
    ) {
        userRepository.save(
            User().also {
                it.password = password
                it.username = username
                it.name = name
            },
        )
    }
}

data class UserDetailsImpl(
    private val username: String,
    private val password: String,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = listOf()

    override fun getPassword(): String = password

    override fun getUsername(): String = username
}
