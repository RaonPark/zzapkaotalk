package com.example.chatservice.service

import com.example.chatservice.bdd.BDDSyntax
import com.example.chatservice.bdd.BDDSyntax.Then
import com.example.chatservice.bdd.BDDSyntax.When
import com.example.chatservice.converter.ChatMessageDtoConverter
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.repository.ChatRoomRepository
import com.example.chatservice.repository.UserRepository
import com.example.chatservice.entity.ChatRoom
import com.example.chatservice.entity.Users
import com.example.chatservice.tdd.TestcontainersTDDConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersTDDConfiguration::class)
@TestConfiguration(proxyBeanMethods = false)
class ConverterTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    @Autowired
    @Qualifier("chatMessageDtoConverterImpl")
    private lateinit var converter: ChatMessageDtoConverter

    @BeforeEach
    fun setUp() {
        val user = Users(
            userId = 1L,
            nickname = "raonpark",
            profileImage = "https://profile.img?id=1"
        )
        userRepository.save(user)

        val chatRoom = ChatRoom(
            id = 1L,
            roomName = "Promotion",
            roomDescription = "Let's talk with Chess!",
            roomImage = "https://room.profile.img?id=1"
        )

        chatRoomRepository.save(chatRoom)
    }

    @Test
    fun `should return entity with no id and checked`() {
        BDDSyntax.Given("Given ChatMessage Request") {

            val chatMessageRequest = ChatMessageRequest(
                content = "Hello World",
                fromUserId = 1L,
                chatRoomId = 1L
            )

            When("") {
                val chatMessageEntity = converter.convertRequestToModel(chatMessageRequest)

                Then("") {
                    assertEquals(1L, chatMessageEntity.chatRoom.id)
                    assertNotNull(chatMessageEntity.from)
                }

            }
        }
    }
}