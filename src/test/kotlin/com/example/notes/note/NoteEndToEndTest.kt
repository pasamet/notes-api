package com.example.notes.note

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private const val USERNAME1 = "user1"

private const val PASSWORD1 = "!Pass123456"

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
class NoteEndToEndTest {
    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun clearDatabase(
        @Autowired jdbcTemplate: JdbcTemplate,
    ) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "note_versions", "notes")
    }

    @Test
    fun `Given a note is created When note is requested by id Then response contains the note`() {
        registerUser()
        val token = getToken()
        val note = createNote(token, "title1", "content1")

        val actual = getNote(token, note.id)

        assertEquals(note.id, actual.id)
        assertEquals(note.title, actual.title)
        assertEquals(note.content, actual.content)
    }

    @Test
    fun `Given a note is created When note title is updated Then response contains the new title and old contents`() {
        registerUser()
        val token = getToken()
        val note = createNote(token, "title1", "content1")

        val expectedTitle = "title2"
        val actual = updateNote(token, note.id, expectedTitle)

        assertEquals(note.id, actual.id)
        assertEquals(expectedTitle, actual.title)
        assertEquals(note.content, actual.content)
        assertNotEquals(note.updatedAt?.toEpochMilli(), actual.updatedAt?.toEpochMilli())
        assertEquals(note.createdAt.toEpochMilli(), actual.createdAt.toEpochMilli())
    }

    @Test
    fun `Given a note is created When note is deleted Then get id result is NOT_FOUND`() {
        registerUser()
        val token = getToken()
        val note = createNote(token, "title1", "content1")

        deleteNote(token, note.id)

        getNoteExpectNotFound(token, note.id)
    }

    @Test
    fun `Given a note is edited 3 times When note with versions are requested Then previous versions are listed`() {
        registerUser()
        val token = getToken()
        val note = createNote(token, "title1", "content1")
        updateNote(token, note.id, "title2")
        updateNote(token, note.id, contentText = "content2")

        val actual = getNoteWithVersions(token, note.id)

        assertEquals(note.id, actual.id)
        assertEquals("title2", actual.title)
        assertEquals("content2", actual.content)
        assertEquals(2, actual.previousVersions.size)
        assertEquals("content1", actual.previousVersions[0].content)
        assertEquals("title2", actual.previousVersions[0].title)
        assertEquals("content1", actual.previousVersions[1].content)
        assertEquals("title1", actual.previousVersions[1].title)
    }

    @Test
    fun `Given user created multiple notes When all notes are requested Then previous notes are listed`() {
        val expectedCount = 10
        registerUser()
        val token = getToken()
        for (index in 1..expectedCount) {
            createNote(token, "title$index", "content$index")
        }

        val actual = getNotesByUser(token)

        assertEquals(expectedCount, actual.size)
    }

    private fun getNote(
        token: String,
        id: Long,
    ): ExistingNote {
        val result =
            mvc
                .get("/note/$id") {
                    header("Authorization", "Bearer $token")
                }.andExpect {
                    status {
                        isOk()
                    }
                    jsonPath("$.id", notNullValue())
                }.andReturn()
        return objectMapper.readValue(
            result.response.contentAsString,
            ExistingNote::class.java,
        )!!
    }

    private fun getNoteWithVersions(
        token: String,
        id: Long,
    ): ExistingNoteWithVersions {
        val result =
            mvc
                .get("/note/$id/versions") {
                    header("Authorization", "Bearer $token")
                }.andExpect {
                    status {
                        isOk()
                    }
                    jsonPath("$.id", notNullValue())
                }.andReturn()
        return objectMapper.readValue(
            result.response.contentAsString,
            ExistingNoteWithVersions::class.java,
        )!!
    }

    private fun getNoteExpectNotFound(
        token: String,
        id: Long,
    ) {
        mvc
            .get("/note/$id") {
                header("Authorization", "Bearer $token")
            }.andExpect {
                status {
                    isNotFound()
                }
            }
    }

    private fun createNote(
        token: String,
        title: String,
        contentText: String,
    ): ExistingNote {
        val result =
            mvc
                .post("/note") {
                    header("Authorization", "Bearer $token")
                    content =
                        """
                        {
                        "title":"$title",
                        "content":"$contentText"
                        }
                        """.trimIndent()
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status {
                        isCreated()
                    }
                    jsonPath("id", notNullValue())
                }.andReturn()
        return objectMapper.readValue(
            result.response.contentAsString,
            ExistingNote::class.java,
        )!!
    }

    private fun updateNote(
        token: String,
        id: Long,
        title: String? = null,
        contentText: String? = null,
    ): ExistingNote {
        val fields = mutableListOf<String>()
        title?.let {
            fields += "\"title\":\"$it\""
        }
        contentText?.let {
            fields += "\"content\":\"$it\""
        }

        val result =
            mvc
                .put("/note/$id") {
                    header("Authorization", "Bearer $token")
                    content = "{${fields.joinToString(",\n")}}"
                    contentType = MediaType.APPLICATION_JSON
                }.andExpect {
                    status {
                        isOk()
                    }
                    jsonPath("id", notNullValue())
                }.andReturn()
        return objectMapper.readValue(
            result.response.contentAsString,
            ExistingNote::class.java,
        )!!
    }

    private fun deleteNote(
        token: String,
        id: Long,
    ) {
        mvc
            .delete("/note/$id") {
                header("Authorization", "Bearer $token")
            }.andExpect {
                status {
                    isOk()
                }
            }
    }

    private fun getNotesByUser(token: String): List<ExistingNote> {
        val result =
            mvc
                .get("/note") {
                    header("Authorization", "Bearer $token")
                }.andExpect {
                    status {
                        isOk()
                    }
                }.andReturn()
        return objectMapper
            .readValue(
                result.response.contentAsString,
                Array<ExistingNote>::class.java,
            ).toList()
    }

    private fun registerUser(
        username: String = USERNAME1,
        password: String = PASSWORD1,
    ) {
        mvc
            .post("/register") {
                content =
                    """
                    {
                    "username":"$username",
                    "name":"user name 1",
                    "password":"$password"
                    }
                    """.trimIndent()
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status {
                    isCreated()
                }
            }
    }

    private fun getToken(
        username: String = USERNAME1,
        password: String = PASSWORD1,
    ): String {
        val result =
            mvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/token")
                        .with(httpBasic(username, password)),
                ).andExpect(status().isOk())
                .andReturn()
        val token = result.response.contentAsString
        return token
    }
}
