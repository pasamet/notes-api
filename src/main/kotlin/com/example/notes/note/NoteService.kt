package com.example.notes.note

import com.example.notes.common.HttpForbiddenException
import com.example.notes.common.HttpNotFoundException
import com.example.notes.user.User
import org.springframework.data.domain.Limit
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class NoteService(
    private val noteRepository: NoteRepository,
) {
    @Transactional(readOnly = false)
    fun createNote(
        user: User,
        title: String,
        content: String,
        expiresAt: Instant?,
    ): Note {
        val note =
            Note(
                user = user,
                title = title,
                content = content,
                createdAt = Instant.now(),
                expiresAt = expiresAt,
            )
        return noteRepository.save(note)
    }

    @Transactional(readOnly = false)
    fun updateNote(
        user: User,
        id: Long,
        title: String?,
        content: String?,
    ): Note {
        val note = noteRepository.findById(id).orElse(null)
        return when {
            note == null -> throw HttpNotFoundException()
            isExpired(note) -> throw HttpNotFoundException()
            note.user.id != user.id -> throw HttpForbiddenException()
            title == null && content == null -> note
            else -> {
                val version =
                    NoteVersion(
                        note = note,
                        title = note.title,
                        content = note.content,
                        createdAt = note.updatedAt ?: note.createdAt,
                    )
                note.versions += version
                note.updatedAt = Instant.now()
                title?.let {
                    note.title = it
                }
                content?.let {
                    note.content = it
                }
                noteRepository.save(note)
            }
        }
    }

    @Transactional(readOnly = false)
    fun deleteNote(
        user: User,
        id: Long,
    ) {
        val note = noteRepository.findById(id).orElse(null)
        when {
            note == null -> throw HttpNotFoundException()
            isExpired(note) -> throw HttpNotFoundException()
            note.user.id != user.id -> throw HttpForbiddenException()
            else -> {
                noteRepository.delete(note)
            }
        }
    }

    private fun isExpired(note: Note): Boolean = note.expiresAt?.isBefore(Instant.now()) ?: false

    @Transactional(readOnly = true)
    fun getNote(
        user: User,
        id: Long,
    ): Note {
        val note = noteRepository.findById(id).orElse(null)

        return when {
            note == null -> throw HttpNotFoundException()
            isExpired(note) -> throw HttpNotFoundException()
            note.user.id != user.id -> throw HttpForbiddenException()
            else -> note
        }
    }

    @Transactional(readOnly = true) // Good practice for read operations
    fun getNotesByUser(user: User): List<Note> =
        noteRepository
            .findByUserOrderByCreatedAtDesc(
                Limit.of(1000),
                user,
            ).filter { !isExpired(it) }
}
