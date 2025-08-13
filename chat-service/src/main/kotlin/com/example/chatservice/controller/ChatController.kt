package com.example.chatservice.controller

import com.example.chatservice.dto.ChatUserResponse
import com.example.chatservice.dto.GroupChatMessageRequest
import com.example.chatservice.dto.GroupChatMessageResponse
import com.example.chatservice.dto.GetAllChatMessagesResponse
import com.example.chatservice.service.ChatService
import kotlinx.coroutines.flow.Flow
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping("/chatting")
    suspend fun postChatting(@RequestBody groupChatMessageRequest: GroupChatMessageRequest): GroupChatMessageResponse {
        return chatService.insertChat(groupChatMessageRequest)
    }

    @GetMapping("/chat/{chatRoomId}/{userId}/{pageOffSet}")
    suspend fun getAllChatting(@PathVariable chatRoomId: Long, @PathVariable userId: Long, @PathVariable pageOffSet: Int): Flow<GetAllChatMessagesResponse> {
        return chatService.selectAllChats(chatRoomId, userId, pageOffSet)
    }

    @GetMapping("/chat/users")
    suspend fun getChatUsers(@AuthenticationPrincipal jwt: Jwt): Flow<ChatUserResponse> {
        val userEmail = jwt.claims["email"] as String
        return chatService.getChatUsers(userEmail)
    }

    @GetMapping("/chat/test")
    suspend fun test(@AuthenticationPrincipal jwt: Jwt): String {
        return jwt.tokenValue
    }
}