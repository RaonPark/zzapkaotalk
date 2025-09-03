package com.example.apigateway.config

import com.example.apigateway.entity.ChatUserResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlow

@RestController
class BFFController(
    private val apiWebClient: WebClient,
    private val oAuth2AuthorizedClientManager: DefaultReactiveOAuth2AuthorizedClientManager
) {
    companion object {
        val log = KotlinLogging.logger { }
    }

    @Value("\${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
    private lateinit var issuerUri: String


    @GetMapping("/loginPage")
    fun loginPage(@AuthenticationPrincipal oidcUser: OidcUser): String {
        return oidcUser.toString()
    }

    @GetMapping("/")
    fun defaultPage(@AuthenticationPrincipal user: OidcUser): String {
        return user.toString()
    }

    @GetMapping("/checkLogin")
    fun checkLogin(@AuthenticationPrincipal user: OidcUser?): ResponseEntity<String> =
        if(user == null) {
            log.info { "User not found" }
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized")
        } else {
            log.info { "User found = $user" }
            log.info { "User Jwt = ${user.idToken.tokenValue}" }
            ResponseEntity.ok(user.idToken.tokenValue)
        }

    @GetMapping("/chat/users")
    suspend fun getChatUsers(@AuthenticationPrincipal user: OidcUser?): Flow<ChatUserResponse> {
        if(user == null) {
            log.info { "User not found" }
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized")

            return emptyFlow()
        } else {
            log.info { "getChatUsers() User found = $user" }
            log.info { "getChatUsers() User Jwt = ${user.idToken.tokenValue}" }
            return apiWebClient
                .get()
                .uri("/chat-service/chat/users")
                .header("Authorization", "Bearer ${user.idToken.tokenValue}")
                .retrieve()
                .bodyToFlow<ChatUserResponse>()
        }
    }

    data class Me(
        val email: String,
        val nickname: String,
        val profileImage: String,
        val lastSeen: String,
    )

    @GetMapping("/me")
    suspend fun me(@AuthenticationPrincipal user: OidcUser?): Me {
        if(user != null) {
            log.info { "me() User found = $user" }
            return apiWebClient
                .get()
                .uri("/auth/user/me")
                .header("Authorization", "Bearer ${user.idToken.tokenValue}")
                .retrieve()
                .awaitBody<Me>()
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized")
            return Me(
                email = "anonymous",
                nickname = "anonymous",
                profileImage = "none",
                lastSeen = "X"
            )
        }
    }
}