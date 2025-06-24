package com.example.chatservice.controller

import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.entity.ChatRoom
import com.example.chatservice.entity.Users
import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.repository.ChatRoomRepository
import com.example.chatservice.repository.UserRepository
import com.example.chatservice.tdd.TestcontainersTDDConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.test.StepVerifier

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
    private lateinit var chatRoomRepository: ChatRoomRepository

    @MockitoBean
    private lateinit var userRepository: UserRepository

    private val log = KotlinLogging.logger {}

    @BeforeEach
    fun setup() {
    }

    @Test
    fun `test @ControllerAdvice and throw ChatRoomNotFoundException`() {
        Mockito.`when`(userRepository.findUserById(1L))
            .thenReturn(Users(
                userId = 1L,
                nickname = "raonpark",
                profileImage = "https://cdn.image.user?id=1"
            ))

        val response = webTestClient.post().uri("/chatting")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(
                BodyInserters.fromValue(
                 ChatMessageRequest(
                fromUserId = 1L,
                chatRoomId = 1L,
                content = "테스트"
            )))
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
        Mockito.`when`(chatRoomRepository.findChatRoomById(1L))
            .thenReturn(
                ChatRoom(
                    id = 1L,
                    roomName = "테스트 채팅방",
                    roomDescription = "테스트 채팅방입니다.",
                    roomImage = "https://cdn.image.room?id=1"
                )
            )

        assertThrows<UserNotFoundException> {
            chatController.postChatting(
                ChatMessageRequest(
                    fromUserId = 1L,
                    chatRoomId = 1L,
                    content = "테스트"
                )
            )
        }
    }
}