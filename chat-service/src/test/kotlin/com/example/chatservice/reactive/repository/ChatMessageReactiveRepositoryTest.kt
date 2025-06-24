package com.example.chatservice.reactive.repository

import com.example.chatservice.reactive.entity.ChatMessage
import com.example.chatservice.reactive.entity.ChatRoom
import com.example.chatservice.reactive.entity.User
import com.example.chatservice.tdd.TestcontainersTDDConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate

@SpringBootTest
@TestConfiguration(proxyBeanMethods = false)
@Import(TestcontainersTDDConfiguration::class)
class ChatMessageReactiveRepositoryTest {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @Autowired
    private lateinit var chatRoomReactiveRepository: ChatRoomReactiveRepository

    @Autowired
    private lateinit var userReactiveRepository: UserReactiveRepository

    @Autowired
    private lateinit var chatMessageReactiveRepository: ChatMessageReactiveRepository

    @Autowired
    private lateinit var chatRoomUsersReactiveRepository: ChatRoomUsersReactiveRepository

    @Autowired
    private lateinit var r2dbcTemplate: R2dbcEntityTemplate

    @Test
    fun testChatMessageRepository(): Unit = runBlocking {
        r2dbcTemplate.insert(User(
            id = 1L,
            nickname = "testUser",
            profileImage = "testImage",
        )).awaitSingle()

        r2dbcTemplate.insert(ChatRoom(
            id = 1L,
            roomName = "testRoom",
            roomDescription = "testRoom",
            roomImage = "testImage",
        )).awaitSingle()

        for(i in 1..100) {
            r2dbcTemplate.insert(ChatMessage(
                id = i.toLong(),
                content = "Hello World $i",
                fromUserId = 1L,
                chatRoomId = 1L,
                checked = 100
            )).awaitSingle()
        }

        chatMessageReactiveRepository.findAllByChatRoomId(1L, PageRequest.of(0, 30))
            .map {
                log.info { "here is message : $it" }
            }
    }
}