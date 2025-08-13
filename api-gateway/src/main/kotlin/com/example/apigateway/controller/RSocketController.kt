package com.example.apigateway.controller

import com.example.apigateway.entity.DirectChatMessageRequest
import com.example.apigateway.entity.DirectChatMessageResponse
import com.example.apigateway.entity.DirectChatStreamRequest
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
    private val chattingServerRequester: RSocketRequester.Builder
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

    @MessageMapping("chat.direct.previous")
    suspend fun getPreviousMessages(@AuthenticationPrincipal jwt: Jwt, @Payload toUserEmail: String): Flow<DirectChatMessageResponse> {
        val userEmail = jwt.claims["email"] ?: TODO("Throw Keycloak Exception")

        log.info { "user email: $userEmail" }

        return chattingServerRequester
            .rsocketConnector { connector -> connector.reconnect(Retry.fixedDelay(10, Duration.ofMillis(500))) }
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.string))
            .connectWebSocketAndAwait(URI.create("ws://localhost:28079/rsocket"))
            .route("chat.direct.previous")
            .data(DirectChatStreamRequest(
                userEmail as String, toUserEmail
            ))
            .retrieveFlow<DirectChatMessageResponse>()
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
            .rsocketConnector { connector -> connector.reconnect(Retry.fixedDelay(10, Duration.ofMillis(500))) }
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.string))
            .connectWebSocketAndAwait(URI.create("ws://localhost:28079/rsocket"))
            .route("chat.direct.send")
            .data(message)
            .send()
            .awaitSingleOrNull()
    }

    @MessageMapping("chat.direct.stream")
    suspend fun directMessageStream(
        @AuthenticationPrincipal jwt: Jwt,
        @DestinationVariable toUserEmail: String
    ): Flow<DirectChatMessageResponse> {
        log.info { "subscribe message: $toUserEmail" }
        log.info { "from : ${jwt.claims["email"]}"}

        return chattingServerRequester
            .rsocketConnector { connector -> connector.reconnect(Retry.fixedDelay(10, Duration.ofMillis(500))) }
            .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
            .metadataMimeType(MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.string))
            .connectWebSocketAndAwait(URI.create("ws://localhost:28079/rsocket"))
            .route("chat.direct.stream")
            .data(DirectChatStreamRequest(
                fromUserEmail = jwt.claims["email"] as String,
                toUserEmail = toUserEmail
            ))
            .retrieveFlow<DirectChatMessageResponse>()
    }
}