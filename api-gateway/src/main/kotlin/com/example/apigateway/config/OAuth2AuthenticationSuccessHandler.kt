package com.example.apigateway.config

import com.example.apigateway.entity.User
import com.example.apigateway.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI

@Component
class OAuth2AuthenticationSuccessHandler(
    private val userRedisOperations: ReactiveRedisOperations<String, User>,
    private val userRepository: UserRepository
): ServerAuthenticationSuccessHandler {
    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)
        private val requestCache = WebSessionServerRequestCache()
    }

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
//        val referer = webFilterExchange.exchange.request.headers["Referer"] ?: throw RuntimeException("No Referer Found")
        val oidcUser = authentication.principal as OidcUser
        val email = oidcUser.attributes["email"] as String

        return requestCache.getRedirectUri(webFilterExchange.exchange)
            .defaultIfEmpty(URI.create("/"))
            .flatMap { redirectUrl ->
                mono(Dispatchers.IO) {
                    userRepository.findByEmail(email)
                }
                .flatMap{ user ->
                    userRedisOperations.opsForValue().set("user:${user.id}", user)
                        .thenReturn(user)
                }
                .doOnSuccess { user ->
                    log.info("Here's Redirection {}", redirectUrl)
                    log.info("Successfully authenticated user {}", user)
                }.then(
                    RedirectServerAuthenticationSuccessHandler(redirectUrl.toString())
                        .onAuthenticationSuccess(webFilterExchange, authentication)
                )
            }

    }
}