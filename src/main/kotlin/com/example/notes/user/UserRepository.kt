package com.example.notes.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {
    fun findByUsername(username: String): User?
}

@Entity
@Table(name = "users")
class User(
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    val username: String,
    @Column(nullable = false)
    val password: String,
    @Column(nullable = false)
    val name: String,
)
