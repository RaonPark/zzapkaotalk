package com.example.chatservice.service

import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.bdd.BDDSyntax
import com.example.chatservice.bdd.BDDSyntax.Then
import com.example.chatservice.bdd.BDDSyntax.When
import com.example.chatservice.converter.ChatMessageDtoConverter
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration::class)
@TestConfiguration(proxyBeanMethods = false)
class ConverterTest {
    @Autowired
    private lateinit var userRepository: UserReactiveRepository

    @Autowired
    private lateinit var chatRoomRepository: ChatroomReactiveRepository

    @Autowired
    @Qualifier("chatMessageDtoConverterImpl")
    private lateinit var converter: ChatMessageDtoConverter

    @Autowired
    private lateinit var r2dbcTemplate: R2dbcEntityTemplate

    @BeforeEach
    fun setUp() {
        val user = User(
            id = 1L,
            nickname = "raonpark",
            profileImage = "https://profile.img?id=1"
        )
        runBlocking {
            r2dbcTemplate.insert(user)
                .awaitSingle()
        }

        val chatRoom = Chatroom(
            id = 1L,
            roomName = "Promotion",
            roomDescription = "Let's talk with Chess!",
            roomImage = "https://room.profile.img?id=1"
        )

        runBlocking {
            r2dbcTemplate.insert(chatRoom)
                .awaitSingle()
        }
    }

    @Test
    fun `should return entity with no id and checked`() {
        BDDSyntax.Given("Given ChatMessage Request") {

            val chatMessageRequest = ChatMessageRequest(
                content = "Hello World",
                fromUserId = 1L,
                chatRoomId = 1L
            )

            When("convert dto to entity") {
                val chatMessageEntity = converter.convertRequestToModel(chatMessageRequest).also { entity ->
                    entity.createdDate = LocalDateTime.now()
                    entity.modifiedDate = LocalDateTime.now()
                }

                Then("assertion") {
                    assertEquals(1L, chatMessageEntity.chatRoomId)
                    assertNotNull(chatMessageEntity.fromUserId)
                    assertEquals(0L, chatMessageEntity.id)
                }

            }
        }
    }
}