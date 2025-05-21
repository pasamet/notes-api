package com.example.notes.note

import com.example.notes.user.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "notes",
    indexes = [
        Index(name = "idx_note_user", columnList = "user_id"),
        Index(name = "idx_note_createdAt", columnList = "created_at"),
    ],
)
class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @OneToMany(mappedBy = "note", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("created_at DESC")
    var versions: MutableList<NoteVersion> = mutableListOf(),
    @Column(nullable = false, updatable = false)
    var createdAt: Instant,
    @Column(nullable = true, updatable = true)
    var updatedAt: Instant? = null,
    @Column(nullable = true, updatable = true)
    var expiresAt: Instant? = null,
    @Column(nullable = false, updatable = true, length = 255)
    var title: String,
    @Lob
    @Column(nullable = false, updatable = true, columnDefinition = "TEXT")
    var content: String,
)

@Entity
@Table(
    name = "note_versions",
    indexes = [
        Index(name = "idx_note_versions_created_at", columnList = "created_at"),
    ],
)
class NoteVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    val note: Note,
    @Column(nullable = false, updatable = false, length = 255)
    val title: String,
    @Lob
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    val content: String,
    @Column(nullable = false, updatable = false)
    val createdAt: Instant,
)
