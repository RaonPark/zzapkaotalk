package com.example.apigateway.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.web.bind.annotation.RestController

@RestController
class RSocketController {
    companion object {
        private val log = KotlinLogging.logger {}
    }

    @ConnectMapping("chat.connect")
    fun connection(@AuthenticationPrincipal user: OidcUser?) {
        log.info { "user found: $user" }
    }
}