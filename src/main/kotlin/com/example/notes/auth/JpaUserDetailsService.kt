package com.example.notes.auth

import com.example.notes.user.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class JpaUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails? {
        return userRepository
            .findByUsername(username)
            ?.let { return UserDetailsImpl(it.username, it.password) }
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
