package com.example.apigateway.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@RestController
class TestController(
    private val webClientBuilder: WebClient.Builder,
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

}