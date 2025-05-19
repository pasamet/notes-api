package com.example.notes.home

import com.example.notes.auth.AuthController
import com.example.notes.auth.SecurityConfig
import com.example.notes.auth.TokenService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(HomeController::class, AuthController::class)
@Import(SecurityConfig::class, TokenService::class)
internal class HomeControllerTest {
    @Autowired
    lateinit var mvc: MockMvc

    @Test
    @Throws(Exception::class)
    fun rootWhenUnauthenticatedThen401() {
        this.mvc!!
            .perform(get("/"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    @Throws(Exception::class)
    fun rootWhenAuthenticatedThenSaysHelloUser() {
        mvc
            .post("/register") {
                content =
                    """
                    { 
                    "username":"user1", 
                    "password":"password1"
                    }
                    """.trimIndent()
                contentType = MediaType.APPLICATION_JSON
            }.andExpect {
                status {
                    isCreated()
                }
            }

        val result =
            mvc
                .perform(
                    post("/token")
                        .with(httpBasic("user1", "password1")),
                ).andExpect(status().isOk())
                .andReturn()

        val token = result.getResponse().getContentAsString()

        this.mvc
            .perform(
                get("/")
                    .header("Authorization", "Bearer " + token),
            ).andExpect(content().string("Hello, dvega"))
    }

    @Test
    @WithMockUser
    @Throws(Exception::class)
    fun rootWithMockUserStatusIsOK() {
        this.mvc!!.perform(get("/")).andExpect(status().isOk())
    }
}
