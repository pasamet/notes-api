package com.example.notes.auth

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

private const val USERNAME = "user1"
private const val PASSWORD = "pass1"
private const val RESOURCE_PATH = "/mock-authenticated-resource"

@RestController
@RequestMapping(RESOURCE_PATH)
class MockMvcController {
    @GetMapping()
    fun currentUser(principal: Principal): String = "User: ${principal.name}"
}

@WebMvcTest(
    MockMvcController::class,
    AuthController::class,
)
@Import(
    SecurityConfig::class,
    TokenService::class,
)
internal class AuthControllerTest {
    @Autowired
    lateinit var mvc: MockMvc

    @MockkBean
    lateinit var jpaUserDetailsService: JpaUserDetailsService

    @Test
    fun `When no token is set Then response status is unauthorized`() {
        mvc.get(RESOURCE_PATH).andExpect {
            status {
                isUnauthorized()
            }
        }
    }

    @Test
    fun `Given user is registered When token is set Then response contains the username`() {
        every {
            jpaUserDetailsService.loadUserByUsername(USERNAME)
        } returns UserDetailsImpl(USERNAME, "{noop}$PASSWORD")

        val result =
            mvc
                .perform(
                    post("/token")
                        .with(httpBasic(USERNAME, PASSWORD)),
                ).andExpect(status().isOk())
                .andReturn()
        val token = result.getResponse().getContentAsString()

        mvc
            .get(RESOURCE_PATH) {
                header("Authorization", "Bearer $token")
            }.andExpect {
                content {
                    string("User: $USERNAME")
                }
            }
    }

    @Test
    @WithMockUser(USERNAME)
    fun `Given mock user set When token is set Then response contains the username`() {
        mvc.get(RESOURCE_PATH).andExpect {
            content {
                string("User: $USERNAME")
            }
        }
    }
}
