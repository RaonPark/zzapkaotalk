package com.example.chatservice.controller

import com.example.chatservice.dto.ChatMessageRequest
import com.example.chatservice.service.ChatService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

@Controller
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping("/chatting")
    fun postChatting(@RequestBody chatMessageRequest: ChatMessageRequest): Mono<ResponseEntity<Int>> {
        val result = chatService.insertChat(chatMessageRequest)

        return if(result != -1) {
            Mono.just(ResponseEntity.ok(result))
        } else {
            Mono.just(ResponseEntity.internalServerError().body(-1))
        }
    }
}