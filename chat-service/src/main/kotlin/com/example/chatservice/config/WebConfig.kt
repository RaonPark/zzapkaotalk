package com.example.chatservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping

@Configuration
class WebConfig {
    @Bean
    fun handlerMapping(): HandlerMapping {
        val map = mapOf("/websocket" to ChatWebSocketHandler)
        val order = -1

        return SimpleUrlHandlerMapping(map, order)
    }


}