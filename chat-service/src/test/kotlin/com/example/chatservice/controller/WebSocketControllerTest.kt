package com.example.chatservice.controller

import app.cash.turbine.test
import com.chatservice.DirectChatMessageBroadcast
import com.chatservice.GroupChatMessageBroadcast
import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.dto.DirectChatMessageRequest
import com.example.chatservice.dto.DirectChatMessageResponse
import com.example.chatservice.dto.GroupChatMessageRequest
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.DirectChatMessageReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.apache.kafka.clients.admin.NewTopic
import org.awaitility.Durations.*
import org.awaitility.kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.messaging.rsocket.*
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.rsocket.server.transport=websocket"]
)
@Import(value = [TestcontainersConfiguration::class])
@Testcontainers
@ActiveProfiles("test")
class WebSocketControllerTest {

    @Autowired
    private lateinit var groupChatMessageBroadcastTopic: NewTopic

    @Autowired
    private lateinit var rsocketBuilder: RSocketRequester.Builder

    @Autowired
    private lateinit var userRepository: UserReactiveRepository

    @Autowired
    private lateinit var chatroomRepository: ChatroomReactiveRepository

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @Autowired
    private lateinit var directChatMessageRepository: DirectChatMessageReactiveRepository

    @LocalServerPort
    private var serverPort: Int = 0

    @BeforeEach
    fun setup() {

        runBlocking {
            r2dbcEntityTemplate.insert(
                Chatroom(
                    id = 1L,
                    roomName = "test Hello Room",
                    roomDescription = "test Hello Room Description",
                    roomImage = "test Hello Room Image",
                )
            ).awaitSingle()

            r2dbcEntityTemplate.insert(
                User(
                    id = 2L,
                    nickname = "tom",
                    profileImage = "avatar",
                    email = "tom@tom.com",
                    password = "tom",
                )
            ).awaitSingle()

            r2dbcEntityTemplate.insert(
                User(
                    id = 1L,
                    nickname = "nick",
                    profileImage = "test nick profile image",
                    email = "nick@nick.com",
                    password = "nick",
                )
            ).awaitSingle()
        }

    }

    @Test
    @ExperimentalTime
    fun `send chat message to room`() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:$serverPort/rsocket"))

            val result = rSocketRequester
                .route("chat.stream.1")
                .retrieveFlow<GroupChatMessageBroadcast>()
                .test {


                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

    @Test
    fun `send chat message to user and it completes`() = runTest {
        println("websocket url => ws://localhost:$serverPort/rsocket")
        val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:$serverPort/rsocket"))

        rSocketRequester
            .route("chat.direct.1")
            .data(
                DirectChatMessageRequest(
                    fromUserId = 1L,
                    toUserId = 2L,
                    message = "Hello Tom!",
                    timestamp = LocalDateTime.now()
                )
            )
            .send()
            .awaitSingleOrNull()

        directChatMessageRepository.findByFromUserId(1L)
            .test(timeout = 5.seconds) {
                val chat = awaitItem()
                assertEquals("Hello Tom!", chat.message)
                awaitComplete()
            }
    }

    @Test
    fun `send chat message to user and test subscribe`() = runTest {
        val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:$serverPort/rsocket"))

        rSocketRequester
            .route("chat.direct.1")
            .data(DirectChatMessageRequest(
                fromUserId = 1L,
                toUserId = 2L,
                message = "Hello Tom!",
                timestamp = LocalDateTime.now()
            ))
            .send()
            .awaitSingleOrNull()


        rSocketRequester
            .route("chat.direct.stream.2")
            .retrieveFlow<DirectChatMessageResponse>()
            .test(timeout = 5.seconds) {
                assertEquals(awaitItem().message, "Hello Tom!")

                cancelAndIgnoreRemainingEvents()
            }
    }
}