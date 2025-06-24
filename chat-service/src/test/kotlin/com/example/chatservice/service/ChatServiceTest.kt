package com.example.chatservice.service

import com.example.chatservice.bdd.BDDSyntax.Given
import com.example.chatservice.bdd.BDDSyntax.Then
import com.example.chatservice.bdd.BDDSyntax.When
import com.example.chatservice.converter.ChatMessageDtoConverter
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.repository.ChatRoomRepository
import com.example.chatservice.repository.UserRepository
import com.example.chatservice.entity.ChatRoom
import com.example.chatservice.entity.Users
import com.example.chatservice.exception.ChatRoomNotFoundException
import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.repository.ChatMessageRepository
import com.example.chatservice.repository.ChatRoomUsersRepository
import com.example.chatservice.tdd.TestcontainersTDDConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import kotlin.test.Test
import kotlin.test.assertEquals

@TestConfiguration(proxyBeanMethods = false)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersTDDConfiguration::class)
class ChatServiceTest {
    val log = KotlinLogging.logger {  }

    @MockitoSpyBean
    private lateinit var chatService: ChatService

    @MockitoBean
    private lateinit var chatMessageRepository: ChatMessageRepository

    @MockitoBean
    private lateinit var chatRoomRepository: ChatRoomRepository

    @MockitoBean
    private lateinit var userRepository: UserRepository

    @MockitoBean
    private lateinit var chatRoomUsersRepository: ChatRoomUsersRepository

    @Autowired
    @Qualifier("chatMessageDtoConverterImpl")
    private lateinit var chatMessageConverter: ChatMessageDtoConverter

    @Test
    fun `insert a new chat`() {
        Given("Given Chats") {
            val user = Users(
                userId = 1L,
                nickname = "raonpark",
                profileImage = "Default_IMG"
            )

            val chatRoom = ChatRoom(
                id = 1L,
                roomName = "채팅방1",
                roomDescription = "테스트 채팅방입니다.",
                roomImage = "Default_IMG"
            )

            val chatMessage = ChatMessageRequest(
                content = "Hello World! to different room : 1",
                fromUserId = 1L,
                chatRoomId = 1L
            )

            Mockito.`when`(userRepository.findUserById(1L))
                .thenReturn(user)

            Mockito.`when`(chatRoomUsersRepository.countChatRoomUsersByChatRoomId(Mockito.anyLong()))
                .thenReturn(100)

            Mockito.`when`(chatRoomRepository.findChatRoomById(1L))
                .thenReturn(chatRoom)

            When("insert Chats") {
                val result = chatService.insertChat(chatMessage)

                Then("returns total number of users of chat room") {
                    assertEquals(100, result)
                }
            }
        }
    }

    @Test
    fun `throw UserNotFoundException when inserting an entity with no user exists`() {
        Given("Given") {
            val chatRoom = ChatRoom(
                id = 1L,
                roomName = "테스트 채팅방",
                roomDescription = "테스트 채팅방입니다.",
                roomImage = "https://cdn.room.image?id=1"
            )

            val chatMessage = ChatMessageRequest(
                content = "Hello World! to different room : 1",
                fromUserId = 20394L,
                chatRoomId = 1L
            )

            Mockito.`when`(chatRoomRepository.findChatRoomById(1L))
                .thenReturn(chatRoom)

            Mockito.`when`(userRepository.findUserById(20394L))
                .thenThrow(UserNotFoundException::class.java)

            When("") {
                assertThrows<UserNotFoundException> { chatService.insertChat(chatMessage) }

                Then("verify") {
                    Mockito.verify(chatRoomRepository, Mockito.atMostOnce()).findChatRoomById(1L)
                    Mockito.verify(userRepository, Mockito.atMostOnce()).findUserById(20394L)
                }
            }
        }
    }

    @Test
    fun `throw ChatRoomNotFoundException when inserting an entity with no chatroom exists`() {
        Given("") {
            val user = Users(
                userId = 1L,
                nickname = "raonpark",
                profileImage = "Default_IMG"
            )

            Mockito.`when`(userRepository.findUserById(1L))
                .thenReturn(user)

            val chatMessage = ChatMessageRequest(
                content = "Hello World! to different room : 1",
                fromUserId = 1L,
                chatRoomId = 2393474L
            )

            When("") {
                assertThrows<ChatRoomNotFoundException> { chatService.insertChat(chatMessage) }

                Then("verify") {
                    Mockito.verify(chatRoomRepository, Mockito.atMostOnce()).findChatRoomById(2393474L)
                    Mockito.verify(userRepository, Mockito.atMostOnce()).findUserById(1L)
                }
            }
        }
    }

//    @Test
//    fun `returns messages that in chatroom`() {
//        Given("insert 10 chats for room #1 and 10 chats for room #2") {
//            val user = Users(
//                userId = 1L,
//                nickname = "raonpark",
//                profileImage = "https://cdn.image.user?id=1L"
//            )
//
//            val chatRoom1 = ChatRoom(
//                id = 1L,
//                roomName = "테스트 채팅방",
//                roomImage = "https://cdn.image.room?id=1L",
//                roomDescription = "테스트 채팅방입니다."
//            )
//
//            val chatRoom2 = ChatRoom(
//                id = 2L,
//                roomName = "테스트 채팅방",
//                roomImage = "https://cdn.image.room?id=2L",
//                roomDescription = "테스트 채팅방입니다."
//            )
//
//            val chatMessages1 = mutableListOf<ChatMessage>()
//            for(i in 1..10) {
//                chatMessages1.add(
//                    ChatMessage(
//                        id = i.toLong(),
//                        content = "Hello World! to Room #1 and ${i}th",
//                        from = user,
//                        chatRoom = chatRoom1,
//                        checked = 20
//                    )
//                )
//            }
//
//            val chatMessages2 = mutableListOf<ChatMessage>()
//            for(i in 1..10) {
//                chatMessages2.add(
//                    ChatMessage(
//                        id = i.toLong() * 10,
//                        content = "Hello World! to Room #2 and ${i}th",
//                        from = user,
//                        chatRoom = chatRoom2,
//                        checked = 34
//                    )
//                )
//            }
//
//            Mockito.`when`(userRepository.findUserById(1L))
//                .thenReturn(user)
//
//            Mockito.`when`(chatRoomRepository.findChatRoomById(1L))
//                .thenReturn(chatRoom1)
//
//            Mockito.`when`(chatRoomRepository.findChatRoomById(2L))
//                .thenReturn(chatRoom2)
//
//            Mockito.`when`(chatMessageRepository.findAllChatsByChatRoomId(1L))
//                .thenReturn(chatMessages1)
//
//            Mockito.`when`(chatMessageRepository.findAllChatsByChatRoomId(2L))
//                .thenReturn(chatMessages2)
//
//            val getAllChatMessagesRequestForChatRoom1 = GetAllChatMessagesRequest(
//                chatRoomId = 1L,
//                userId = 1L,
//            )
//
//            val getAllChatMessagesRequestForChatRoom2 = GetAllChatMessagesRequest(
//                chatRoomId = 2L,
//                userId = 1L
//            )
//
//            When("get chats from room #1 and room #2") {
//                val chatMessagesResult1 = chatService.selectAllChats(getAllChatMessagesRequestForChatRoom1)
//                val chatMessagesResult2 = chatService.selectAllChats(getAllChatMessagesRequestForChatRoom2)
//
//                Then("verify") {
//                    Mockito.verify(chatMessageRepository, Mockito.times(2))
//                        .findAllChatsByChatRoomId(Mockito.anyLong())
//
//                }
//            }
//        }
//    }
}