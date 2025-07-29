package com.example.chatservice.reactive.repository

import com.example.chatservice.TestcontainersConfiguration
import com.example.chatservice.reactive.entity.GroupChatMessage
import com.example.chatservice.reactive.entity.Chatroom
import com.example.chatservice.reactive.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.test.context.ActiveProfiles
import kotlin.time.measureTime

@SpringBootTest
@TestConfiguration(proxyBeanMethods = false)
@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
class GroupGroupChatMessageReactiveRepositoryTest {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @Autowired
    private lateinit var chatRoomReactiveRepository: ChatroomReactiveRepository

    @Autowired
    private lateinit var userReactiveRepository: UserReactiveRepository

    @Autowired
    private lateinit var groupChatMessageReactiveRepository: GroupChatMessageReactiveRepository

    @Autowired
    private lateinit var chatRoomUsersReactiveRepository: ChatroomUsersReactiveRepository

    @Autowired
    private lateinit var r2dbcTemplate: R2dbcEntityTemplate

    @Test
    fun testChatMessageRepository(): Unit = runBlocking {
        r2dbcTemplate.insert(User(
            id = 1L,
            nickname = "testUser",
            profileImage = "testImage",
            email = "test@test.com",
            password = "test",
        )).awaitSingle()

        r2dbcTemplate.insert(Chatroom(
            id = 1L,
            roomName = "testRoom",
            roomDescription = "testRoom",
            roomImage = "testImage",
        )).awaitSingle()

        val insertElapsedTime = measureTime {
            for(i in 1..100000) {
                r2dbcTemplate.insert(GroupChatMessage(
                    id = i.toLong(),
                    content = "Hello World $i",
                    fromUserId = 1L,
                    chatRoomId = 1L,
                    checked = 100
                )).awaitSingle()
            }
        }


        val selectElapsedTime = measureTime {
            groupChatMessageReactiveRepository.findAllByChatRoomIdOrderByCreatedDateDesc(1L, PageRequest.of(0, 100))
                .collect {
                    log.info { "here is chatRoom: $it" }
                }
        }

        log.info { "insertElapsedTime: $insertElapsedTime" }
        log.info { "selectElapsedTime: $selectElapsedTime" }
    }
}