package com.example.apigateway.controller

import com.example.apigateway.entity.DirectChatMessageRequest
import com.example.apigateway.entity.DirectChatMessageResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.rsocket.metadata.WellKnownMimeType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.messaging.rsocket.connectWebSocketAndAwait
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.stereotype.Controller
import org.springframework.util.MimeTypeUtils
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.net.URI
import java.time.Duration

@Controller
class RSocketController(
    private val chattingServerRequester: RSocketRequester
) {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ConnectMapping
    fun connection(@AuthenticationPrincipal jwt: Jwt?) {
        log.info { "connection established" }

        if(jwt != null) {
            log.info { "user connected : ${jwt.tokenValue}" }
        } else {
            log.info { "user not connected" }
        }
    }

    data class Response(val message: String)

    @MessageMapping("chat.direct.stream")
    suspend fun getPreviousMessages(@AuthenticationPrincipal jwt: Jwt): Flow<DirectChatMessageResponse> {
        val userEmail = jwt.claims["email"] ?: TODO("Throw Keycloak Exception")

        log.info { "user email: $userEmail" }

        return chattingServerRequester
            .route("chat.direct.stream")
            .data(userEmail)
            .retrieveFlow<DirectChatMessageResponse>()
    }

    @MessageMapping("chat.chat.direct")
    suspend fun directMessage(@AuthenticationPrincipal jwt: Jwt, rSocketRequester: RSocketRequester, message: DirectChatMessageRequest): Flow<Response> {
        log.info { "direct message produced by ${jwt.claims["email"]}" }
        log.info { "rSocket request received" }

        rSocketRequester
            .route("chat.direct.${jwt.claims["email"]}")
            .data(message)
            .send()
            .awaitSingle()

        return flow {
            emit(Response("hello world"))
            emit(Response("Next word"))
        }
    }

    @MessageMapping("chat.direct.send")
    suspend fun directMessageSend(
        @AuthenticationPrincipal jwt: Jwt,
        @Payload message: DirectChatMessageRequest
    ) {
        log.info { "fire and forget message: $message" }
        log.info { "from : ${jwt.claims["email"]}"}

        val userEmail = jwt.claims["email"]

        chattingServerRequester
            .route("chat.direct.send")
            .data(message)
            .send()
            .awaitSingleOrNull()
    }
}