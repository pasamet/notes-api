package com.example.notes.note
import com.example.notes.user.User
import org.springframework.data.domain.Limit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : JpaRepository<Note, Long> {
    fun findByUserOrderByCreatedAtDesc(
        limit: Limit,
        user: User,
    ): List<Note>
}
