package com.example.notes.user

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import kotlin.test.assertNull

@DataJpaTest()
class UserRepositoryTest {
    @Autowired
    lateinit var userRepository: UserRepository
//
//    @Autowired
//    lateinit var entityManager: EntityManager

//    @Test
//    fun `Given user with username exist When find user by name called Then returns the user`() {
//        entityManager.persist(
//            User().apply {
//                name = "name"
//                password = "password"
//                username = "username"
//            },
//        )
//
//        val actual = userRepository.findByUsername("username")
//
//        assertEquals("password", actual?.password)
//    }

    @Test
    fun `Given no user exists When find user by name called Then returns null`() {
        val actual = userRepository.findByUsername("username")

        assertNull(actual)
    }
}
