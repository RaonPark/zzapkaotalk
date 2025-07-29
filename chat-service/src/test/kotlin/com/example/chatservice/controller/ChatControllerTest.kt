package com.example.chatservice.controller

import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.bdd.BDDSyntax
import com.example.chatservice.bdd.BDDSyntax.When
import com.example.chatservice.dto.GetAllChatMessagesResponse
import com.example.chatservice.reactive.entity.GroupChatMessage
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import com.example.chatservice.service.ChatService
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@Testcontainers
class ChatControllerTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockitoBean
    private lateinit var chatService: ChatService

    @Autowired
    private lateinit var userReactiveRepository: UserReactiveRepository

    @Autowired
    private lateinit var chatroomReactiveRepository: ChatroomReactiveRepository

    @Autowired
    private lateinit var r2dbcEntityTemplate: R2dbcEntityTemplate

    @BeforeEach
    fun setup() {
        runBlocking {
            r2dbcEntityTemplate.insert(
                Chatroom(
                    id = 1L,
                    roomName = "test room",
                    roomImage = "https://cdn.image.room?id=1",
                    roomDescription = "test room description",
                )
            ).awaitSingle()

            r2dbcEntityTemplate.insert(
                User(
                    id = 1L,
                    nickname = "raonpark",
                    profileImage = "https://cdn.image.profile/avatar/avatar.png",
                    email = "raonpark@gmail.com",
                    password = "raonpark",
                )
            ).awaitSingle()
        }

    }

    @Test
    fun `insert 200 chats and show 30 chats`() {
        BDDSyntax.Given("insert 200 chats") {
            runBlocking {
                for (i in 1L..200L) {
                    r2dbcEntityTemplate.insert(
                        GroupChatMessage(
                            id = i,
                            content = "chat message $i",
                            fromUserId = 1L,
                            chatRoomId = 1L,
                            checked = 100
                        )
                    ).awaitSingle()
                }
            }

            When("show 30 chats") {
                for(pageOffSet in 0 until 200 step 30) {
                    val result = webTestClient.get()
                        .uri("/chat/1/1/$pageOffSet")
                        .exchange()
                        .expectStatus().isOk
                        .returnResult(GetAllChatMessagesResponse::class.java)

                    val chats = result.responseBody.collectList().block()!!
                        .forEach { chat ->
                            println(chat)
                        }
                }
            }
        }
    }
}