package com.example.userservice.support

import org.slf4j.LoggerFactory
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
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration

@Component
class OAuth2AuthenticationSuccessHandler(
    private val authorizedClientRepository: ServerOAuth2AuthorizedClientRepository
): ServerAuthenticationSuccessHandler {
    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)
    }

    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> {
        val oauth2Token = authentication as OAuth2AuthenticationToken
        val clientRegistrationId = oauth2Token.authorizedClientRegistrationId

        return authorizedClientRepository.loadAuthorizedClient<OAuth2AuthorizedClient>(clientRegistrationId, authentication, webFilterExchange.exchange)
            .flatMap<Void> { authorizedClient ->
                val response = webFilterExchange.exchange.response
                val accessToken = authorizedClient.accessToken
                val refreshToken = authorizedClient.refreshToken

                log.info("accessToken={} and refreshToken={}", accessToken.tokenValue, refreshToken?.tokenValue)

                addCookie(response, "access_token", accessToken.tokenValue, Duration.ofMinutes(5))

                if(refreshToken != null) {
                    addCookie(response, "refresh_token", refreshToken.tokenValue, Duration.ofHours(2))
                }

                authorizedClientRepository.removeAuthorizedClient(clientRegistrationId, authentication, webFilterExchange.exchange)
                    .then(Mono.fromRunnable {
                        response.statusCode = HttpStatus.FOUND
                        response.headers.location = URI.create("/loginPage")
                    })
            }.then()
    }

    private fun addCookie(response: ServerHttpResponse, name: String, token: String, duration: Duration) {
        response.addCookie(
            ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(duration)
                .sameSite("Strict")
                .build()
        )
    }
}