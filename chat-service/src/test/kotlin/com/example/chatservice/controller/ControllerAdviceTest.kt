package com.example.chatservice.controller

import com.example.chatservice.dto.GroupChatMessageRequest
import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import com.example.chatservice.tdd.TestcontainersTDDConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConfiguration(proxyBeanMethods = false)
@ActiveProfiles("test")
@Import(TestcontainersTDDConfiguration::class)
class ControllerAdviceTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Autowired
    private lateinit var chatController: ChatController

    @MockitoBean
    private lateinit var chatRoomRepository: ChatroomReactiveRepository

    @MockitoBean
    private lateinit var userRepository: UserReactiveRepository

    private val log = KotlinLogging.logger {}

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `test @ControllerAdvice and throw ChatRoomNotFoundException`() {
        coEvery {
            userRepository.findById(1L)
        } returns User(
            id = 1L,
            nickname = "raonpark",
            profileImage = "https://cdn.image.user?id=1",
            email = "raonpark@gmail.com",
            password = "raonpark",
        )

        val response = webTestClient.post().uri("/chatting")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(
                BodyInserters.fromValue(
                    GroupChatMessageRequest(
                        fromUserId = 1L,
                        chatRoomId = 1L,
                        content = "테스트",
                        createdTime = LocalDateTime.now(),
                    )
                )
            )
            .exchange()
            .returnResult(com.example.chatservice.dto.ErrorResponse::class.java)
            .responseBody

        StepVerifier.create(response)
            .expectNextMatches { errorResponse ->
                log.info { errorResponse }
                errorResponse.errorCode == "EntityNotFound" && errorResponse.path == "/chatting"
            }
            .verifyComplete()
    }

    @Test
    fun `test @ControllerAdvice with @Controller itself and throw UserNotFoundException`() {
        coEvery { chatRoomRepository.findById(1L) } returns
                Chatroom(
                    id = 1L,
                    roomName = "테스트 채팅방",
                    roomDescription = "테스트 채팅방입니다.",
                    roomImage = "https://cdn.image.room?id=1"
                )

        coVerify {
            assertThrows<UserNotFoundException> {
                chatController.postChatting(
                    GroupChatMessageRequest(
                        fromUserId = 1L,
                        chatRoomId = 1L,
                        content = "테스트",
                        createdTime = LocalDateTime.now(),
                    )
                )
            }
        }
    }
}