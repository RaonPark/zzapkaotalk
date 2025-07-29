package com.example.userservice.service

import com.example.userservice.TestcontainersConfiguration
import com.example.userservice.dto.LoginRequest
import com.example.userservice.entity.User
import com.example.userservice.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ServerWebExchange
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@Testcontainers
@ExtendWith(MockKExtension::class)
class UserServiceTest {
    @InjectMockKs
    private lateinit var userService: UserService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var passwordEncoder: PasswordEncoder

    @MockK
    private lateinit var serverWebExchange: ServerWebExchange

    @Test
    fun `login user`() = runTest {
        coEvery { passwordEncoder.encode(any<String>()) } returns "test"
        coEvery { passwordEncoder.matches(any<String>(), any<String>()) } returns true

        coEvery { userRepository.findByEmail(any<String>()) } returns User(
            id = 1L,
            nickname = "test",
            profileImage = "test",
            email = "test@test.com",
            password = passwordEncoder.encode("test"),
        )

        val loginRequest = LoginRequest(
            email = "test@test.com",
            password = "test"
        )

        val response = userService.doLogin(loginRequest, serverWebExchange)

        coVerify(exactly = 1) {
            userRepository.findByEmail("test@test.com")
        }

        assertTrue { response.success }
    }

    @Test
    fun `cannot login when password is incorrect`() = runTest {
        coEvery { passwordEncoder.encode(any<String>()) } returns "test"

        coEvery { passwordEncoder.matches(any<String>(), any<String>()) } returns false

        coEvery { userRepository.findByEmail(any<String>()) } returns User(
            id = 1L,
            nickname = "test",
            profileImage = "test",
            email = "test@test.com",
            password = "trip"
        )

        val response = userService.doLogin(
            LoginRequest(email = "test@test.com", password = "test"),
            serverWebExchange
        )

        coVerify(exactly = 1) {
            userRepository.findByEmail("test@test.com")
        }

        assertFalse { response.success }
    }
}