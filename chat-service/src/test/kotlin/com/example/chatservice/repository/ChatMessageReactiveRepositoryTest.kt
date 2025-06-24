package com.example.chatservice.repository

import com.example.chatservice.bdd.BDDSyntax
import com.example.chatservice.bdd.BDDSyntax.Then
import com.example.chatservice.bdd.BDDSyntax.When
import com.example.chatservice.entity.ChatMessage
import com.example.chatservice.entity.ChatRoom
import com.example.chatservice.entity.ChatRoomUsers
import com.example.chatservice.entity.Users
import com.example.chatservice.supports.MachineIdGenerator
import com.example.chatservice.supports.SnowflakeIdGenerator
import com.example.chatservice.tdd.TestcontainersTDDConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.support.WindowIterator
import org.springframework.test.context.ActiveProfiles
import kotlin.time.measureTime

@SpringBootTest
@ActiveProfiles("test")
@TestConfiguration(proxyBeanMethods = false)
@Import(TestcontainersTDDConfiguration::class)
class ChatMessageReactiveRepositoryTest {
    @Autowired
    private lateinit var chatRoomUsersRepository: ChatRoomUsersRepository

    @Autowired
    private lateinit var chatMessageRepository: ChatMessageRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var chatRoomRepository: ChatRoomRepository

    private lateinit var user1: Users

    private lateinit var user2: Users

    private lateinit var chatRoom: ChatRoom

    private lateinit var chatRoomUsers1: ChatRoomUsers

    private lateinit var chatRoomUsers2: ChatRoomUsers

    private val snowflakeIdGenerator = SnowflakeIdGenerator(MachineIdGenerator.machineId())

    private val log = KotlinLogging.logger {}

    private val chatRoomId = snowflakeIdGenerator.nextId()

    @BeforeEach
    fun setup() {
        chatRoom = ChatRoom(
            id = chatRoomId,
            roomName = "TestRoom",
            roomDescription = "TestRoom Description",
            roomImage = "https://cdn.image.room?id=1",
        )

        log.info { "ChatRoom: ${chatRoom.id}"}

        chatRoomRepository.save(chatRoom)

        user1 = Users(
            userId = snowflakeIdGenerator.nextId(),
            nickname = "TestUser",
            profileImage = "https://cdn.image.user?id=1"
        )

        log.info { "User: ${user1.id}" }

        userRepository.save(user1)

        user2 = Users(
            userId = snowflakeIdGenerator.nextId(),
            nickname = "TestUser2",
            profileImage = "https://cdn.image.user?id=2"
        )

        log.info { "User2: ${user2.id}" }

        userRepository.save(user2)

        chatRoomUsers1 = ChatRoomUsers(
            id = snowflakeIdGenerator.nextId(),
            chatRoom = chatRoom,
            user = user1,
            role = "Member"
        )

        log.info { "ChatRoomUsers1: ${chatRoomUsers1.id}" }

        chatRoomUsersRepository.save(chatRoomUsers1)

        chatRoomUsers2 = ChatRoomUsers(
            id = snowflakeIdGenerator.nextId(),
            chatRoom = chatRoom,
            user = user2,
            role = "Admin"
        )

        log.info { "ChatRoomUsers2: ${chatRoomUsers2.id}" }

        chatRoomUsersRepository.save(chatRoomUsers2)
    }

    @Test
    fun `should return only 100 pages of chat messages`() {
        BDDSyntax.Given("Given 1000 chats") {
            val elapsedTime = measureTime {
                for(i in 1..1000) {
                    chatMessageRepository.save(
                        ChatMessage(
                            id = snowflakeIdGenerator.nextId(),
                            content = "Hello ${i}th World!",
                            from = user1,
                            chatRoom = chatRoom,
                            checked = chatRoomUsersRepository.countChatRoomUsersByChatRoomId(1L)
                        )
                    )
                }
            }
            log.info { "save 1000 chats : $elapsedTime" }

            When("select only 100 chats when get page") {
                val chatsWindowIterator = WindowIterator.of { position ->
                    chatMessageRepository.findAllChatsByChatRoomId(
                        chatRoomId,
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdDate")),
                        position
                    )
                }.startingAt(ScrollPosition.keyset())

                var result = 0
                chatsWindowIterator.forEachRemaining { window ->

                    result++
                }

                Then("verify") {
                    log.info { result }
                }
            }
        }
    }
}