package com.example.apigateway.config

import com.example.apigateway.entity.User
import com.example.apigateway.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.savedrequest.ServerRequestCache
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@Component
class OAuth2AuthenticationSuccessHandler(
    private val userRedisOperations: ReactiveRedisOperations<String, User>,
    private val userRepository: UserRepository
): ServerAuthenticationSuccessHandler {
    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)
    }

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
        val oidcUser = authentication.principal as OidcUser
        val email = oidcUser.attributes["email"] as String

        return mono(Dispatchers.IO) {
            userRepository.findByEmail(email)
        }
        .flatMap{ user ->
            userRedisOperations.opsForValue().set("user:${user.id}", user)
                .thenReturn(user)
        }
        .doOnSuccess { user ->
            log.info("Successfully authenticated user {}", user)
        }
        .then(
            RedirectServerAuthenticationSuccessHandler("/")
                .onAuthenticationSuccess(webFilterExchange, authentication)
        )
    }
}