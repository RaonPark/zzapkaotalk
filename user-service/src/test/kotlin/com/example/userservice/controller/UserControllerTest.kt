package com.example.userservice.controller

import com.example.userservice.TestcontainersConfiguration
import com.example.userservice.dto.LoginRequest
import com.example.userservice.dto.LoginResponse
import com.example.userservice.service.UserService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.server.ServerWebExchange
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
@Testcontainers
@ActiveProfiles("test")
@ExtendWith(MockKExtension::class)
class UserControllerTest {
    @InjectMockKs
    private lateinit var userController: UserController

    @MockK
    private lateinit var userService: UserService

    @MockK
    private lateinit var serverWebExchange: ServerWebExchange

    @Test
    fun `login test`() = runTest {
        val loginRequest = LoginRequest(
            email = "test@test.com",
            password = "test",
        )

        val loginResponse = LoginResponse(
            success = true,
            timestamp = LocalDateTime.now().toString(),
        )

        coEvery { userService.doLogin(loginRequest) } returns loginResponse

        val response = userController.login(loginRequest, serverWebExchange)

        assertEquals(true, response.success)

        coVerify(exactly = 1) {
            userService.doLogin(any<LoginRequest>())
        }
    }

    @Test
    fun `register test`() = runTest {
        val registerRequest = RegisterRequest(
            email = "aron@chess.com",
            password = "aron",
            nickname = "Nimzowitsch",
            profileImage = "https://cdn.avatar.com/id=aron@chess.com",
        )

        val registerResponse = RegisterResponse(
            success = true,
            timestamp = LocalDateTime.now().toString(),
        )

        coEvery {
            userService.register(registerRequest)
        } returns registerResponse

        val response = userController.register(registerRequest)

        assertEquals(true, response.success)

        coVerify(exactly = 1) {
            userService.register(registerRequest)
        }
    }
}