package com.example.notes.note

import com.example.notes.common.HttpForbiddenException
import com.example.notes.common.HttpNotFoundException
import com.example.notes.common.HttpUnauthorizedException
import com.example.notes.user.User
import com.example.notes.user.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.time.Instant

@RestController
class NoteController(
    private val noteService: NoteService,
    private val userService: UserService,
) {
    @GetMapping("/note/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Throws(
        HttpNotFoundException::class,
        HttpForbiddenException::class,
        HttpUnauthorizedException::class,
    )
    fun getNote(
        principal: Principal,
        @PathVariable id: Long,
    ): ExistingNote? {
        val user = resolveUser(principal)
        return noteService.getNote(user, id).toExistingNote()
    }

    @PostMapping("/note")
    @ResponseStatus(HttpStatus.CREATED)
    @Throws(
        HttpForbiddenException::class,
        HttpUnauthorizedException::class,
    )
    fun createNote(
        principal: Principal,
        @RequestBody newNote: NewNote,
    ): ExistingNote {
        val user = resolveUser(principal)

        val result =
            noteService.createNote(
                user,
                title = newNote.title,
                content = newNote.content,
                expiresAt = newNote.expiresAt,
            )

        return result.toExistingNote()
    }

    @PutMapping("/note/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Throws(
        HttpNotFoundException::class,
        HttpForbiddenException::class,
        HttpUnauthorizedException::class,
    )
    fun updateNote(
        principal: Principal,
        @PathVariable id: Long,
        @RequestBody updatedNote: UpdatedNote,
    ): ExistingNote {
        val user = resolveUser(principal)

        val result =
            noteService.updateNote(
                user,
                id,
                title = updatedNote.title,
                content = updatedNote.content,
            )

        return result.toExistingNote()
    }

    @DeleteMapping("/note/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Throws(
        HttpNotFoundException::class,
        HttpForbiddenException::class,
        HttpUnauthorizedException::class,
    )
    fun deleteNote(
        principal: Principal,
        @PathVariable id: Long,
    ) {
        val user = resolveUser(principal)

        noteService.deleteNote(user, id)
    }

    @GetMapping("/note/{id}/versions")
    @ResponseStatus(HttpStatus.OK)
    @Throws(
        HttpNotFoundException::class,
        HttpForbiddenException::class,
        HttpUnauthorizedException::class,
    )
    fun getNoteWithVersions(
        principal: Principal,
        @PathVariable id: Long,
    ): ExistingNoteWithVersions {
        val user = resolveUser(principal)
        return noteService.getNote(user, id).toExistingNoteWithVersions()
    }

    @GetMapping("/note")
    @ResponseStatus(HttpStatus.OK)
    @Throws(
        HttpForbiddenException::class,
        HttpUnauthorizedException::class,
    )
    fun getNotesByUser(principal: Principal): List<ExistingNote> {
        val user = resolveUser(principal)
        return noteService.getNotesByUser(user).map { it.toExistingNote() }
    }

    private fun resolveUser(principal: Principal): User {
        val user = userService.findUserByUsername(principal.name)
        return user ?: throw HttpUnauthorizedException()
    }
}

data class NewNote(
    val title: String,
    val content: String,
    val expiresAt: Instant? = null,
)

data class UpdatedNote(
    val title: String?,
    val content: String?,
)

data class ExistingNote(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
)

data class ExistingNoteWithVersions(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val previousVersions: List<PreviousVersion>,
)

data class PreviousVersion(
    val title: String?,
    val content: String?,
    val createdAt: Instant,
)

private fun Note.toExistingNote(): ExistingNote = ExistingNote(id ?: 0, title, content, createdAt, updatedAt)

private fun Note.toExistingNoteWithVersions(): ExistingNoteWithVersions =
    ExistingNoteWithVersions(
        id ?: 0,
        title,
        content,
        createdAt,
        updatedAt,
        previousVersions =
            versions.map {
                PreviousVersion(
                    title = it.title,
                    content = it.content,
                    createdAt = it.createdAt,
                )
            },
    )
