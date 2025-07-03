package com.example.chatservice.service

import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.bdd.BDDSyntax.Given
import com.example.chatservice.bdd.BDDSyntax.Then
import com.example.chatservice.bdd.BDDSyntax.When
import com.example.chatservice.converter.ChatMessageDtoConverter
import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.exception.ChatRoomNotFoundException
import com.example.chatservice.exception.UserNotFoundException
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.reactive.repository.ChatMessageReactiveRepository
import com.example.chatservice.reactive.repository.ChatroomReactiveRepository
import com.example.chatservice.reactive.repository.ChatroomUsersReactiveRepository
import com.example.chatservice.reactive.repository.UserReactiveRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@TestConfiguration(proxyBeanMethods = false)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class)
class ChatServiceTest {
    val log = KotlinLogging.logger {  }

    private val chatMessageRepository = mockk<ChatMessageReactiveRepository>()

    private val chatRoomRepository = mockk<ChatroomReactiveRepository>()

    private val userRepository = mockk<UserReactiveRepository>()

    private val chatRoomUsersRepository = mockk<ChatroomUsersReactiveRepository>()

    private val r2dbcTemplate = mockk<R2dbcEntityTemplate>()

    private val chatMessageConverter = mockk<ChatMessageDtoConverter>()

    private val userRedisOperations = mockk<ReactiveRedisOperations<String, User>>()

    private val chatService = spyk(ChatService(
        chatMessageRepository, chatMessageConverter, chatRoomUsersRepository, userRepository,
        chatRoomRepository, r2dbcTemplate, userRedisOperations
    ))

    @Test
    fun `insert a new chat`() {
        Given("Given Chats") {
            val user = User(
                id = 1L,
                nickname = "raonpark",
                profileImage = "Default_IMG"
            )

            val chatRoom = Chatroom(
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

            coEvery { userRepository.findById(1L) } returns user

            coEvery { chatRoomUsersRepository.countChatroomUsersByChatroomId(any()) } returns 100

            coEvery { chatRoomRepository.findById(1L) } returns chatRoom

            When("insert Chats") {
                runBlocking {
                    val result = chatService.insertChat(chatMessage)

                    Then("returns total number of users of chat room") {
                        assertEquals(1L, result.chatRoomId)
                    }
                }
            }
        }
    }

    @Test
    fun `throw UserNotFoundException when inserting an entity with no user exists`() {
        Given("Given") {
            val chatRoom = Chatroom(
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

            coEvery { chatRoomRepository.findChatroomById(1L) } returns chatRoom

            coEvery { userRepository.findById(20394L) } throws UserNotFoundException(20394L)

            When("") {
                runBlocking {
                    assertThrows<UserNotFoundException> { chatService.insertChat(chatMessage) }
                }

                Then("verify") {
                    coVerify(exactly = 1) { chatRoomRepository.findById(1L) }
                    coVerify(exactly = 1) { userRepository.findById(20394L) }
                }
            }
        }
    }

    @Test
    fun `throw ChatRoomNotFoundException when inserting an entity with no chatroom exists`() {
        Given("") {
            val user = User(
                id = 1L,
                nickname = "raonpark",
                profileImage = "Default_IMG"
            )

            coEvery { userRepository.findById(1L) } returns user

            val chatMessage = ChatMessageRequest(
                content = "Hello World! to different room : 1",
                fromUserId = 1L,
                chatRoomId = 2393474L
            )

            When("") {
                runBlocking {
                    assertThrows<ChatRoomNotFoundException> { chatService.insertChat(chatMessage) }
                }

                Then("verify") {
                    coVerify(exactly = 1) { chatRoomRepository.findById(2393474L) }
                    coVerify(exactly = 1) { userRepository.findById(1L) }
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